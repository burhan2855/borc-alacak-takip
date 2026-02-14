package com.burhan2855.personeltakip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.burhan2855.personeltakip.data.AppDatabase
import com.burhan2855.personeltakip.data.EmployeeRepository
import com.burhan2855.personeltakip.ui.EmployeeViewModel
import com.burhan2855.personeltakip.ui.EmployeeViewModelFactory
import com.burhan2855.personeltakip.ui.theme.PersonelTakipTheme
import com.burhan2855.personeltakip.ui.screens.*

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "personel_takip_db"
        )
        .addMigrations(AppDatabase.MIGRATION_8_9)
        .fallbackToDestructiveMigration() // For development version 2 migration
        .build()

        val repository = EmployeeRepository(
            database.employeeDao(), 
            database.workLogDao(),
            database.adjustmentDao()
        )
        val factory = EmployeeViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            val settings = com.burhan2855.personeltakip.shared.adapters.AndroidSettings(com.burhan2855.personeltakip.util.PreferenceManager(applicationContext))
            val employeeService = com.burhan2855.personeltakip.shared.adapters.AndroidEmployeeService(repository)
            
            var isLoggedIn by remember { mutableStateOf(false) }
            
            if (!isLoggedIn) {
                com.burhan2855.personeltakip.shared.App(
                    settings = settings,
                    employeeService = employeeService,
                    onLoginSuccess = { isLoggedIn = true }
                )
            } else {
                PersonelTakipTheme {
                    val navController = rememberNavController()
                    val viewModel: EmployeeViewModel = viewModel(factory = factory)

                    Scaffold { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "employee_list", // Skip login, we just did it
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // ... existing composables ...
                            composable("employee_list") {
                                EmployeeListScreen(
                                    viewModel = viewModel,
                                    onAddEmployee = { navController.navigate("add_employee") },
                                    onEmployeeClick = { id -> navController.navigate("employee_details/$id") }
                                )
                            }
                            composable("add_employee") {
                                AddEmployeeScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                "employee_details/{employeeId}",
                                arguments = listOf(
                                    androidx.navigation.navArgument("employeeId") { type = androidx.navigation.NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val employeeId = backStackEntry.arguments?.getInt("employeeId")
                                if (employeeId != null) {
                                    EmployeeDetailsScreen(
                                        employeeId = employeeId,
                                        viewModel = viewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
