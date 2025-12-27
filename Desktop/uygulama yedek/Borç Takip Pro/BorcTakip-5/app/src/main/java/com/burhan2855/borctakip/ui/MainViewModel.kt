package com.burhan2855.borctakip.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.burhan2855.borctakip.data.*
import com.burhan2855.borctakip.data.calendar.CalendarManager
import com.burhan2855.borctakip.data.calendar.CalendarSettingsRepository
import com.burhan2855.borctakip.util.NotificationWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class MainViewModel(
    application: Application,
    private val transactionRepository: TransactionRepository,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val calendarManager: CalendarManager,
    private val calendarSettingsRepository: CalendarSettingsRepository
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _errorFlow = MutableStateFlow<String?>(null)
    val errorFlow: StateFlow<String?> = _errorFlow.asStateFlow()

    val allTransactions: StateFlow<List<Transaction>> = transactionRepository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val kasaBalance: StateFlow<Double> = allTransactions.map { transactions ->
        transactions
            .filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }
            .sumOf { if (it.category == "Kasa Girişi") it.amount.toDouble() else -it.amount.toDouble() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val bankaBalance: StateFlow<Double> = allTransactions.map { transactions ->
        transactions
            .filter { it.category == "Banka Girişi" || it.category == "Banka Çıkışı" }
            .sumOf { if (it.category == "Banka Girişi") it.amount.toDouble() else -it.amount.toDouble() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allContacts: StateFlow<List<Contact>> = contactRepository.allContacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currencyCode: StateFlow<String> = settingsRepository.currencyCodeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "TRY"
    )

    val currencySymbol: StateFlow<String> = settingsRepository.currencySymbolFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "₺"
    )

    val language: StateFlow<String> = settingsRepository.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "tr"
    )

    private var dataSyncInitialized = false

    fun initializeDataSync() {
        if (dataSyncInitialized) {
            Log.d("MainViewModel", "Data sync already initialized, skipping...")
            return
        }
        
        dataSyncInitialized = true
        Log.d("MainViewModel", "=== INITIALIZING DATA SYNC ===")
        // Start contacts first so FK targets exist before transactions sync
        Log.d("MainViewModel", "Starting ContactRepository listener...")
        contactRepository.startListeningForChanges()

        Log.d("MainViewModel", "Starting TransactionRepository listener...")
        transactionRepository.startListeningForChanges()
        
        Log.d("MainViewModel", "Fixing historical transactions with missing paymentType...")
        // Fix any historical transactions that missed paymentType metadata
        viewModelScope.launch {
            try {
                transactionRepository.fixMissingPaymentTypes()
                Log.d("MainViewModel", "Historical transactions fixed successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fixing historical transactions: ${e.message}", e)
                _errorFlow.value = "Önceki işlemler güncellenirken hata: ${e.message}"
            }
        }
        
        Log.d("MainViewModel", "Fixing Kasa/Banka transactions with wrong isDebt value...")
        // FIX: Kasa/Banka işlemlerinin yanlış isDebt değerlerini düzelt
        viewModelScope.launch {
            try {
                transactionRepository.fixCashBankIsDebt()
                Log.d("MainViewModel", "Kasa/Banka isDebt values fixed successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fixing Kasa/Banka isDebt: ${e.message}", e)
            }
        }
        
        Log.d("MainViewModel", "=== DATA SYNC INITIALIZED ===")
    }

    fun getTransactionById(id: Long): Flow<Transaction?> = transactionRepository.getTransactionById(id)

    fun insert(transaction: Transaction) = viewModelScope.launch {
        try {
            Log.d("DB_DUMP", "=== ViewModel: Calling repository.insert() ===")
            Log.d("DB_DUMP", "Input transaction.isDebt: ${transaction.isDebt}")
            Log.d("DB_DUMP", "Input transaction.type: ${transaction.type}")
            Log.d("DB_DUMP", "Input transaction.title: ${transaction.title}")
            
            val insertedId = transactionRepository.insert(transaction)
            
            if (insertedId <= 0) {
                Log.e("DB_DUMP", "ERROR: Transaction was not saved properly (ID: $insertedId)")
                _errorFlow.value = "İşlem kaydedilemedi"
                return@launch
            }
            
            Log.d("DB_DUMP", "✅ Transaction saved with ID: $insertedId")

            // Veritabanında kaydedilmesinden sonra takvim etkinliğini oluştur
            Log.d("DB_DUMP", "Transaction successfully saved, now creating calendar event")
            
            // DAO'dan tekrar oku - transaction'ın tamamen kaydedildiğini doğrula
            val savedTransaction = transactionRepository.getTransactionByIdOnce(insertedId)
            
            if (savedTransaction != null) {
                Log.d("DB_DUMP", "Transaction verified in database with ID: ${savedTransaction.id}")
                Log.d("DB_DUMP", "Saved transaction.isDebt: ${savedTransaction.isDebt}")
                Log.d("DB_DUMP", "Saved transaction.type: ${savedTransaction.type}")
                
                // Takvim etkinliği oluştur - TÜM işlemler için (kasa/banka işlemleri hariç)
                val isCashBankTransaction = savedTransaction.category?.let {
                    it == "Kasa Çıkışı" || it == "Banka Çıkışı" || 
                    it == "Kasa Girişi" || it == "Banka Girişi"
                } ?: false
                
                if (!isCashBankTransaction) {
                    Log.d("DB_DUMP", "Creating calendar event for transaction ID: ${savedTransaction.id}, category: '${savedTransaction.category}'")
                    try {
                        Log.d("DB_DUMP", "Before calling handleCalendarEvent, transaction ID: ${savedTransaction.id}")
                        handleCalendarEvent(savedTransaction)
                        Log.d("DB_DUMP", "Calendar event created successfully")
                    } catch (calendarError: Exception) {
                        Log.e("DB_DUMP", "Calendar event creation failed: ${calendarError.message}", calendarError)
                        calendarError.printStackTrace()
                        // Takvim hatası işlemi başarısız kılmasın - transaction zaten kaydedildi
                    }
                }
                
                // Bildirim zamanla (sadece ödenmemiş borçlar için)
                if (savedTransaction.isDebt && savedTransaction.status == "Ödenmedi") {
                    Log.d("DB_DUMP", "Scheduling notification for debt transaction")
                    scheduleNotification(savedTransaction)
                }
            } else {
                Log.e("DB_DUMP", "ERROR: Transaction saved but could not be read from database (ID: $insertedId)")
                _errorFlow.value = "İşlem kaydedildi ancak doğrulanamadı"
                return@launch
            }
        } catch (e: Exception) {
            Log.e("DB_DUMP", "=== INSERT TRANSACTION ERROR: ${e.message} ===", e)
            e.printStackTrace()
            _errorFlow.value = "İşlem kaydedilemedi: ${e.message}"
        }
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        try {
            transactionRepository.update(transaction)
            if (transaction.isDebt && transaction.status == "Ödenmedi") {
                scheduleNotification(transaction)
            } else {
                cancelNotification(transaction)
            }

            viewModelScope.launch {
                try {
                    handleCalendarEventUpdate(transaction)
                } catch (calendarError: Exception) {
                    Log.e("DB_DUMP", "Calendar event update failed: ${calendarError.message}", calendarError)
                }
            }
        } catch (e: Exception) {
            _errorFlow.value = "İşlem güncellenemedi: ${e.message}"
        }
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        try {
            transactionRepository.delete(transaction)
            cancelNotification(transaction)
            calendarManager.deleteTransactionEvent(transaction.id)
        } catch (e: Exception) {
            _errorFlow.value = "İşlem silinemedi: ${e.message}"
        }
    }

    fun processPayment(transaction: Transaction, paymentSource: String) = viewModelScope.launch {
        try {
            Log.d("DB_DUMP", "=== PAYMENT PROCESSING START ===")
            Log.d("DB_DUMP", "Transaction ID: ${transaction.id}")
            Log.d("DB_DUMP", "Transaction amount: ${transaction.amount}")
            Log.d("DB_DUMP", "Payment source: $paymentSource")

            // Validate transaction ID
            if (transaction.id == 0L) {
                Log.e("DB_DUMP", "ERROR: Transaction ID is 0")
                _errorFlow.value = "Hata: İşlem kaydı bulunamadı"
                return@launch
            }

            // Validate transaction is not already paid
            if (transaction.status == "Ödendi") {
                Log.e("DB_DUMP", "ERROR: Transaction already paid")
                _errorFlow.value = "Hata: Bu borç zaten ödenmiş"
                return@launch
            }

            val paymentAmount = transaction.amount

            // Step 1: Create cash flow transaction (Kasa/Banka kaydı)
            val cashFlowTransaction = Transaction(
                title = if (transaction.isDebt) "Ödeme: ${transaction.title}" else "Tahsilat: ${transaction.title}",
                amount = paymentAmount,
                date = System.currentTimeMillis(),
                transactionDate = System.currentTimeMillis(),
                isDebt = false, // Kasa/Banka işlemleri ASLA borç/alacak değildir
                status = "Ödendi",
                paymentType = paymentSource,
                // Borç ödeme = Çıkış, Alacak tahsilat = Giriş
                category = if (transaction.isDebt) {
                    if (paymentSource == "Kasa") "Kasa Çıkışı" else "Banka Çıkışı"
                } else {
                    if (paymentSource == "Kasa") "Kasa Girişi" else "Banka Girişi"
                },
                contactId = transaction.contactId
            )
            
            Log.d("DB_DUMP", "Creating cash flow transaction: ${cashFlowTransaction.title}")
            try {
                transactionRepository.insert(cashFlowTransaction)
            } catch (e: Exception) {
                Log.e("DB_DUMP", "CRITICAL: Failed to insert cash flow transaction: ${e.message}", e)
                _errorFlow.value = "Ödeme kaydedilemedi: ${e.message}"
                return@launch
            }
            Log.d("DB_DUMP", "Cash flow transaction created")

            // Step 2: Mark original transaction as paid
            Log.d("DB_DUMP", "Marking transaction ${transaction.id} as paid")
            val paidTransaction = transaction.copy(
                status = "Ödendi",
                remainingAmount = 0.0
            )
            
            try {
                transactionRepository.update(paidTransaction)
                Log.d("DB_DUMP", "Transaction marked as paid successfully")
                cancelNotification(paidTransaction)
                handleCalendarEventUpdate(paidTransaction)
                Log.d("DB_DUMP", "=== PAYMENT COMPLETED SUCCESSFULLY ===")
                _errorFlow.value = "Ödeme başarıyla kaydedildi"
            } catch (e: Exception) {
                Log.e("DB_DUMP", "CRITICAL: Failed to mark transaction as paid: ${e.message}", e)
                _errorFlow.value = "Ödeme işlemi başarısız: ${e.message}"
            }
        } catch (e: Exception) {
            Log.e("DB_DUMP", "EXCEPTION in processPayment: ${e.message}", e)
            _errorFlow.value = "Ödeme sırasında hata: ${e.message}"
        }
    }

    // Keep this for backward compatibility if needed
    fun processPartialPayment(transaction: Transaction, paymentSource: String) = viewModelScope.launch {
        processPayment(transaction, paymentSource)
    }

    private suspend fun handleCalendarEvent(transaction: Transaction) {
        Log.d("DB_DUMP", "handleCalendarEvent called for transaction ID: ${transaction.id}")
        try {
            Log.d("DB_DUMP", "=== handleCalendarEvent START ===")
            Log.d("DB_DUMP", "Transaction: ${transaction.title}, ID: ${transaction.id}, Status: ${transaction.status}")
            
            calendarSettingsRepository.initializeDefaultSettings()
            Log.d("DB_DUMP", "CalendarSettingsRepository initialized")
            
            val settings = calendarSettingsRepository.getSettingsSync()
            Log.d("DB_DUMP", "Settings retrieved: ${settings?.autoCreateReminders}")
            
            // Tüm işlemleri takvime ekle - ayar kontrol etme
            Log.d("DB_DUMP", "Calling calendarManager.createPaymentReminder")
            calendarManager.createPaymentReminder(transaction)
            Log.d("DB_DUMP", "calendarManager.createPaymentReminder completed")
            
            Log.d("DB_DUMP", "=== handleCalendarEvent SUCCESS ===")
        } catch (e: Exception) {
            Log.e("DB_DUMP", "=== handleCalendarEvent ERROR: ${e.message} ===", e)
            throw e
        }
    }

    private suspend fun handleCalendarEventUpdate(transaction: Transaction) {
        try {
            val settings = calendarSettingsRepository.getSettingsSync()
            Log.d("DB_DUMP", "Updating calendar event for transaction: ${transaction.id}, status: ${transaction.status}")
            
            if (transaction.status == "Ödendi") {
                Log.d("DB_DUMP", "Transaction paid, deleting calendar event")
                calendarManager.deleteTransactionEvent(transaction.id)
            } else {
                Log.d("DB_DUMP", "Transaction not paid, updating calendar event")
                calendarManager.updateTransactionEvent(
                    transaction.id,
                    com.burhan2855.borctakip.data.calendar.EventUpdates(
                        title = if (settings?.privacyModeEnabled == true) "Payment Reminder" else "Payment: ${transaction.amount} - ${transaction.title}",
                        startTime = transaction.date,
                        endTime = transaction.date?.plus(60 * 60 * 1000)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("DB_DUMP", "Error updating calendar event: ${e.message}", e)
            _errorFlow.value = "Takvim etkinliği güncellenemedi: ${e.message}"
        }
    }

    fun insertContact(contact: Contact) = viewModelScope.launch {
        contactRepository.insert(contact)
    }

    fun updateContact(contact: Contact) = viewModelScope.launch {
        try {
            contactRepository.update(contact)
            Log.d("MainViewModel", "Contact updated: ${contact.name}")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error updating contact", e)
            _errorFlow.value = "Kişi güncellenirken hata: ${e.message}"
        }
    }

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        try {
            // Kişiye ait işlemleri kontrol et
            val contactTransactions = allTransactions.value.filter { 
                it.contactId?.toLong() == contact.id 
            }
            
            if (contactTransactions.isNotEmpty()) {
                // Kişiye ait işlemler varsa contactId'yi null yap
                contactTransactions.forEach { transaction ->
                    val updatedTransaction = transaction.copy(contactId = null)
                    transactionRepository.update(updatedTransaction)
                }
                Log.d("MainViewModel", "Cleared contactId from ${contactTransactions.size} transactions")
            }
            
            // Firestore'dan sil
            val userCollection = contactRepository.getContactsCollection()
            contact.documentId?.let { docId ->
                userCollection?.document(docId)?.delete()?.await()
            }
            
            // Local DB'den de sil
            contactRepository.clearContact(contact)
            Log.d("MainViewModel", "Contact deleted: ${contact.name}")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error deleting contact", e)
            _errorFlow.value = "Kişi silinirken hata: ${e.message}"
        }
    }

    fun setCurrency(code: String, symbol: String) = viewModelScope.launch {
        settingsRepository.setCurrency(code, symbol)
    }

    fun setLanguage(language: String): Job = viewModelScope.launch {
        settingsRepository.setLanguage(language)
    }

    fun clearError() {
        _errorFlow.value = null
    }

    fun onSignOut() = viewModelScope.launch {
        transactionRepository.stopListeningForChanges()
        // Leave local data intact so user records persist across logout/login
        contactRepository.stopListeningForChanges()
    }

    private fun scheduleNotification(transaction: Transaction) {
        val now = Calendar.getInstance().timeInMillis
        val delay = (transaction.date ?: 0L) - now

        if (delay > 0) {
            val data = Data.Builder()
                .putString("title", transaction.title)
                .putDouble("amount", transaction.amount.toDouble())
                .putString("currencySymbol", currencySymbol.value)
                .build()

            val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            workManager.enqueueUniqueWork(
                "notification_${transaction.id}",
                ExistingWorkPolicy.REPLACE,
                notificationWork
            )
        }
    }

    private fun cancelNotification(transaction: Transaction) {
        workManager.cancelUniqueWork("notification_${transaction.id}")
    }

    fun onTransactionUpdated(transaction: Transaction) {
        viewModelScope.launch {
            val originalTransaction = transactionRepository.getTransactionByIdOnce(transaction.id)
            if (originalTransaction != null) {
                val amountDifference = transaction.amount - originalTransaction.amount
                transactionRepository.update(transaction)
                updateContactBalance(transaction.contactId, amountDifference)
            }
        }
    }

    fun onTransactionDeleted(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
            // Revert the balance change
            updateContactBalance(transaction.contactId, -transaction.amount)
        }
    }

    private suspend fun updateContactBalance(contactId: Int?, amountChange: Double) {
        if (contactId != null) {
            val contact = contactRepository.allContacts.first().find { it.id.toInt() == contactId }
            if (contact != null) {
                val newBalance = contact.balance + amountChange
                contactRepository.update(contact.copy(balance = newBalance))
            }
        }
    }

    fun fixHistoricalTransactions() {
        viewModelScope.launch {
            val allTransactions = transactionRepository.allTransactions.first()
            val allContacts = contactRepository.allContacts.first()

            val contactBalances = mutableMapOf<Int, Double>()

            for (contact in allContacts) {
                contactBalances[contact.id.toInt()] = 0.0
            }

            for (transaction in allTransactions.sortedBy { it.date }) {
                transaction.contactId?.let { contactId ->
                    val balance = contactBalances[contactId] ?: 0.0
                    val newBalance = if (transaction.type == "debt" || transaction.type == "payment_out") {
                        balance - transaction.amount
                    } else {
                        balance + transaction.amount
                    }
                    contactBalances[contactId] = newBalance
                }
            }

            for ((contactId, balance) in contactBalances) {
                val contact = allContacts.find { it.id.toInt() == contactId }
                if (contact != null && contact.balance != balance) {
                    contactRepository.update(contact.copy(balance = balance))
                }
            }
            Log.d("MainViewModel", "Historical transactions fixed successfully")
        }
    }
}

class MainViewModelFactory(
    private val application: Application,
    private val transactionRepository: TransactionRepository,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val calendarManager: CalendarManager,
    private val calendarSettingsRepository: CalendarSettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                application, 
                transactionRepository, 
                contactRepository, 
                settingsRepository, 
                calendarManager, 
                calendarSettingsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}