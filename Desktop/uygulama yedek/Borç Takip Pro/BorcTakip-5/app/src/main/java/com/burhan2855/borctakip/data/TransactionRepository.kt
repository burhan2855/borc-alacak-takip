package com.burhan2855.borctakip.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class TransactionRepository(private val transactionDao: TransactionDao) {

    private val firestore = Firebase.firestore
    private val transactionsCollection = firestore.collection("transactions")
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var listenerRegistration: ListenerRegistration? = null

    private fun getTransactionsCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("transactions")
    }

    val allTransactions: Flow<List<Transaction>> = transactionDao.getTransactions()

    private fun normalizeTransaction(input: Transaction): Transaction {
        val category = input.category ?: ""
        val isCashBank = category == "Kasa Girişi" || category == "Banka Girişi" ||
                category == "Kasa Çıkışı" || category == "Banka Çıkışı"

        val typeFromCategory = when {
            isCashBank && category.contains("Giriş") -> "cash_in"
            isCashBank && category.contains("Çıkışı") -> "cash_out"
            else -> null
        }

        // Trust explicit type first; only derive when type is blank.
        val normalizedType = when {
            typeFromCategory != null -> typeFromCategory
            input.type.isNotBlank() -> input.type  // Use explicit type if provided
            else -> if (input.isDebt) "debt" else "credit"
        }

        val normalizedIsDebt = when {
            isCashBank -> false
            normalizedType == "credit" -> false
            normalizedType == "cash_in" -> false
            normalizedType == "cash_out" -> false
            normalizedType == "debt" -> true
            else -> input.isDebt  // Keep original if type is unknown
        }

        return input.copy(
            isDebt = normalizedIsDebt,
            type = normalizedType
        )
    }

    suspend fun insert(transaction: Transaction): Long {
        val normalized = normalizeTransaction(transaction)
        Log.d("DB_DUMP", "=== INSERT TRANSACTION START ===")
        Log.d("DB_DUMP", "Transaction Title: ${normalized.title}")
        Log.d("DB_DUMP", "Is Debt: ${normalized.isDebt}")
        Log.d("DB_DUMP", "Category: ${normalized.category}")
        
        val id = transactionDao.insertTransaction(normalized)
        Log.d("DB_DUMP", "Local DB insert returned ID: $id")
        
        if (id <= 0) {
            Log.e("DB_DUMP", "❌ CRITICAL: insertTransaction returned ID: $id")
            return 0
        }
        
        val userCollection = getTransactionsCollection()
        if (userCollection == null) {
            Log.e("DB_DUMP", "❌ User not logged in! Cannot sync to Firestore")
            // Even without Firestore, update local with the ID
            val newTransaction = transaction.copy(id = id, isSynced = false)
            transactionDao.updateTransaction(newTransaction)
            Log.d("DB_DUMP", "Transaction saved locally with ID: $id (not synced)")
            return id
        }
        
        val documentId = transaction.documentId ?: userCollection.document().id
        val newTransaction = normalized.copy(id = id, documentId = documentId, isSynced = true)
        transactionDao.updateTransaction(newTransaction)
        
        try {
            userCollection.document(documentId).set(newTransaction).await()
            Log.d("DB_DUMP", "✅ Transaction saved with ID: $id and synced to Firestore")
        } catch (e: Exception) {
            Log.e("DB_DUMP", "Firestore sync failed: ${e.message}", e)
        }
        
        return id
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        val userCollection = getTransactionsCollection()
        transaction.documentId?.let { docId ->
            userCollection?.document(docId)?.set(transaction)?.await()
                ?: Log.w("TransactionRepo", "User not logged in, transaction update not synced")
        }
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        val userCollection = getTransactionsCollection()
        transaction.documentId?.let { docId ->
            userCollection?.document(docId)?.delete()?.await()
                ?: Log.w("TransactionRepo", "User not logged in, transaction deletion not synced")
        }
    }

    suspend fun syncTransactions() {
        try {
            val userCollection = getTransactionsCollection()
            if (userCollection == null) {
                Log.w("TransactionRepo", "User not logged in, cannot sync transactions")
                return
            }
            
            val snapshot = userCollection.get().await()
            val firestoreTransactions = snapshot.toObjects(Transaction::class.java)
                .map { transaction ->
                    normalizeTransaction(transaction)
                }
            Log.d("TransactionRepo", "Received ${firestoreTransactions.size} transactions from Firestore")
            val localTransactions = transactionDao.getAllTransactionsOnce()

            for (firestoreTransaction in firestoreTransactions) {
                val localTransaction = localTransactions.find { it.documentId == firestoreTransaction.documentId }
                if (localTransaction == null) {
                    transactionDao.insertTransaction(firestoreTransaction)
                } else {
                    val firestoreTime = firestoreTransaction.transactionDate ?: 0L
                    val localTime = localTransaction.transactionDate ?: 0L
                    if (firestoreTime > localTime) {
                        transactionDao.updateTransaction(firestoreTransaction)
                    }
                }
            }
            Log.d("TransactionRepo", "Synced ${firestoreTransactions.size} transactions to local DB")
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Error syncing transactions", e)
        }
    }

    fun startListeningForChanges() {
        Log.d("TransactionRepo", "startListeningForChanges called")
        listenerRegistration?.remove()
        val currentUserId = auth.currentUser?.uid
        Log.d("TransactionRepo", "Current user ID: $currentUserId")
        
        currentUserId?.let {
            scope.launch {
                try {
                    val collection = getTransactionsCollection()
                    Log.d("TransactionRepo", "Transactions collection: $collection")
                    
                    listenerRegistration = collection?.orderBy("date", Query.Direction.DESCENDING)
                        ?.addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e("TransactionRepo", "Firestore listener error: ${error.message}", error)
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                Log.d("TransactionRepo", "Received ${snapshot.size()} transactions from Firestore")
                                try {
                                    val transactions = mutableListOf<Transaction>()
                                    for (document in snapshot.documents) {
                                        try {
                                            val transaction = document.toObject(Transaction::class.java)
                                            if (transaction != null) {
                                                val updatedTransaction = normalizeTransaction(transaction.copy(documentId = document.id))
                                                transactions.add(updatedTransaction)
                                            }
                                        } catch (e: Exception) {
                                            Log.e("TransactionRepo", "Error deserializing individual transaction: ${e.message}", e)
                                            // Skip this document and continue with others
                                        }
                                    }
                                    
                                    scope.launch {
                                        if (transactions.isNotEmpty()) {
                                            transactionDao.syncTransactions(transactions)
                                            Log.d("TransactionRepo", "Synced ${transactions.size} transactions to local DB")
                                        } else {
                                            Log.d("TransactionRepo", "No valid transactions to sync")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("TransactionRepo", "Error processing Firestore transactions: ${e.message}", e)
                                    // Continue with offline mode - transactions can still be accessed from local DB
                                }
                            }
                        }
                    Log.d("TransactionRepo", "Firestore listener registered successfully for transactions")
                } catch (e: Exception) {
                    Log.e("TransactionRepo", "Failed to start Firestore listener: ${e.message}", e)
                }
            }
        } ?: Log.w("TransactionRepo", "Cannot start listener - user not signed in")
    }

    fun stopListeningForChanges() {
        Log.d("TransactionRepo", "stopListeningForChanges called")
        listenerRegistration?.remove()
    }

    fun getTransactionById(id: Long): Flow<Transaction?> = transactionDao.getTransactionById(id)

    suspend fun getAllTransactionsOnce(): List<Transaction> = transactionDao.getAllTransactionsOnce()

    suspend fun getTransactionByIdOnce(id: Long): Transaction? = transactionDao.getTransactionByIdOnce(id)

    // New helper: fix transactions that lack paymentType but have category indicating Kasa/Banka
    suspend fun fixMissingPaymentTypes() {
        withContext(Dispatchers.IO) {
            try {
                val all = transactionDao.getAllTransactionsOnce()
                all.forEach { t ->
                    if (t.paymentType.isBlank()) {
                        val updated = when {
                            t.category?.contains("Kasa", ignoreCase = true) == true -> t.copy(paymentType = "Kasa")
                            t.category?.contains("Banka", ignoreCase = true) == true -> t.copy(paymentType = "Banka")
                            else -> null
                        }
                        if (updated != null) {
                            transactionDao.updateTransaction(updated)
                            updated.documentId?.let { docId ->
                                try {
                                    getTransactionsCollection()?.document(docId)?.set(updated)?.await()
                                } catch (e: Exception) {
                                    Log.e("DB_DUMP", "Failed to sync fixed paymentType to Firestore: ${e.message}")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DB_DUMP", "fixMissingPaymentTypes failed: ${e.message}")
            }
        }
    }

    // FIX: Kasa/Banka işlemlerinin yanlış isDebt değerlerini düzelt
    suspend fun fixCashBankIsDebt() {
        withContext(Dispatchers.IO) {
            try {
                val all = transactionDao.getAllTransactionsOnce()
                var fixedCount = 0

                all.forEach { t ->
                    val fixed = normalizeTransaction(t)
                    if (fixed != t) {
                        transactionDao.updateTransaction(fixed)
                        fixed.documentId?.let { docId ->
                            try {
                                getTransactionsCollection()?.document(docId)?.set(fixed)?.await()
                            } catch (e: Exception) {
                                Log.e("TransactionRepo", "Failed to sync fixed isDebt to Firestore: ${e.message}")
                            }
                        }
                        fixedCount++
                    }
                }

                Log.d("TransactionRepo", "Fixed $fixedCount transactions")
            } catch (e: Exception) {
                Log.e("TransactionRepo", "fixCashBankIsDebt failed: ${e.message}")
            }
        }
    }
}