package com.burhan2855.borctakip.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burhan2855.borctakip.gemini.GeminiViewModel
import com.burhan2855.borctakip.gemini.GeminiUiState

/**
 * Gemini AI ile etkileşim kurmak için örnek Compose ekranı
 */
@Composable
fun GeminiAIScreen(
    viewModel: GeminiViewModel = viewModel()
) {
    var inputText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Gemini AI Asistanı",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Soru gir
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Sorunuzu yazın") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            enabled = uiState !is GeminiUiState.Loading,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Gönder butonu
        Button(
            onClick = { 
                if (inputText.isNotBlank()) {
                    viewModel.generateContent(inputText)
                    inputText = ""
                }
            },
            enabled = uiState !is GeminiUiState.Loading && inputText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is GeminiUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yanıt Alınıyor...")
            } else {
                Text("Yanıt Al")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Yanıt göster
        when (uiState) {
            is GeminiUiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as GeminiUiState.Success).output,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is GeminiUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as GeminiUiState.Error).errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            is GeminiUiState.Loading -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Yanıt hazırlanıyor...")
                    }
                }
            }
            is GeminiUiState.Initial -> {
                // Başlangıçta hiçbir şey gösterme
            }
        }
    }
}
