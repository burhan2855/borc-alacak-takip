package com.burhan2855.borctakip.ui.payment

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.formatDate
import com.burhan2855.borctakip.ui.MainViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashPaymentScreen(
    transaction: Transaction?,
    isCashIn: Boolean,
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    if (transaction == null) {
        onNavigateUp()
        return
    }

    var paymentAmount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
    val kasaBalance by viewModel.kasaBalance.collectAsState()

    val title = if (isCashIn) "Kasadan Tahsilat" else "Kasadan Ödeme"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // İşlem Bilgisi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("İşlem: ${transaction.title}", style = MaterialTheme.typography.bodyMedium)
                    Text("Borç Tutarı: ₺${String.format("%.2f", transaction.amount)}", style = MaterialTheme.typography.bodySmall)
                    Text("Durum: ${transaction.status}", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Ödeme Tutarı Girişi
            OutlinedTextField(
                value = paymentAmount,
                onValueChange = {
                    paymentAmount = it
                    amountError = null
                },
                label = { Text("Tutar") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountError != null,
                supportingText = { amountError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text("₺") }
            )

            // İşlem Tarihi Seçimi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("İşlem Tarihi: ${formatDate(selectedDate)}", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { showDatePicker = true }) {
                    Text("Tarihi Değiştir")
                }
            }

            // Tarih Seçici Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDate = datePickerState.selectedDateMillis ?: selectedDate
                            showDatePicker = false
                        }) {
                            Text("Tamam")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("İptal")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Kaydet Butonu
            Button(
                onClick = {
                    val amount = paymentAmount.toDoubleOrNull()
                    when {
                        amount == null || paymentAmount.isEmpty() -> {
                            amountError = "Geçerli bir tutar girin"
                        }
                        amount <= 0 -> {
                            amountError = "Tutar 0'dan büyük olmalıdır"
                        }
                        !isCashIn && amount > kasaBalance -> {
                            amountError = "Kasa bakiyesi yetersiz (Mevcut: ₺${String.format("%.2f", kasaBalance)})"
                        }
                        else -> {
                            Log.d("DB_DUMP", "=== CASH PAYMENT START ===")
                            Log.d("DB_DUMP", "Transaction: ${transaction.title}")
                            Log.d("DB_DUMP", "Payment Amount: $amount")
                            Log.d("DB_DUMP", "Payment Source: Kasa")
                            
                            // Nakit akışı transaction'ı oluştur
                            // Borç ödeme = Kasa Çıkışı, Alacak tahsilat = Kasa Girişi
                            val cashFlowTransaction = Transaction(
                                title = if (isCashIn) "Tahsilat: ${transaction.title}" else "Ödeme: ${transaction.title}",
                                amount = amount.toDouble(),
                                date = selectedDate,
                                transactionDate = selectedDate,
                                isDebt = false,  // Kasa/Banka işlemleri isDebt=false
                                status = "Ödendi",
                                paymentType = "Kasa",
                                category = if (isCashIn) "Kasa Girişi" else "Kasa Çıkışı",
                                contactId = transaction.contactId
                            )
                            
                            coroutineScope.launch {
                                viewModel.insert(cashFlowTransaction)
                                
                                // Orijinal işlemi güncelle
                                val updatedAmount = transaction.amount - amount.toDouble()
                                val newStatus = if (updatedAmount <= 0) "Ödendi" else transaction.status
                                val updatedTransaction = transaction.copy(
                                    amount = maxOf(0.0, updatedAmount),
                                    status = newStatus
                                )
                                viewModel.update(updatedTransaction)
                                
                                Log.d("DB_DUMP", "=== CASH PAYMENT COMPLETED ===")
                                onNavigateUp()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Kaydet", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }

            // İptal Butonu
            OutlinedButton(
                onClick = onNavigateUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("İptal")
            }
        }
    }
}