package com.burhan2855.personeltakip.shared

import androidx.compose.foundation.layout.*
import androidx.compose.ui.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.burhan2855.personeltakip.shared.logic.IEmployeeService
import com.burhan2855.personeltakip.shared.ui.screens.LoginScreen
import com.burhan2855.personeltakip.shared.ui.screens.SharedEmployeeListScreen
import com.burhan2855.personeltakip.shared.ui.screens.SharedEmployeeDetailsScreen
import com.burhan2855.personeltakip.shared.util.ISettings

@Composable
fun App(
    settings: ISettings,
    employeeService: IEmployeeService, 
    onLoginSuccess: (() -> Unit)? = null
) {
    var isLoggedIn by remember { mutableStateOf(false) }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    MaterialTheme {
        if (!isLoggedIn) {
            LoginScreen(
                settings = settings,
                onLoginSuccess = {
                    isLoggedIn = true
                    onLoginSuccess?.invoke()
                }
            )
        } else {
            when (val screen = currentScreen) {
                is Screen.List -> {
                    SharedEmployeeListScreen(
                        employeeService = employeeService,
                        onAddEmployee = { },
                        onEmployeeClick = { id -> 
                            currentScreen = Screen.Details(id)
                        }
                    )
                }
                is Screen.Details -> {
                    SharedEmployeeDetailsScreen(
                        employeeId = screen.id,
                        employeeService = employeeService,
                        onNavigateBack = {
                            currentScreen = Screen.List
                        }
                    )
                }
            }
        }
    }
}

sealed class Screen {
    object List : Screen()
    data class Details(val id: Int) : Screen()
}
