package com.burhan2855.borctakip.ui.add

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.data.Contact
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    initialIsDebt: Boolean = true
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var category by remember { mutableStateOf("") }
    var isDebt by remember(initialIsDebt) { mutableStateOf(initialIsDebt) }
    var expanded by remember { mutableStateOf(false) }
    var installmentCount by remember { mutableStateOf("1") }
    var installmentExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Başlangıç tarihini SAATİ SIFIRLANARAK AL
    val initialDate = System.currentTimeMillis().let { millis ->
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    var selectedTransactionDate by remember { mutableStateOf(initialDate) }
    var selectedDueDate by remember { mutableStateOf(initialDate) }

    val transactionDateState = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    val dueDateState = rememberDatePickerState(initialSelectedDateMillis = initialDate)

    var showTransactionDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    val contacts by viewModel.allContacts.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = if (isDebt) "Borç Ekle" else "Alacak Ekle")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
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
            // Başlık
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Başlık") },
                modifier = Modifier.fillMaxWidth()
            )

            // Tutar
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Tutar") },
                modifier = Modifier.fillMaxWidth()
            )

            // İşlem Türü Seçimi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isDebt = true },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDebt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Borç", color = if (isDebt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { isDebt = false },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isDebt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Alacak", color = if (!isDebt) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Kişi Seçimi
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedContact?.name ?: "Kişi (İsteğe Bağlı)",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Seçim Yok") },
                        onClick = {
                            selectedContact = null
                            expanded = false
                        }
                    )
                    contacts.forEach { contact ->
                        DropdownMenuItem(
                            text = { Text(contact.name) },
                            onClick = {
                                selectedContact = contact
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Kategori
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Kategori") },
                modifier = Modifier.fillMaxWidth()
            )

            // Taksit Seçimi
            ExposedDropdownMenuBox(
                expanded = installmentExpanded,
                onExpandedChange = { installmentExpanded = !installmentExpanded }
            ) {
                OutlinedTextField(
                    value = installmentCount,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Taksit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = installmentExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = installmentExpanded,
                    onDismissRequest = { installmentExpanded = false }
                ) {
                    (1..12).forEach { count ->
                        DropdownMenuItem(
                            text = { Text("$count Taksit") },
                            onClick = {
                                installmentCount = count.toString()
                                installmentExpanded = false
                            }
                        )
                    }
                }
            }

            // İşlem Tarihi
            Button(
                onClick = { showTransactionDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("İşlem Tarihi: ${dateFormatter.format(Date(selectedTransactionDate))}")
            }

            // Vade Tarihi (İlk vade tarihi)
            Button(
                onClick = { showDueDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("İlk Vade Tarihi: ${dateFormatter.format(Date(selectedDueDate))}")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Kaydet Butonu
            Button(
                onClick = {
                    android.util.Log.d("ADD_TRANSACTION", "=== SAVE CLICKED === isDebt=$isDebt, title='$title', amount='$amount'")
                    
                    if (title.isNotBlank() && amount.isNotBlank()) {
                        val installments = installmentCount.toIntOrNull() ?: 1
                        val amountPerInstallment = (amount.toDoubleOrNull() ?: 0.0) / installments
                        
                        scope.launch {
                            android.util.Log.d("ADD_TRANSACTION", "=== INSTALLMENT CALCULATION ===")
                            android.util.Log.d("ADD_TRANSACTION", "selectedDueDate: ${dateFormatter.format(Date(selectedDueDate))}")
                            android.util.Log.d("ADD_TRANSACTION", "installments: $installments")
                            android.util.Log.d("ADD_TRANSACTION", "isDebt (before loop): $isDebt")
                            
                            // Taksitli işlemler oluştur - Basit ve güvenli yöntem
                            repeat(installments) { index ->
                                try {
                                    // Temel tarihi al
                                    val baseCal = Calendar.getInstance().apply {
                                        timeInMillis = selectedDueDate
                                    }
                                    val baseDay = baseCal.get(Calendar.DAY_OF_MONTH)
                                    val baseMonth = baseCal.get(Calendar.MONTH)  // 0-11
                                    val baseYear = baseCal.get(Calendar.YEAR)
                                    
                                    // Ay hesabı: 0-11 aralığında tut
                                    val totalMonths = baseMonth + index
                                    val newMonth = totalMonths % 12
                                    val yearOffset = totalMonths / 12
                                    val newYear = baseYear + yearOffset
                                    
                                    // Yeni Calendar oluştur
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, newYear)
                                        set(Calendar.MONTH, newMonth)
                                        set(Calendar.DAY_OF_MONTH, baseDay)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    val dueDate = newCal.timeInMillis
                                    
                                    android.util.Log.d("ADD_TRANSACTION", "Index=$index: base=$baseDay/${baseMonth+1}/$baseYear + $index months = $baseDay/${newMonth+1}/$newYear → ${dateFormatter.format(Date(dueDate))}")


                                    
                                    val transactionType = if (isDebt) "debt" else "credit"
                                    val transaction = Transaction(
                                        title = if (installments > 1) "$title (${index + 1}/$installments)" else title,
                                        amount = amountPerInstallment,
                                        contactId = selectedContact?.id?.toInt(),
                                        category = category.ifBlank { null },
                                        date = selectedTransactionDate,
                                        dueDate = dueDate,
                                        isDebt = isDebt,
                                        transactionDate = selectedTransactionDate,
                                        type = transactionType
                                    )
                                    android.util.Log.d("ADD_TRANSACTION", "Creating installment ${index + 1}/$installments with dueDate=$dueDate, formatted=${dateFormatter.format(Date(dueDate))}")
                                    viewModel.insert(transaction)
                                } catch (e: Exception) {
                                    android.util.Log.e("ADD_TRANSACTION", "Error creating installment $index: ${e.message}", e)
                                }
                            }
                            Toast.makeText(context, "İşlem kaydedildi", Toast.LENGTH_SHORT).show()
                            onNavigateUp()
                        }
                    } else {
                        Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kaydet")
            }

            // İptal Butonu
            OutlinedButton(
                onClick = onNavigateUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("İptal")
            }
        }
    }

    // İşlem Tarihi Picker
    if (showTransactionDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showTransactionDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionDateState.selectedDateMillis?.let { pickedMillis ->
                            // Picker'dan gelen saati sıfırla
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = pickedMillis
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            selectedTransactionDate = cal.timeInMillis
                        }
                        showTransactionDatePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransactionDatePicker = false }) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(state = transactionDateState)
        }
    }

    // Vade Tarihi Picker
    if (showDueDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDateState.selectedDateMillis?.let {
                            selectedDueDate = it
                        }
                        showDueDatePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(state = dueDateState)
        }
    }
}

