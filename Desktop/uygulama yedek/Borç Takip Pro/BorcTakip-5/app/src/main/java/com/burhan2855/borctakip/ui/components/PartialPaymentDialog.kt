package com.burhan2855.borctakip.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PartialPaymentDialog(
    isVisible: Boolean,
    totalAmount: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    if (isVisible) {
        var paymentAmount by remember { mutableStateOf("") }
        var paymentError by remember { mutableStateOf<String?>(null) }
        
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Başlık
                    Text(
                        text = "Kısmi Ödeme",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Toplam tutar bilgisi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Toplam Borç",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6A1B9A)
                            )
                            Text(
                                text = "${String.format("%.2f", totalAmount)} $currencySymbol",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Ödeme tutarı girişi
                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { 
                            paymentAmount = it
                            paymentError = null
                        },
                        label = { Text("Ödeme Tutarı") },
                        placeholder = { Text("Örn: 500.00") },
                        suffix = { Text(currencySymbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = paymentError != null,
                        supportingText = { 
                            paymentError?.let { 
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Butonlar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6A1B9A)
                            )
                        ) {
                            Text("İptal")
                        }
                        
                        Button(
                            onClick = {
                                val amount = paymentAmount.toDoubleOrNull()
                                when {
                                    amount == null -> {
                                        paymentError = "Geçerli bir tutar girin"
                                    }
                                    amount <= 0 -> {
                                        paymentError = "Tutar 0'dan büyük olmalıdır"
                                    }
                                    amount >= totalAmount -> {
                                        paymentError = "Kısmi ödeme toplam tutardan küçük olmalıdır"
                                    }
                                    else -> {
                                        onConfirm(amount)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Öde", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}