package com.burhan2855.borctakip.gemini

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.util.CopilotService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopilotVoiceAssistantScreen(
    totalDebt: Double = 0.0,
    totalCredit: Double = 0.0,
    monthlyIncome: Double = 0.0,
    monthlyExpense: Double = 0.0
) {
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var copilotResponse by remember { mutableStateOf("Hoş geldiniz! Sesli komut verin veya soru sorun.") }
    var isProcessing by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val copilotService = remember { CopilotService(context) }

    LaunchedEffect(Unit) {
        return@LaunchedEffect
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            text = "🤖 Copilot Sesli Asistan",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Cevap Kartı
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isProcessing) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("İşleniyor...")
                } else {
                    Text(
                        text = copilotResponse,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Tanınan Metin
        if (recognizedText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Tanınan Komut:",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Mikrofon Butonu
        Button(
            onClick = {
                if (!isListening) {
                    isListening = true
                    recognizedText = ""
                    copilotService.startSpeechRecognition { text ->
                        recognizedText = text
                        isListening = false
                        isProcessing = true
                        
                        // Copilot'a soru sor
                        scope.launch {
                            val response = copilotService.askCopilot(text)
                            copilotResponse = response
                            isProcessing = false
                            copilotService.speakResponse(response)
                        }
                    }
                } else {
                    isListening = false
                }
            },
            modifier = Modifier
                .size(80.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isListening) "Durdur" else "Başlat",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Örnek Komutlar
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Örnek Komutlar:",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExampleCommand("\"Borç raporumu oluştur\"")
                ExampleCommand("\"Ödeme tavsiyesi ver\"")
                ExampleCommand("\"Bütçem nasıl?\"")
                ExampleCommand("\"Finansal durumumu analiz et\"")
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            copilotService.cleanup()
        }
    }
}

@Composable
fun ExampleCommand(command: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "•",
            modifier = Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = command,
            style = MaterialTheme.typography.bodySmall
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}
