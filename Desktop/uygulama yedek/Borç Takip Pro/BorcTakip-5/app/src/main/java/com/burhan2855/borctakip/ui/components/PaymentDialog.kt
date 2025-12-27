package com.burhan2855.borctakip.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel

@Composable
fun PaymentDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Float, String) -> Unit,
    viewModel: MainViewModel
) {
    var amountState by remember { mutableStateOf(TextFieldValue("")) }
    var selectedSource by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(text = "Ödeme Yöntemi Seçin") 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("İşlem: ${transaction.title}")
                Text("Tutar: ₺${String.format("%.2f", transaction.amount)}")
                Text("Seçilen Ödeme Yöntemi: ${selectedSource ?: "Seçilmedi"}", color = Color(0xFF6A1B9A))
                
                // Amount input field
                OutlinedTextField(
                    value = amountState,
                    onValueChange = { amountState = it },
                    label = { Text("Tutar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {
                        val amount = amountState.text.toFloatOrNull()
                        if (amount != null && amount > 0) {
                            onConfirm(amount, "Kasa")
                        }
                    }) {
                        Text("Kasa")
                    }
                    Button(onClick = {
                        val amount = amountState.text.toFloatOrNull()
                        if (amount != null && amount > 0) {
                            onConfirm(amount, "Banka")
                        }
                    }) {
                        Text("Banka")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.d("DB_DUMP", "PaymentDialog confirm clicked, amount=${transaction.amount}, selectedSource=$selectedSource")
                    if (selectedSource != null) {
                        Log.d("DB_DUMP", "Calling onConfirm with full amount: ${transaction.amount}")
                        onConfirm(transaction.amount.toFloat(), selectedSource!!)
                    }
                },
                enabled = selectedSource != null
            ) {
                Text(stringResource(R.string.onayla))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.iptal))
            }
        }
    )
}