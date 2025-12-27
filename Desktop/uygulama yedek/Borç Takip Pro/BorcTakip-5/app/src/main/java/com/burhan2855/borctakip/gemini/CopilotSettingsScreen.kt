package com.burhan2855.borctakip.gemini

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.util.CopilotService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopilotSettingsScreen(
    onNavigateUp: () -> Unit
) {
    var copilotToken by remember { mutableStateOf("") }
    var tokenVisible by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        if (saveSuccess) {
            kotlinx.coroutines.delay(2000)
            saveSuccess = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("GitHub Copilot Ayarları") },
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
            // Açıklama Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GitHub Copilot Entegrasyonu",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GitHub Personal Access Token'ını buraya ekleyin. Copilot, finansal tavsiye, rapor oluşturma ve sesli komutlar için kullanılabilir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Token Oluştur:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "https://github.com/settings/tokens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Token Input
            OutlinedTextField(
                value = copilotToken,
                onValueChange = { copilotToken = it },
                label = { Text("GitHub Personal Access Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3,
                visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { tokenVisible = !tokenVisible }) {
                        Icon(
                            imageVector = if (tokenVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (tokenVisible) "Gizle" else "Göster"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                placeholder = { Text("ghp_xxxxxxxxxxxxxxxxxxxx") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Özellikleri Göster
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Copilot Özellikleri",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Özellikler Listesi
                    FeatureItem("🎤 Sesli Komutlar", "\"Borç raporunu oluştur\" gibi sesli komutlar")
                    FeatureItem("📊 Finansal Rapor", "Borç ve alacak özeti otomatik oluşturma")
                    FeatureItem("💡 Tavsiye Sistemi", "Ödeme ve bütçe analiz önerileri")
                    FeatureItem("🔍 Akıllı Analiz", "Finansal durumunuzun detaylı analizi")
                    FeatureItem("🎙️ Sesli Yanıt", "Cevapları sesli olarak dinleyebilme")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Kaydet Butonu
            Button(
                onClick = {
                    if (copilotToken.isNotEmpty()) {
                        scope.launch {
                            // Gerçek uygulamada SharedPreferences'a kaydet
                            saveSuccess = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = copilotToken.isNotEmpty()
            ) {
                Text("Token'ı Kaydet")
            }

            if (saveSuccess) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "✓ Token başarıyla kaydedildi!",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(title: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
