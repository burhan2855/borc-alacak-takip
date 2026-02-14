package com.burhan2855.personeltakip.shared

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.burhan2855.personeltakip.shared.App
import com.burhan2855.personeltakip.shared.adapters.WebSettings
import com.burhan2855.personeltakip.shared.adapters.WebEmployeeService

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App(
            settings = WebSettings(),
            employeeService = WebEmployeeService(),
            onLoginSuccess = { 
                // Navigate to Main Screen (TODO)
             }
        )
    }
}
