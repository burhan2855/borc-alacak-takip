package com.burhan2855.borctakip.ui.installment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Contact
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.formatDate
import com.burhan2855.borctakip.ui.MainViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstallmentScreen(
    contacts: List<Contact>,
    onSave: (List<Transaction>) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var installmentCount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    val firstInstallmentDateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedFirstInstallmentDate by remember { mutableStateOf(firstInstallmentDateState.selectedDateMillis ?: System.currentTimeMillis()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.taksitli_islem_ekle)) },
                actions = {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(id = R.string.iptal))
                    }
                }
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
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(id = R.string.baslik)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = totalAmount, onValueChange = { totalAmount = it }, label = { Text(stringResource(id = R.string.toplam_tutar)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = installmentCount, onValueChange = { installmentCount = it }, label = { Text(stringResource(id = R.string.taksit_sayisi)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Text("${stringResource(id = R.string.ilk_taksit_tarihini_sec)}: ${formatDate(selectedFirstInstallmentDate)}")
            Button(onClick = { showDatePicker = true }) {
                Text(stringResource(id = R.string.tarihi_degistir))
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = { 
                        TextButton(onClick = { 
                            // Save the confirmed date into selectedFirstInstallmentDate
                            firstInstallmentDateState.selectedDateMillis?.let { selectedFirstInstallmentDate = it }
                            showDatePicker = false 
                        }) { Text(stringResource(id = R.string.tamam)) }
                    },
                    dismissButton = { 
                        TextButton(onClick = { showDatePicker = false }) { Text(stringResource(id = R.string.iptal)) }
                    }
                ) {
                    DatePicker(state = firstInstallmentDateState)
                }
            }

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(value = selectedContact?.name ?: "", onValueChange = {}, label = { Text(stringResource(id = R.string.kisi_istege_bagli)) }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    contacts.forEach {
                        DropdownMenuItem(text = { Text(it.name) }, onClick = { 
                            selectedContact = it
                            expanded = false
                        })
                    }
                }
            }
            
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(id = R.string.kategori)) }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val totalAmountDouble = totalAmount.toDoubleOrNull()
                    val count = installmentCount.toIntOrNull()
                    // Use the confirmed selectedFirstInstallmentDate instead of reading the state directly
                    val firstDate = selectedFirstInstallmentDate

                    if (title.isBlank() || totalAmountDouble == null || totalAmountDouble <= 0.0 || count == null || count <= 0 || firstDate == null) {
                        return@Button
                    }

                    val installmentAmount = totalAmountDouble / count
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = firstDate

                    val generated = mutableListOf<Transaction>()

                    for (i in 1..count) {
                        val transactionDate = calendar.timeInMillis
                        val transaction = Transaction(
                            id = 0, // Auto-generated
                            contactId = selectedContact?.id?.toInt(),
                            amount = installmentAmount,
                            type = "debt",
                            description = "",
                            date = System.currentTimeMillis(),
                            dueDate = transactionDate,
                            isPaid = false,
                            isSynced = false,
                            category = category,
                            title = "$title ${i}/$count",
                            isDebt = true,
                            status = "Ödenmedi",
                            remainingAmount = installmentAmount,
                            documentId = null,
                            paymentType = "",
                            transactionDate = System.currentTimeMillis()
                        )
                        generated.add(transaction)
                        calendar.add(Calendar.MONTH, 1)
                    }

                    // Call onSave once with the full list of generated installments
                    onSave(generated)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.taksitleri_olustur))
            }
        }
    }
}