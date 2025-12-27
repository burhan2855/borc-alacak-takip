package com.burhan2855.borctakip.gemini

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.util.GeminiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSettingsScreen(
    geminiViewModel: GeminiViewModel,
    geminiPreferencesManager: GeminiPreferencesManager,
    currentApiKey: String,
    onNavigateUp: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (saveSuccess) {
            kotlinx.coroutines.delay(2000)
            saveSuccess = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Gemini Ayarları") },
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
            }
        )

        // İçerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gemini API Anahtarı",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Google AI Studio'dan aldığınız API anahtarını buraya yapıştırın. " +
                                "AI Studio'ya erişmek için: https://aistudio.google.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // API Key Input
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("API Anahtarı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4,
                visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                        Icon(
                            imageVector = if (apiKeyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (apiKeyVisible) "Gizle" else "Göster"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kaydet butonu
            Button(
                onClick = {
                    if (apiKeyInput.isNotBlank()) {
                        scope.launch {
                            geminiPreferencesManager.saveApiKey(apiKeyInput)
                            GeminiService.initialize(apiKeyInput)
                            saveSuccess = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = apiKeyInput.isNotBlank() && apiKeyInput != currentApiKey
            ) {
                Text("Kaydet")
            }

            if (saveSuccess) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "✓ API anahtarı başarıyla kaydedildi",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bilgi kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "API Anahtarını Nasıl Alırım?",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. https://aistudio.google.com adresine gidin\n" +
                                "2. Google hesabınızla giriş yapın\n" +
                                "3. 'API key' düğmesine tıklayın\n" +
                                "4. 'Create new API key' seçin\n" +
                                "5. Oluşturulan anahtarı kopyalayıp buraya yapıştırın",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ Güvenlik Uyarısı",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "API anahtarınızı kimseyle paylaşmayın! " +
                                "Başkası tarafından kötüye kullanılabilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
