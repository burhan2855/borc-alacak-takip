package com.burhan2855.borctakip

import android.app.Application
import android.content.Context
import android.util.Log
import com.burhan2855.borctakip.data.AppDatabase
import com.burhan2855.borctakip.data.ContactRepository
import com.burhan2855.borctakip.data.PartialPaymentRepository
import com.burhan2855.borctakip.data.SettingsRepository
import com.burhan2855.borctakip.data.TransactionRepository
import com.burhan2855.borctakip.data.calendar.*
import com.burhan2855.borctakip.gemini.GeminiViewModel
import com.burhan2855.borctakip.gemini.GeminiPreferencesManager
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.util.LocaleHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebtApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)
    private val database by lazy { AppDatabase.getDatabase(this) }

    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val contactRepository by lazy { ContactRepository(database.contactDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
    val calendarEventRepository: CalendarEventRepository by lazy { CalendarEventRepositoryImpl(database.calendarEventDao()) }
    val calendarSettingsRepository: CalendarSettingsRepository by lazy { CalendarSettingsRepositoryImpl(database.calendarSettingsDao()) }
    val calendarManager: CalendarManager by lazy { CalendarManagerImpl(this, database.calendarEventDao(), calendarSettingsRepository) }
    val partialPaymentRepository by lazy { PartialPaymentRepository(database.partialPaymentDao()) }
    val calendarPermissionHandler: CalendarPermissionHandler by lazy { CalendarPermissionHandlerImpl(this) }
    val privacyModeManager: PrivacyModeManager by lazy { PrivacyModeManagerImpl(calendarSettingsRepository, applicationScope) }

    val mainViewModel: MainViewModel by lazy {
        MainViewModel(
            this,
            transactionRepository,
            contactRepository,
            settingsRepository,
            calendarManager,
            calendarSettingsRepository
        )
    }

    val geminiPreferencesManager: GeminiPreferencesManager by lazy { GeminiPreferencesManager(this) }

    val geminiViewModel: GeminiViewModel by lazy { GeminiViewModel(geminiPreferencesManager) }

    override fun onCreate() {
        super.onCreate()
        Log.d("DebtApplication", "✅ Application onCreate called - starting initialization")
        
        try {
            // Database initialize et
            val db = AppDatabase.getDatabase(this)
            Log.d("DebtApplication", "✅ Database initialized successfully")
        } catch (e: Exception) {
            Log.e("DebtApplication", "❌ Database initialization failed: ${e.message}", e)
            e.printStackTrace()
            return  // Database hatasında çıkış yap, uygulamayı crash et
        }
        
        try {
            // Firebase Auth durumunu kontrol et ve senkronizasyonu başlat
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    Log.d("DebtApplication", "✅ User signed in: ${user.email}, starting Firebase sync")
                    // Kullanıcı giriş yaptıysa Firebase senkronizasyonunu background thread'de başlat
                    try {
                        applicationScope.launch {
                            try {
                                mainViewModel.initializeDataSync()
                                Log.d("DebtApplication", "✅ Data sync initialized (transactions + contacts)")
                            } catch (e: Exception) {
                                Log.e("DebtApplication", "❌ Senkronizasyon başlatılırken hata: ${e.message}", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("DebtApplication", "❌ Coroutine başlatılırken hata: ${e.message}", e)
                    }
                } else {
                    Log.d("DebtApplication", "⚠️ No user signed in, stopping Firebase sync")
                    // Kullanıcı çıkış yaptıysa senkronizasyonu durdur
                    try {
                        transactionRepository.stopListeningForChanges()
                        contactRepository.stopListeningForChanges()
                        Log.d("DebtApplication", "✅ Listeners stopped")
                    } catch (e: Exception) {
                        Log.e("DebtApplication", "❌ Listener durdurulamadı: ${e.message}", e)
                    }
                }
            }
            Log.d("DebtApplication", "✅ Firebase Auth listener registered")
        } catch (e: Exception) {
            Log.e("DebtApplication", "❌ Firebase Auth listener eklenirken hata: ${e.message}", e)
            e.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = prefs.getString("language_key", "tr") ?: "tr"
        super.attachBaseContext(LocaleHelper.setLocale(base, language))
    }
}