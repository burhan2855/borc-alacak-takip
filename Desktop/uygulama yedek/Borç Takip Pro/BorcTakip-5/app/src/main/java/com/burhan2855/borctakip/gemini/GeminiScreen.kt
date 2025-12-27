package com.burhan2855.borctakip.gemini

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiScreen(
    geminiViewModel: GeminiViewModel,
    currentApiKey: String,
    onNavigateUp: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val uiState by geminiViewModel.uiState.collectAsState()
    var isApiKeyValid by remember { mutableStateOf(currentApiKey.isNotBlank()) }

    LaunchedEffect(currentApiKey) {
        isApiKeyValid = currentApiKey.isNotBlank()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Gemini AI Asistanı") },
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
            },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Ayarlar")
                }
            }
        )

        // İçerik
        if (isApiKeyValid) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Soru gir
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Sorunuzu yazın...") },
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
                            geminiViewModel.generateContent(inputText)
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
                when (val state = uiState) {
                    is GeminiUiState.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = state.output,
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
                                text = state.errorMessage,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is GeminiUiState.Loading -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
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
                    GeminiUiState.Initial -> {
                        // Başlangıçta hiçbir şey gösterme
                    }
                }
            }
        } else {
            // API Key uyarısı
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "API Anahtarı Eksik",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gemini AI'ı kullanmak için ayarlarda API anahtarını girin.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onOpenSettings) {
                    Text("Ayarlara Git")
                }
            }
        }
    }
}
