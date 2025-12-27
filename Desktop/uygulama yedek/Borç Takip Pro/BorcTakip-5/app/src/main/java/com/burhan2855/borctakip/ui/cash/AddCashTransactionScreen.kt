package com.burhan2855.borctakip.ui.cash

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.formatDate
import com.burhan2855.borctakip.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCashTransactionScreen(
    isCashIn: Boolean,
    viewModel: MainViewModel,
    onSave: (Transaction) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    val transactionDateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showTransactionDatePicker by remember { mutableStateOf(false) }
    var selectedTransactionDate by remember { mutableStateOf(transactionDateState.selectedDateMillis ?: System.currentTimeMillis()) }

    val context = LocalContext.current
    val kasaBalance by viewModel.kasaBalance.collectAsState()

    // Collect errors from ViewModel and show as a Toast
    LaunchedEffect(Unit) {
        viewModel.errorFlow.collectLatest { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearError() // Clear the error after showing
            }
        }
    }

    fun validateFields(): Boolean {
        titleError = if (title.isBlank()) "Başlık boş olamaz" else null
        amountError = if (amount.isBlank() || amount.toDoubleOrNull() == null) "Geçerli bir tutar girin" else null
        
        // Kasa çıkışı kontrolü
        if (!isCashIn && amount.isNotBlank() && amountError == null) {
            val transactionAmount = amount.toDouble()
            if (transactionAmount > kasaBalance) {
                amountError = "Kasa bakiyesi yetersiz (Mevcut: ₺${String.format("%.2f", kasaBalance)})"
            }
        }
        
        return titleError == null && amountError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCashIn) stringResource(id = R.string.kasa_girisi) else stringResource(id = R.string.kasa_cikisi)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { 
                    title = it
                    titleError = null
                },
                label = { Text(stringResource(id = R.string.baslik)) },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it) } }
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    amount = it 
                    amountError = null
                },
                label = { Text(stringResource(id = R.string.tutar)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountError != null,
                supportingText = { amountError?.let { Text(it) } }
            )
            Column {
                Text("İşlem Tarihi: ${formatDate(selectedTransactionDate)}")
                Button(onClick = { showTransactionDatePicker = true }) {
                    Text("İşlem Tarihini Değiştir")
                }
            }
            if (showTransactionDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showTransactionDatePicker = false },
                    confirmButton = { 
                        TextButton(onClick = { 
                            selectedTransactionDate = transactionDateState.selectedDateMillis!!
                            showTransactionDatePicker = false 
                        }) { Text(stringResource(id = R.string.tamam)) }
                    },
                    dismissButton = { 
                        TextButton(onClick = { showTransactionDatePicker = false }) { Text(stringResource(id = R.string.iptal)) }
                    }
                ) {
                    DatePicker(state = transactionDateState)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.iptal))
                }
                Button(
                    onClick = {
                        if (validateFields()) {
                            onSave(
                                Transaction(
                                    title = title,
                                    amount = amount.toFloat().toDouble(),
                                    category = if (isCashIn) "Kasa Girişi" else "Kasa Çıkışı",
                                    date = selectedTransactionDate,
                                    transactionDate = selectedTransactionDate,
                                    isDebt = false, // Kasa işlemleri borç/alacak DEĞİLDİR
                                    paymentType = "Kasa"
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.kaydet))
                }
            }
        }
    }
}