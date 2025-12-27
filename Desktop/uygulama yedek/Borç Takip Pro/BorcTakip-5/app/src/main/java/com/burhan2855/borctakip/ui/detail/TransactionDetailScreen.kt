package com.burhan2855.borctakip.ui.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Contact
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.formatDate
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.util.PdfUtils
import com.burhan2855.borctakip.data.FinancialTotals
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    contacts: List<Contact>,
    viewModel: MainViewModel,
    currencySymbol: String,
    onNavigateUp: () -> Unit,
    navController: androidx.navigation.NavController? = null
) {
    val transactionState by viewModel.getTransactionById(transactionId).collectAsState(initial = null)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorFlow.collectLatest { error ->
            error?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(id = R.string.islemi_sil)) },
            text = { Text(stringResource(id = R.string.emin_misiniz)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionState?.let { transaction ->
                            viewModel.delete(transaction)
                            showDeleteDialog = false
                            onNavigateUp()
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.evet))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(id = R.string.hayir))
                }
            }
        )
    }

    transactionState?.let { transaction ->
        var title by remember(transaction.id) { mutableStateOf(transaction.title) }
        var amount by remember(transaction.id) { mutableStateOf(transaction.amount.toString()) }
        var category by remember(transaction.id) { mutableStateOf(transaction.category) }
        var isDebt by remember(transaction.id) { mutableStateOf(transaction.isDebt) }
        var selectedContact by remember(transaction.id) { mutableStateOf(contacts.find { it.id == transaction.contactId?.toLong() }) }
        var expanded by remember { mutableStateOf(false) }

        // Use transaction.dueDate (fall back to transaction.date if dueDate is null)
        val dueInitial = transaction.dueDate ?: transaction.date ?: System.currentTimeMillis()
        val dueDateState = rememberDatePickerState(initialSelectedDateMillis = dueInitial)
        var showDueDatePicker by remember { mutableStateOf(false) }
        var selectedDueDate by remember(transaction.id) { mutableStateOf(transaction.dueDate ?: transaction.date) }

        val transactionDateState = rememberDatePickerState(initialSelectedDateMillis = transaction.transactionDate)
        var showTransactionDatePicker by remember { mutableStateOf(false) }
        var selectedTransactionDate by remember(transaction.id) { mutableStateOf(transaction.transactionDate) }

        var titleError by remember { mutableStateOf<String?>(null) }
        var amountError by remember { mutableStateOf<String?>(null) }

        fun validateFields(): Boolean {
            titleError = if (title.isBlank()) "Başlık boş olamaz" else null
            amountError = if (amount.isBlank() || amount.toDoubleOrNull() == null) "Geçerli bir tutar girin" else null
            return titleError == null && amountError == null
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.islem_detayi)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val financialTotals = FinancialTotals(0.0, 0.0, 0.0, 0.0, 0.0)
                            val pdfFile = PdfUtils.createTransactionListPdf(context, listOf(transaction), transaction.title, currencySymbol, financialTotals)
                            openPdf(context, pdfFile)
                        }) {
                            Icon(Icons.Default.Download, contentDescription = stringResource(id = R.string.pdf_indir))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
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

                // Person (Optional)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedContact?.name ?: "",
                        onValueChange = {},
                        label = { Text(stringResource(id = R.string.kisi_istege_bagli)) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
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

                // Category
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category ?: "",
                        onValueChange = { category = it },
                        label = { Text(stringResource(id = R.string.kategori)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transaction and Due Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("${stringResource(id = R.string.islem_tarihi)}: ${formatDate(selectedTransactionDate ?: System.currentTimeMillis())}")
                        Button(onClick = { showTransactionDatePicker = true }) {
                            Text(stringResource(id = R.string.islem_tarihini_degistir))
                        }
                    }
                    Column {
                        Text("${stringResource(id = R.string.vade_tarihi)}: ${formatDate(selectedDueDate ?: System.currentTimeMillis())}")
                        Button(onClick = { showDueDatePicker = true }) {
                            Text(stringResource(id = R.string.vade_tarihini_degistir))
                        }
                    }
                }

                if (showDueDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDueDatePicker = false },
                        confirmButton = { 
                            TextButton(onClick = { 
                                selectedDueDate = dueDateState.selectedDateMillis
                                showDueDatePicker = false 
                            }) { Text(stringResource(id = R.string.tamam)) }
                        },
                        dismissButton = { 
                            TextButton(onClick = { showDueDatePicker = false }) { Text(stringResource(id = R.string.iptal)) }
                        }
                    ) {
                        DatePicker(state = dueDateState)
                    }
                }

                if (showTransactionDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showTransactionDatePicker = false },
                        confirmButton = { 
                            TextButton(onClick = { 
                                selectedTransactionDate = transactionDateState.selectedDateMillis
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

                Spacer(modifier = Modifier.weight(1f))

                // Ödeme butonları - sadece ödenmemiş borçlar için
                if (isDebt && transaction.status != "Ödendi" && transaction.category != "Kasa Çıkışı" && transaction.category != "Banka Çıkışı") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                navController?.navigate("cashPayment/${transaction.id}?isCashIn=false")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Kasadan Öde", color = Color.White)
                        }
                        Button(
                            onClick = { 
                                navController?.navigate("bankPayment/${transaction.id}?isBankIn=false")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Bankadan Öde", color = Color.White)
                        }
                    }
                }

                // Tahsilat butonları - sadece tahsil edilmemiş alacaklar için
                if (!isDebt && transaction.status != "Ödendi" && transaction.category != "Kasa Girişi" && transaction.category != "Banka Girişi") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                navController?.navigate("cashPayment/${transaction.id}?isCashIn=true")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Kasadan Tahsil", color = Color.White)
                        }
                        Button(
                            onClick = { 
                                navController?.navigate("bankPayment/${transaction.id}?isBankIn=true")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Bankadan Tahsil", color = Color.White)
                        }
                    }
                }

                Button(
                    onClick = {
                        if (validateFields()) {
                            val updatedTransaction = transaction.copy(
                                title = title,
                                amount = amount.toDouble(),
                                category = category,
                                // Save due date into the dueDate field (was incorrectly saving into date)
                                dueDate = selectedDueDate,
                                // transactionDate is kept as before (or updated from picker)
                                transactionDate = selectedTransactionDate,
                                isDebt = isDebt,
                                contactId = selectedContact?.id?.toInt()
                            )
                            viewModel.update(updatedTransaction)
                            onNavigateUp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.guncelle))
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(id = R.string.sil))
                }
            }
        }
    }
}

private fun openPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "PDF görüntüleyici bulunamadı.", Toast.LENGTH_SHORT).show()
    }
}