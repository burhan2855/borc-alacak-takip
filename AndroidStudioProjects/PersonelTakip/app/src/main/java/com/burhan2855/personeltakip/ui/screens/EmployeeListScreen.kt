package com.burhan2855.personeltakip.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.burhan2855.personeltakip.data.Employee
import com.burhan2855.personeltakip.data.WorkLog
import com.burhan2855.personeltakip.data.Adjustment
import com.burhan2855.personeltakip.ui.EmployeeViewModel
import com.burhan2855.personeltakip.util.ReportGenerator
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.burhan2855.personeltakip.data.AdjustmentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    viewModel: EmployeeViewModel,
    onAddEmployee: () -> Unit,
    onEmployeeClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val employees by viewModel.allEmployees.collectAsState(initial = emptyList())
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    
    val backupManager = remember { com.burhan2855.personeltakip.util.BackupManager(context) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let {
                if (backupManager.exportDatabase(it)) {
                    Toast.makeText(context, "Yedekleme başarılı", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Yedekleme başarısız", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                if (backupManager.importDatabase(it)) {
                    Toast.makeText(context, "Yedek geri yüklendi. Uygulamayı yeniden başlatın.", Toast.LENGTH_LONG).show()
                    // Ideally we should restart the app or force reload VM
                } else {
                    Toast.makeText(context, "Geri yükleme başarısız", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) selectedImageUri = uri
    }
    var showEditDialog by remember { mutableStateOf(false) }

    var summaryStartDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var summaryEndDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())) }
    var showSummaryRangePicker by remember { mutableStateOf(false) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personel Listesi") },
                actions = {
                    IconButton(onClick = { showSummaryRangePicker = true }) {
                        Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    var showPdfMenu by remember { mutableStateOf(false) }
                    var showExcelMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { showPdfMenu = true }) {
                            Icon(Icons.Default.Summarize, null, tint = Color(0xFFE91E63))
                        }
                        DropdownMenu(expanded = showPdfMenu, onDismissRequest = { showPdfMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("PDF Görüntüle") },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                                onClick = {
                                    showPdfMenu = false
                                    scope.launch {
                                        val allData = generateSummaryData(viewModel, employees, summaryStartDate, summaryEndDate)
                                        if (allData.isNotEmpty()) ReportGenerator.openSummaryPdf(context, allData, summaryStartDate, summaryEndDate)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("PDF Paylaş") },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                onClick = {
                                    showPdfMenu = false
                                    scope.launch {
                                        val allData = generateSummaryData(viewModel, employees, summaryStartDate, summaryEndDate)
                                        if (allData.isNotEmpty()) ReportGenerator.openSummaryPdf(context, allData, summaryStartDate, summaryEndDate, isShare = true)
                                    }
                                }
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showExcelMenu = true }) {
                            Icon(Icons.Default.FileDownload, null, tint = Color(0xFF4CAF50))
                        }
                        DropdownMenu(expanded = showExcelMenu, onDismissRequest = { showExcelMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Excel Görüntüle") },
                                leadingIcon = { Icon(Icons.Default.TableChart, null) },
                                onClick = {
                                    showExcelMenu = false
                                    scope.launch {
                                        val allData = generateSummaryData(viewModel, employees, summaryStartDate, summaryEndDate)
                                        if (allData.isNotEmpty()) ReportGenerator.openSummaryExcel(context, allData, summaryStartDate, summaryEndDate)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Excel Paylaş") },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                onClick = {
                                    showExcelMenu = false
                                    scope.launch {
                                        val allData = generateSummaryData(viewModel, employees, summaryStartDate, summaryEndDate)
                                        if (allData.isNotEmpty()) ReportGenerator.openSummaryExcel(context, allData, summaryStartDate, summaryEndDate, isShare = true)
                                    }
                                }
                            )
                        }
                    }

                    var showMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(expanded = showMoreMenu, onDismissRequest = { showMoreMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Verileri Yedekle") },
                                leadingIcon = { Icon(Icons.Default.Backup, null) },
                                onClick = {
                                    showMoreMenu = false
                                    backupLauncher.launch("personel_takip_yedek_${LocalDate.now()}.db")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Yedekten Geri Yükle") },
                                leadingIcon = { Icon(Icons.Default.Restore, null) },
                                onClick = {
                                    showMoreMenu = false
                                    restoreLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEmployee) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offset += pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(employees) { employee ->
                EmployeeItem(
                    employee = employee, 
                    viewModel = viewModel,
                    onClick = { onEmployeeClick(employee.id) },
                    onEdit = { 
                        employeeToEdit = employee
                        showEditDialog = true
                    },
                    onDelete = { viewModel.deleteEmployee(employee) }
                )
            }
        }
    }

    if (showSummaryRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = summaryStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = summaryEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showSummaryRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    } ?: summaryStartDate
                    val end = dateRangePickerState.selectedEndDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    } ?: summaryEndDate
                    summaryStartDate = start
                    summaryEndDate = end
                    showSummaryRangePicker = false
                }) { Text("Uygula") }
            },
            dismissButton = {
                TextButton(onClick = { showSummaryRangePicker = false }) { Text("İptal") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Özet Rapor Aralığı Seçin", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false,
                modifier = Modifier.fillMaxWidth().height(400.dp)
            )
        }
    }

    if (showEditDialog && employeeToEdit != null) {
        EditEmployeeDialog(
            employee = employeeToEdit!!,
            selectedImageUri = selectedImageUri,
            onImagePick = { imageLauncher.launch("image/*") },
            onDismiss = { 
                showEditDialog = false
                selectedImageUri = null
            },
            onConfirm = { updatedEmployee ->
                viewModel.updateEmployee(updatedEmployee)
                showEditDialog = false
                selectedImageUri = null
            }
        )
    }
}

