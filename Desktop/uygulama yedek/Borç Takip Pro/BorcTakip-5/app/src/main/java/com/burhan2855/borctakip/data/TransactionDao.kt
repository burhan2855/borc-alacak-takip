package com.burhan2855.borctakip.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction as RoomTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactionsOnce(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Long): Flow<Transaction?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionByIdOnce(id: Long): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionByIdSync(id: Long): Transaction?
    
    @Query("SELECT * FROM transactions WHERE documentId = :documentId")
    suspend fun getTransactionByDocumentId(documentId: String): Transaction?

    @androidx.room.Transaction
    suspend fun syncTransactions(transactions: List<Transaction>) {
        // Merge strategy: upsert Firestore data without wiping local records
        val filtered = transactions.filter { !it.documentId.isNullOrBlank() }
        
        filtered.forEach { transaction ->
            // Check if transaction with this documentId already exists
            val existing = getTransactionByDocumentId(transaction.documentId!!)
            if (existing != null) {
                // Update existing transaction keeping the local id
                val updated = Transaction(
                    id = existing.id,
                    contactId = transaction.contactId,
                    amount = transaction.amount,
                    type = transaction.type,
                    description = transaction.description,
                    date = transaction.date,
                    dueDate = transaction.dueDate,
                    isPaid = transaction.isPaid,
                    isSynced = transaction.isSynced,
                    category = transaction.category,
                    title = transaction.title,
                    isDebt = transaction.isDebt,
                    status = transaction.status,
                    remainingAmount = transaction.remainingAmount,
                    documentId = transaction.documentId,
                    paymentType = transaction.paymentType,
                    transactionDate = transaction.transactionDate
                )
                updateTransaction(updated)
            } else {
                // Insert new transaction
                insertTransaction(transaction)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}