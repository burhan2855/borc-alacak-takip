package com.burhan2855.borctakip

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.burhan2855.borctakip.ui.theme.BorcTakipTheme
import com.google.firebase.auth.FirebaseAuth
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.ui.add.AddTransactionScreen
import com.burhan2855.borctakip.ui.bank.AddBankTransactionScreen
import com.burhan2855.borctakip.ui.bank.BankScreen
import com.burhan2855.borctakip.ui.calendar.CalendarSettingsScreen
import com.burhan2855.borctakip.ui.calendar.CalendarViewScreen
import com.burhan2855.borctakip.ui.cash.AddCashTransactionScreen
import com.burhan2855.borctakip.ui.cash.CashScreen
import com.burhan2855.borctakip.ui.contacts.AddContactScreen
import com.burhan2855.borctakip.ui.contacts.AllContactTransactionsScreen
import com.burhan2855.borctakip.ui.contacts.ContactSelectionScreen
import com.burhan2855.borctakip.ui.contacts.ContactTransactionsScreen
import com.burhan2855.borctakip.ui.contacts.ContactsScreen
import com.burhan2855.borctakip.ui.detail.TransactionDetailScreen
import com.burhan2855.borctakip.ui.DebtTrackerApp
import com.burhan2855.borctakip.ui.installment.AddInstallmentScreen
import com.burhan2855.borctakip.ui.auth.LoginScreen
import com.burhan2855.borctakip.ui.reports.MonthlyDetailScreen
import com.burhan2855.borctakip.ui.reports.ReportScreen
import com.burhan2855.borctakip.ui.settings.CurrencySelectionScreen
import com.burhan2855.borctakip.ui.settings.LanguageSelectionScreen
import com.burhan2855.borctakip.ui.settings.SettingsScreen
import com.burhan2855.borctakip.ui.transactions.AllTransactionsScreen
import com.burhan2855.borctakip.ui.transactions.DebtTransactionsScreen
import com.burhan2855.borctakip.ui.transactions.CreditTransactionsScreen
import com.burhan2855.borctakip.ui.payment.CashPaymentScreen
import com.burhan2855.borctakip.ui.payment.BankPaymentScreen
import com.burhan2855.borctakip.ui.upcoming.UpcomingPaymentsScreen
import com.burhan2855.borctakip.gemini.GeminiScreen
import com.burhan2855.borctakip.gemini.GeminiSettingsScreen
import com.burhan2855.borctakip.gemini.GeminiViewModel
import com.burhan2855.borctakip.gemini.GeminiPreferencesManager
import com.burhan2855.borctakip.gemini.CopilotSettingsScreen
import com.burhan2855.borctakip.gemini.CopilotVoiceAssistantScreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    // İzin talep launcher'ı - explicit type belirtildi
    private val requestCalendarPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val readGranted = permissions[android.Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[android.Manifest.permission.WRITE_CALENDAR] ?: false
        
        if (readGranted && writeGranted) {
            android.util.Log.d("DB_DUMP", "✅ Calendar permissions GRANTED")
        } else {
            android.util.Log.w("DB_DUMP", "⚠️ Calendar permissions DENIED - Device calendar sync will not work")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // mainViewModel'i güvenli şekilde al
        val mainViewModel: MainViewModel = try {
            (application as DebtApplication).mainViewModel
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "ViewModel initialization failed: ${e.message}", e)
            throw e  // Exception'ı yukarıya fırlat
        }

        val geminiViewModel = (application as DebtApplication).geminiViewModel
        val geminiPreferencesManager = (application as DebtApplication).geminiPreferencesManager
        
        // Check if user is already signed in and trigger sync if needed
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                android.util.Log.d("MainActivity", "User already signed in on startup: ${currentUser.email} - ensuring data sync")
                // DebtApplication's auth listener will also trigger, but this ensures immediate sync
                mainViewModel.initializeDataSync()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error checking auth state on startup: ${e.message}", e)
        }
        
        setContent {
            BorcTakipTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Firebase Auth'u güvenli şekilde kontrol et
                    val startDestination = try {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser != null) "main" else "login"
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Firebase Auth error: ${e.message}", e)
                        "login"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                                },
                                onNavigateToSignUp = {
                                    navController.navigate("signup")
                                }
                            )
                        }
                        composable("signup") {
                            com.burhan2855.borctakip.ui.auth.SignUpScreen(
                                onSignUpSuccess = {
                                    // Ensure data sync starts immediately after signup
                                    android.util.Log.d("MainActivity", "Signup success - triggering data sync")
                                    mainViewModel.initializeDataSync()
                                    navController.navigate("main") { 
                                        popUpTo("login") { inclusive = true } 
                                    }
                                },
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }
                        composable("main") {
                            val transactions by mainViewModel.allTransactions.collectAsState()
                            val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                            DebtTrackerApp(
                                transactions = transactions,
                                currencySymbol = currencySymbol,
                                navController = navController,
                                viewModel = mainViewModel
                            )
                        }
                        composable("gemini_screen") {
                            val apiKey by geminiPreferencesManager.apiKeyFlow.collectAsState(initial = "")
                            GeminiScreen(
                                geminiViewModel = geminiViewModel,
                                currentApiKey = apiKey,
                                onNavigateUp = { navController.navigateUp() },
                                onOpenSettings = { navController.navigate("gemini_settings") }
                            )
                        }
                        composable("gemini_settings") {
                            val apiKey by geminiPreferencesManager.apiKeyFlow.collectAsState(initial = "")
                            GeminiSettingsScreen(
                                geminiViewModel = geminiViewModel,
                                geminiPreferencesManager = geminiPreferencesManager,
                                currentApiKey = apiKey,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("add/{isDebt}", arguments = listOf(navArgument("isDebt") { type = NavType.StringType })) { backStackEntry ->
                            val isDebtString = backStackEntry.arguments?.getString("isDebt") ?: "true"
                            val isDebt = isDebtString.equals("true", ignoreCase = true)
                            android.util.Log.d("NAV_DEBUG", "isDebtString=$isDebtString, isDebt=$isDebt")
                            AddTransactionScreen(
                                viewModel = mainViewModel,
                                onNavigateUp = { navController.navigateUp() },
                                initialIsDebt = isDebt
                            )
                        }
                        composable("add_installment") {
                            val contacts by mainViewModel.allContacts.collectAsState()
                            AddInstallmentScreen(
                                contacts = contacts,
                                onSave = { transactions ->
                                    transactions.forEach { mainViewModel.insert(it) }
                                    navController.navigateUp()
                                },
                                onCancel = { navController.navigateUp() }
                            )
                        }
                        composable("add_cash_transaction/{isCashIn}", arguments = listOf(navArgument("isCashIn") { type = NavType.BoolType })) { backStackEntry ->
                            val isCashIn = backStackEntry.arguments?.getBoolean("isCashIn") ?: true
                            AddCashTransactionScreen(
                                isCashIn = isCashIn,
                                viewModel = mainViewModel,
                                onSave = {
                                    mainViewModel.insert(it)
                                    navController.navigateUp()
                                },
                                onCancel = { navController.navigateUp() }
                            )
                        }
                        composable("add_bank_transaction/{isBankIn}", arguments = listOf(navArgument("isBankIn") { type = NavType.BoolType })) { backStackEntry ->
                            val isBankIn = backStackEntry.arguments?.getBoolean("isBankIn") ?: true
                            AddBankTransactionScreen(
                                isBankIn = isBankIn,
                                viewModel = mainViewModel,
                                onSave = {
                                    mainViewModel.insert(it)
                                    navController.navigateUp()
                                },
                                onCancel = { navController.navigateUp() }
                            )
                        }
                        composable(
                            "detail/{transactionId}",
                            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val transactionId = backStackEntry.arguments?.getLong("transactionId")
                            transactionId?.let {
                                val contacts by mainViewModel.allContacts.collectAsState()
                                val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                                TransactionDetailScreen(
                                    transactionId = it,
                                    contacts = contacts,
                                    viewModel = mainViewModel,
                                    currencySymbol = currencySymbol,
                                    onNavigateUp = { navController.navigateUp() },
                                    navController = navController
                                )
                            }
                        }
                        composable("contacts") {
                            val transactions by mainViewModel.allTransactions.collectAsState()
                            val contacts by mainViewModel.allContacts.collectAsState()
                            ContactsScreen(
                                transactions = transactions,
                                contacts = contacts,
                                onNavigateUp = { navController.navigateUp() },
                                onNavigateToContactDetail = { transactionId ->
                                    navController.navigate("detail/$transactionId")
                                },
                                viewModel = mainViewModel
                            )
                        }
                        composable("add_contact") {
                            AddContactScreen(
                                onSave = { contact ->
                                    mainViewModel.insertContact(contact)
                                    navController.navigateUp()
                                },
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("cash_screen") {
                            val transactions by mainViewModel.allTransactions.collectAsState()
                            val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                            CashScreen(
                                transactions = transactions.filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }.sortedBy { it.dueDate ?: it.date },
                                navController = navController,
                                currencySymbol = currencySymbol,
                                viewModel = mainViewModel
                            )
                        }
                        composable("bank_screen") {
                            val transactions by mainViewModel.allTransactions.collectAsState()
                            val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                            BankScreen(
                                transactions = transactions.filter { it.category == "Banka Girişi" || it.category == "Banka Çıkışı" }.sortedBy { it.dueDate ?: it.date },
                                navController = navController,
                                currencySymbol = currencySymbol,
                                viewModel = mainViewModel
                            )
                        }
                        composable("all_transactions_screen") {
                            AllTransactionsScreen(
                                navController = navController,
                                viewModel = mainViewModel,
                                onTransactionClick = { transaction ->
                                    navController.navigate("detail/${transaction.id}")
                                },
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("debt_transactions_screen") {
                            DebtTransactionsScreen(
                                navController = navController,
                                viewModel = mainViewModel,
                                onTransactionClick = { transaction ->
                                    navController.navigate("detail/${transaction.id}")
                                }
                            )
                        }
                        composable("credit_transactions_screen") {
                            CreditTransactionsScreen(
                                navController = navController,
                                viewModel = mainViewModel,
                                onTransactionClick = { transaction ->
                                    navController.navigate("detail/${transaction.id}")
                                }
                            )
                        }
                        composable("calendar_screen") {
                            CalendarViewScreen(
                                mainViewModel = mainViewModel,
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable("report_screen") {
                            val transactions by mainViewModel.allTransactions.collectAsState()
                            val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                            val contacts by mainViewModel.allContacts.collectAsState()
                            ReportScreen(
                                transactions = transactions,
                                currencySymbol = currencySymbol,
                                contacts = contacts,
                                navController = navController,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable(
                            "monthly_detail_screen/{year}/{month}",
                            arguments = listOf(
                                navArgument("year") { type = NavType.IntType },
                                navArgument("month") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val year = backStackEntry.arguments?.getInt("year")
                            val monthInt = backStackEntry.arguments?.getInt("month")
                            if (year != null && monthInt != null) {
                                val transactions by mainViewModel.allTransactions.collectAsState()
                                val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                                MonthlyDetailScreen(
                                    year = year,
                                    month = monthInt,
                                    transactions = transactions,
                                    currencySymbol = currencySymbol,
                                    onNavigateUp = { navController.navigateUp() }
                                )
                            }
                        }
                        composable("settings_screen") {
                            val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                            val language by mainViewModel.language.collectAsState()
                            SettingsScreen(
                                selectedCurrencyCode = currencySymbol,
                                selectedLanguage = language,
                                onNavigateToCurrencySelection = { navController.navigate("currency_selection_screen") },
                                onNavigateToLanguageSelection = { navController.navigate("language_selection_screen") },
                                onNavigateToCalendarSettings = { navController.navigate("calendar_settings_screen") },
                                onNavigateToCopilotSettings = { navController.navigate("copilot_settings_screen") },
                                onNavigateToGeminiSettings = { navController.navigate("gemini_settings_screen") },
                                onNavigateUp = { navController.navigateUp() },
                                onSignOut = {
                                    FirebaseAuth.getInstance().signOut()
                                    mainViewModel.onSignOut()
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("currency_selection_screen") {
                            CurrencySelectionScreen(
                                onCurrencyChange = { code, symbol -> 
                                    mainViewModel.setCurrency(code, symbol)
                                },
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("language_selection_screen") {
                            val language by mainViewModel.language.collectAsState()
                            LanguageSelectionScreen(
                                onLanguageChange = { 
                                    mainViewModel.setLanguage(it).invokeOnCompletion { recreate() }
                                },
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("calendar_settings_screen") {
                            CalendarSettingsScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable("copilot_settings_screen") {
                            CopilotSettingsScreen(
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("gemini_settings_screen") {
                            val currentApiKey by geminiPreferencesManager.apiKey.collectAsState("")
                            GeminiSettingsScreen(
                                geminiViewModel = geminiViewModel,
                                geminiPreferencesManager = geminiPreferencesManager,
                                currentApiKey = currentApiKey,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable(
                            "contact_selection_screen",
                        ) {
                            val contacts by mainViewModel.allContacts.collectAsState()
                            ContactSelectionScreen(
                                contacts = contacts,
                                navController = navController,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable(
                            "contact_transactions_screen/{contactId}",
                            arguments = listOf(navArgument("contactId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val contactId = backStackEntry.arguments?.getLong("contactId")
                            contactId?.let {
                                ContactTransactionsScreen(
                                    contactId = it,
                                    viewModel = mainViewModel,
                                    navController = navController,
                                    onNavigateUp = { navController.navigateUp() }
                                )
                            }
                        }
                        composable("all_contact_transactions_screen") {
                            val transactions by mainViewModel.allTransactions.collectAsState()
                            val currencySymbol by mainViewModel.currencySymbol.collectAsState()
                            AllContactTransactionsScreen(
                                transactions = transactions,
                                currencySymbol = currencySymbol,
                                navController = navController,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable("upcoming_payments_screen") {
                            UpcomingPaymentsScreen(
                                viewModel = mainViewModel,
                                navController = navController,
                                onTransactionClick = { transaction ->
                                    navController.navigate("detail/${transaction.id}")
                                }
                            )
                        }
                        composable(
                            "cashPayment/{transactionId}?isCashIn={isCashIn}",
                            arguments = listOf(
                                navArgument("transactionId") { type = NavType.LongType },
                                navArgument("isCashIn") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val transactionId = backStackEntry.arguments?.getLong("transactionId")
                            val isCashIn = backStackEntry.arguments?.getBoolean("isCashIn") ?: false
                            transactionId?.let {
                                val transactions by mainViewModel.allTransactions.collectAsState()
                                val transaction = transactions.find { t -> t.id == it }
                                CashPaymentScreen(
                                    transaction = transaction,
                                    isCashIn = isCashIn,
                                    viewModel = mainViewModel,
                                    onNavigateUp = { navController.navigateUp() }
                                )
                            }
                        }
                        composable(
                            "bankPayment/{transactionId}?isBankIn={isBankIn}",
                            arguments = listOf(
                                navArgument("transactionId") { type = NavType.LongType },
                                navArgument("isBankIn") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val transactionId = backStackEntry.arguments?.getLong("transactionId")
                            val isBankIn = backStackEntry.arguments?.getBoolean("isBankIn") ?: false
                            transactionId?.let {
                                val transactions by mainViewModel.allTransactions.collectAsState()
                                val transaction = transactions.find { t -> t.id == it }
                                BankPaymentScreen(
                                    transaction = transaction,
                                    isBankIn = isBankIn,
                                    viewModel = mainViewModel,
                                    onNavigateUp = { navController.navigateUp() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