@Composable
fun EmployeeItem(employee: Employee, viewModel: EmployeeViewModel, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val workLogs by viewModel.getWorkLogs(employee.id).collectAsState(initial = emptyList())
    val adjustments by viewModel.getAdjustments(employee.id).collectAsState(initial = emptyList())

    val netAlacak = remember(employee, workLogs, adjustments) {
        val now = LocalDate.now()
        val filterStartDate = now.withDayOfMonth(1)
        val filterEndDate = now.withDayOfMonth(now.lengthOfMonth())
        
        val report = com.burhan2855.personeltakip.logic.SalaryCalculator().calculateRangeSalary(
            employee, workLogs, adjustments, filterStartDate, filterEndDate
        )
        
        // Turnover calculation (before current month)
        var totalPriorEarnings = 0.0
        var totalPriorPayments = 0.0
        val startDate = employee.startDate ?: now.minusMonths(12)
        var iterDate = startDate.withDayOfMonth(1)
        
        while (iterDate.isBefore(filterStartDate)) {
            val mLogs = workLogs.filter { it.date.year == iterDate.year && it.date.monthValue == iterDate.monthValue }
            val mAdjs = adjustments.filter { it.date.year == iterDate.year && it.date.monthValue == iterDate.monthValue }
            
            if (mLogs.isNotEmpty() || mAdjs.isNotEmpty()) {
                val mReport = com.burhan2855.personeltakip.logic.SalaryCalculator().calculateMonthlySalary(
                    employee, mLogs, mAdjs, iterDate.year, iterDate.monthValue
                )
                totalPriorEarnings += mReport.totalEarnings
                totalPriorPayments += mAdjs.filter { it.type == AdjustmentType.MAAS_ODEME }.sumOf { it.amount }
            }
            iterDate = iterDate.plusMonths(1)
        }
        val turnover = totalPriorEarnings - totalPriorPayments
        
        // Top. Alacak (Genel) = Hakediş + Devir
        report.totalEarnings + turnover
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Personel Resmi
            if (employee.imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(employee.imageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${employee.firstName} ${employee.lastName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = employee.position, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Top. Alacak (Genel): ${String.format("%,.2f", netAlacak)} TL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
                if (!employee.iban.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .clickable {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("IBAN", employee.iban)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "IBAN kopyalandı", Toast.LENGTH_SHORT).show()
                            }
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "IBAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = employee.iban!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmployeeDialog(
    employee: Employee, 
    selectedImageUri: Uri?,
    onImagePick: () -> Unit,
    onDismiss: () -> Unit, 
    onConfirm: (Employee) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var firstName by remember { mutableStateOf(employee.firstName) }
    var lastName by remember { mutableStateOf(employee.lastName) }
    var position by remember { mutableStateOf(employee.position) }
    var additionalSalary by remember { mutableStateOf(employee.additionalSalary.toString()) }
    var annualLeave by remember { mutableStateOf(employee.annualLeaveEntitlement.toString()) }
    var mealAllowance by remember { mutableStateOf(employee.mealAllowance.toString()) }
    var transportAllowance by remember { mutableStateOf(employee.transportAllowance.toString()) }
    var startDate by remember { mutableStateOf(employee.startDate) }
    var endDate by remember { mutableStateOf(employee.endDate) }
    var phoneNumber by remember { mutableStateOf(employee.phoneNumber ?: "") }
    var email by remember { mutableStateOf(employee.email ?: "") }
    var iban by remember { mutableStateOf(employee.iban ?: "") }
    var address by remember { mutableStateOf(employee.address ?: "") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val currentImageUri = selectedImageUri ?: employee.imageUri?.let { Uri.parse(it) }
    
    // Mevcut saatlik ücret ve çalışma saatinden aylık maaşı geri hesapla
    val initialMonthlySalary = (employee.hourlyRate * 30 * employee.dailyWorkingHours).let { 
        if (it > 0) String.format("%.0f", it) else "" 
    }
    var monthlySalary by remember { mutableStateOf(initialMonthlySalary) }
    var dailyHours by remember { mutableStateOf(employee.dailyWorkingHours.toString()) }
    var overtimeMultiplier by remember { mutableStateOf(employee.overtimeMultiplier.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personeli Düzenle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profil Resmi Alanı (Düzenleme için)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onImagePick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(currentImageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Resim Seç", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Ad") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Soyad") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = position, onValueChange = { position = it }, label = { Text("Pozisyon") }, modifier = Modifier.fillMaxWidth())
                
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Telefon Numarası") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = iban, onValueChange = { iban = it.uppercase() }, label = { Text("IBAN") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                
                OutlinedTextField(
                    value = monthlySalary, 
                    onValueChange = { monthlySalary = it }, 
                    label = { Text("Aylık Net Maaş (TL)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = dailyHours, 
                    onValueChange = { dailyHours = it }, 
                    label = { Text("Günlük Çalışma Saati") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = overtimeMultiplier, 
                    onValueChange = { overtimeMultiplier = it }, 
                    label = { Text("Mesai Çarpanı") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = additionalSalary,
                    onValueChange = { additionalSalary = it },
                    label = { Text("Ek Maaş (TL)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mealAllowance,
                        onValueChange = { mealAllowance = it },
                        label = { Text("Günlük Yemek (TL)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = transportAllowance,
                        onValueChange = { transportAllowance = it },
                        label = { Text("Günlük Yol (TL)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = annualLeave,
                    onValueChange = { annualLeave = it },
                    label = { Text("Yıllık İzin Hakkı (Gün)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // İşe Başlama Tarihi
                OutlinedTextField(
                    value = startDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "",
                    onValueChange = { },
                    label = { Text("İşe Başlama Tarihi") },
                    readOnly = true,
                    trailingIcon = { 
                        IconButton(onClick = { showStartDatePicker = true }) { 
                            Icon(Icons.Default.CalendarToday, null) 
                        } 
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true }
                )

                // İşten Çıkış Tarihi
                OutlinedTextField(
                    value = endDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "",
                    onValueChange = { },
                    label = { Text("İşten Çıkış Tarihi (Opsiyonel)") },
                    readOnly = true,
                    trailingIcon = { 
                        IconButton(onClick = { showEndDatePicker = true }) { 
                            Icon(Icons.Default.CalendarToday, null) 
                        } 
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val salary = monthlySalary.toDoubleOrNull() ?: 0.0
                val additionalSalaryVal = additionalSalary.toDoubleOrNull() ?: 0.0
                val annualLeaveVal = annualLeave.toIntOrNull() ?: 0
                val mealVal = mealAllowance.toDoubleOrNull() ?: 0.0
                val transportVal = transportAllowance.toDoubleOrNull() ?: 0.0
                val hours = dailyHours.toDoubleOrNull() ?: 8.0
                val multiplier = overtimeMultiplier.toDoubleOrNull() ?: 1.5
                val calculatedHourlyRate = if (hours > 0) salary / (30 * hours) else 0.0

                // Resim değişmişse dahili depolamaya kaydet
                val finalImageUri = if (selectedImageUri != null) {
                    selectedImageUri.let { uri ->
                        com.burhan2855.personeltakip.util.ImageUtils.saveImageToInternalStorage(context, uri)
                    }
                } else {
                    employee.imageUri
                }

                onConfirm(employee.copy(
                    firstName = firstName,
                    lastName = lastName,
                    position = position,
                    hourlyRate = calculatedHourlyRate,
                    dailyWorkingHours = hours,
                    overtimeMultiplier = multiplier,
                    additionalSalary = additionalSalaryVal,
                    annualLeaveEntitlement = annualLeaveVal,
                    mealAllowance = mealVal,
                    transportAllowance = transportVal,
                    imageUri = finalImageUri,
                    startDate = startDate,
                    endDate = endDate,
                    iban = iban,
                    phoneNumber = phoneNumber,
                    email = email,
                    address = address
                ))
            }) { Text("Güncelle") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showStartDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showEndDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private suspend fun generateSummaryData(
    viewModel: EmployeeViewModel,
    employees: List<Employee>,
    startDate: LocalDate,
    endDate: LocalDate
): List<ReportGenerator.SummaryRowData> {
    val allData = mutableListOf<ReportGenerator.SummaryRowData>()
    employees.forEach { emp ->
        val logs = viewModel.getWorkLogs(emp.id).first()
        val adjustments = viewModel.getAdjustments(emp.id).first()
        val currentReport = com.burhan2855.personeltakip.logic.SalaryCalculator().calculateRangeSalary(
            emp, logs, adjustments, startDate, endDate
        )

        var totalPriorEarnings = 0.0
        var totalPriorPayments = 0.0
        val empStartDate = emp.startDate ?: startDate.minusMonths(12)
        var iterDate = empStartDate.withDayOfMonth(1)
        val summaryStartMonth = startDate.withDayOfMonth(1)

        while (iterDate.isBefore(summaryStartMonth)) {
            val mLogs = logs.filter { it.date.year == iterDate.year && it.date.monthValue == iterDate.monthValue }
            val mAdjs = adjustments.filter { it.date.year == iterDate.year && it.date.monthValue == iterDate.monthValue }
            if (mLogs.isNotEmpty() || mAdjs.isNotEmpty()) {
                val mReport = com.burhan2855.personeltakip.logic.SalaryCalculator().calculateMonthlySalary(
                    emp, mLogs, mAdjs, iterDate.year, iterDate.monthValue
                )
                totalPriorEarnings += mReport.totalEarnings
                totalPriorPayments += mAdjs.filter { it.type == AdjustmentType.MAAS_ODEME }.sumOf { it.amount }
            }
            iterDate = iterDate.plusMonths(1)
        }
        val turnover = totalPriorEarnings - totalPriorPayments
        allData.add(ReportGenerator.SummaryRowData(emp, currentReport, adjustments, turnover))
    }
    return allData
}
