package com.burhan2855.personeltakip.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import com.burhan2855.personeltakip.data.*
import com.burhan2855.personeltakip.ui.EmployeeViewModel
import com.burhan2855.personeltakip.util.ReportGenerator
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailsScreen(
    employeeId: Int,
    viewModel: EmployeeViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val employees by viewModel.allEmployees.collectAsState(initial = emptyList())
    val employee = employees.find { it.id == employeeId }
    val workLogs by viewModel.getWorkLogs(employeeId).collectAsState(initial = emptyList())
    val adjustments by viewModel.getAdjustments(employeeId).collectAsState(initial = emptyList())
    
    var showLogDialog by remember { mutableStateOf(false) }
    var showAdjustmentDialog by remember { mutableStateOf(false) }
    var showReportOptions by remember { mutableStateOf(false) }
    var showExcelOptions by remember { mutableStateOf(false) }
    var showSeveranceDialog by remember { mutableStateOf(false) }
    
    var editingLog by remember { mutableStateOf<WorkLog?>(null) }
    var editingAdjustment by remember { mutableStateOf<Adjustment?>(null) }

    var filterStartDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var filterEndDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())) }
    var showRangePicker by remember { mutableStateOf(false) }
    
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var fabOffset by remember { mutableStateOf(Offset.Zero) }

    if (employee == null) return

    val report = remember(employee, workLogs, adjustments, filterStartDate, filterEndDate) {
        com.burhan2855.personeltakip.logic.SalaryCalculator().calculateRangeSalary(
            employee, workLogs, adjustments, filterStartDate, filterEndDate
        )
    }

    val turnover = remember(workLogs, adjustments, filterStartDate) {
        var totalPriorEarnings = 0.0
        var totalPriorPayments = 0.0
        val now = LocalDate.now()
        val startDate = employee.startDate ?: now.minusMonths(12)
        var iterDate = startDate.withDayOfMonth(1)
        val filterStartMonth = filterStartDate.withDayOfMonth(1)
        
        while (iterDate.isBefore(filterStartMonth)) {
            val mLogs = workLogs.filter { it.date.year == iterDate.year && it.date.monthValue == iterDate.monthValue }
            val mAdjs = adjustments.filter { it.date.year == iterDate.year && it.date.monthValue == iterDate.monthValue }
            
            if (mLogs.isNotEmpty() || mAdjs.isNotEmpty()) {
                val mReport = com.burhan2855.personeltakip.logic.SalaryCalculator().calculateMonthlySalary(
                    employee, mLogs, mAdjs, iterDate.year, iterDate.monthValue
                )
                totalPriorEarnings += mReport.totalEarnings
                totalPriorPayments += mAdjs.filter { it.type == com.burhan2855.personeltakip.data.AdjustmentType.MAAS_ODEME }.sumOf { it.amount }
            }
            
            iterDate = iterDate.plusMonths(1)
        }
        totalPriorEarnings - totalPriorPayments
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (employee.imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(employee.imageUri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(16.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(Modifier.width(16.dp))
                        }
                        Column {
                            Text("${employee.firstName} ${employee.lastName}")
                                val combinedSalary = (employee.hourlyRate * employee.dailyWorkingHours * 30) + employee.additionalSalary
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(employee.position, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Maaş: ${String.format("%,.0f", combinedSalary)} TL",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            // İşe Başlama ve Çıkış Tarihleri
                            if (employee.startDate != null || employee.endDate != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (employee.startDate != null) {
                                        Text(
                                            "Başlama: ${employee.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (employee.startDate != null && employee.endDate != null) {
                                        Text(" • ", style = MaterialTheme.typography.labelSmall)
                                    }
                                    if (employee.endDate != null) {
                                        Text(
                                            "Çıkış: ${employee.endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFF44336)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showRangePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Tarih Aralığı", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.deleteEmployee(employee); onNavigateBack() }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Personeli Sil", tint = Color.Red)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingLog = null; showLogDialog = true },
                modifier = Modifier
                    .offset { IntOffset(fabOffset.x.roundToInt(), fabOffset.y.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress { change, dragAmount ->
                            change.consume()
                            fabOffset += dragAmount
                        }
                    }
            ) {
                Icon(Icons.Default.Add, contentDescription = "İşlem Ekle")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        val newOffset = offset + pan
                        offset = newOffset
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Kişisel Bilgiler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        
                        if (!employee.phoneNumber.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(employee.phoneNumber!!, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${employee.phoneNumber}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Call, "Ara", modifier = Modifier.size(18.dp), tint = Color(0xFF4CAF50))
                                    }
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${employee.phoneNumber}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Sms, "SMS", modifier = Modifier.size(18.dp), tint = Color(0xFF2196F3))
                                    }
                                    IconButton(
                                        onClick = {
                                            val url = "https://api.whatsapp.com/send?phone=${employee.phoneNumber!!.replace(" ", "").replace("+", "")}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, "WhatsApp", modifier = Modifier.size(18.dp), tint = Color(0xFF25D366))
                                    }
                                }
                            }
                        }
                        
                        if (!employee.email.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Email, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(employee.email!!, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${employee.email}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Send, "E-posta", modifier = Modifier.size(18.dp), tint = Color(0xFFFB8C00))
                                }
                            }
                        }
                        
                        if (!employee.address.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(employee.address!!, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (!employee.iban.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                    .clickable {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("IBAN", employee.iban)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "IBAN kopyalandı", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("IBAN", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(
                                    text = employee.iban!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { 
                SalaryDashboard(
                    employee = employee, 
                    report = report, 
                    monthlyAdjustments = adjustments.filter { !it.date.isBefore(filterStartDate) && !it.date.isAfter(filterEndDate) },
                    turnover = turnover
                ) 
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { editingAdjustment = null; showAdjustmentDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Payments, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ödeme/Kesinti", maxLines = 1, fontSize = 13.sp)
                    }

                    FilledIconButton(
                        onClick = { showReportOptions = true },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Rapor")
                    }

                    FilledIconButton(
                        onClick = { showExcelOptions = true },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "Excel Rapor")
                    }
                }
            }

            // Tazminat Hesaplama Butonu (sadece çıkış tarihi varsa)
            if (employee.endDate != null) {
                item {
                    Button(
                        onClick = { showSeveranceDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Icon(Icons.Default.Calculate, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tazminat Hesapla")
                    }
                }
            }

            item { Text("Puantaj ve Ödemeler", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

            items(workLogs.sortedByDescending { it.date }) { log ->
                EnhancedWorkLogItem(
                    log = log, 
                    dailyHours = employee.dailyWorkingHours, 
                    onEdit = { editingLog = log; showLogDialog = true },
                    onDelete = { viewModel.deleteWorkLog(log) }
                )
            }

            item { Text("Ek İşlemler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }

            items(adjustments.sortedByDescending { it.date }) { adj ->
                AdjustmentItem(
                    adjustment = adj,
                    onEdit = { editingAdjustment = adj; showAdjustmentDialog = true },
                    onDelete = { viewModel.deleteAdjustment(adj) }
                )
            }
        }
    }

    if (showReportOptions) {
        AlertDialog(
            onDismissRequest = { showReportOptions = false },
            title = { Text("PDF Rapor Türü Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedCard(
                        onClick = { 
                            ReportGenerator.openMonthlySlipPdf(context, employee, report, adjustments, turnover)
                            showReportOptions = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("1. Personel Özet Maaş Bordrosu", modifier = Modifier.weight(1f))
                            
                            // Image Share Button
                            IconButton(onClick = { 
                                ReportGenerator.openMonthlySlipImage(context, employee, report, adjustments, turnover)
                                showReportOptions = false
                            }) {
                                Icon(Icons.Default.Image, "Resim Olarak Paylaş", tint = MaterialTheme.colorScheme.secondary)
                            }
                            
                            // PDF Share Button
                            IconButton(onClick = { 
                                ReportGenerator.openMonthlySlipPdf(context, employee, report, adjustments, turnover, isShare = true)
                                showReportOptions = false
                            }) {
                                Icon(Icons.Default.Share, "Paylaş", tint = Color(0xFF4CAF50))
                            }
                        }
                    }
                    OutlinedCard(
                        onClick = { 
                            ReportGenerator.openDetailedPdf(context, employee, report, workLogs, adjustments, turnover)
                            showReportOptions = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(12.dp))
                            Text("2. Aylık Detaylı Puantaj ve Maaş Tablosu", modifier = Modifier.weight(1f))
                            IconButton(onClick = { 
                                ReportGenerator.openDetailedPdf(context, employee, report, workLogs, adjustments, turnover, isShare = true)
                                showReportOptions = false
                            }) {
                                Icon(Icons.Default.Share, "Paylaş", tint = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReportOptions = false }) { Text("İptal") }
            }
        )
    }

    if (showExcelOptions) {
        AlertDialog(
            onDismissRequest = { showExcelOptions = false },
            title = { Text("Excel Rapor Türü Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedCard(
                        onClick = { 
                            ReportGenerator.openMonthlySlipExcel(context, employee, report, adjustments, turnover)
                            showExcelOptions = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, null, tint = Color(0xFF4CAF50))
                            Spacer(Modifier.width(12.dp))
                            Text("1. Personel Özet Maaş Bordrosu", modifier = Modifier.weight(1f))
                            IconButton(onClick = { 
                                ReportGenerator.openMonthlySlipExcel(context, employee, report, adjustments, turnover, isShare = true)
                                showExcelOptions = false
                            }) {
                                Icon(Icons.Default.Share, "Paylaş", tint = Color(0xFF4CAF50))
                            }
                        }
                    }
                    OutlinedCard(
                        onClick = { 
                            ReportGenerator.openDetailedExcel(context, employee, report, workLogs, adjustments, turnover)
                            showExcelOptions = false 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, null, tint = Color(0xFF2196F3))
                            Spacer(Modifier.width(12.dp))
                            Text("2. Aylık Detaylı Puantaj ve Maaş Tablosu", modifier = Modifier.weight(1f))
                            IconButton(onClick = { 
                                ReportGenerator.openDetailedExcel(context, employee, report, workLogs, adjustments, turnover, isShare = true)
                                showExcelOptions = false
                            }) {
                                Icon(Icons.Default.Share, "Paylaş", tint = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExcelOptions = false }) { Text("İptal") }
            }
        )
    }

    if (showLogDialog) {
        ManualWorkLogDialog(
            initialLog = editingLog,
            onDismiss = { showLogDialog = false },
            onConfirm = { log -> 
                if (editingLog == null) viewModel.addWorkLog(log.copy(employeeId = employeeId))
                else viewModel.updateWorkLog(log.copy(id = editingLog!!.id, employeeId = employeeId))
                showLogDialog = false 
            }
        )
    }

    if (showAdjustmentDialog) {
        AdjustmentDialog(
            initialAdjustment = editingAdjustment,
            onDismiss = { showAdjustmentDialog = false },
            onConfirm = { adj -> 
                if (editingAdjustment == null) viewModel.addAdjustment(adj.copy(employeeId = employeeId))
                else viewModel.updateAdjustment(adj.copy(id = editingAdjustment!!.id, employeeId = employeeId))
                showAdjustmentDialog = false 
            }
        )
    }

    // Tazminat Hesaplama Dialog
    if (showSeveranceDialog) {
        SeveranceCalculationDialog(
            employee = employee,
            workLogs = workLogs,
            adjustments = adjustments,
            onDismiss = { showSeveranceDialog = false }
        )
    }

    // Tarih Aralığı Seçici Dialog
    if (showRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = filterStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = filterEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    } ?: filterStartDate
                    val end = dateRangePickerState.selectedEndDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    } ?: filterEndDate
                    
                    filterStartDate = start
                    filterEndDate = end
                    showRangePicker = false
                }) { Text("Uygula") }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) { Text("İptal") }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("Rapor Tarih Aralığı Seçin", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false,
                modifier = Modifier.fillMaxWidth().height(400.dp)
            )
        }
    }
}

@Composable
fun SalaryDashboard(
    employee: Employee, 
    report: com.burhan2855.personeltakip.logic.SalaryReport, 
    monthlyAdjustments: List<Adjustment>,
    turnover: Double = 0.0
) {
    val fixedBase = employee.hourlyRate * employee.dailyWorkingHours * 30
    val fixedAdditional = employee.additionalSalary
    val combinedMonthlySalary = fixedBase + fixedAdditional
    
    val manualKesintiAmount = report.totalAdjustments.kesintiAmount
    val avans = report.totalAdjustments.avansAmount
    val totalCombinedDeduction = report.totalMissingDeduction + manualKesintiAmount + avans
    
    // Aylık Hakediş = Calculator tarafından hesaplanan toplam (Sabit Maaş - Kesintiler + Mesailer + Yan Ödemeler)
    // report.totalEarnings artık bu sabit maaş mantığını default olarak döndürüyor.
    val monthlyHakedis = report.totalEarnings
    
    // Toplam Alacak = Aylık Hakediş + Önceki Devir
    val totalReceivable = monthlyHakedis + turnover

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Sabit Net Maaş", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${String.format("%,.2f", combinedMonthlySalary)} TL",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Top. Alacak (Genel)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "${String.format("%,.2f", totalReceivable)} TL",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem("Dönem", "${report.startDate.format(DateTimeFormatter.ofPattern("dd.MM"))} - ${report.endDate.format(DateTimeFormatter.ofPattern("dd.MM"))}", MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem("Fazla Mesai (${String.format("%.1f", report.overtimeHours)} sa)", "+${String.format("%.2f", report.totalOvertimeEarnings)} TL", Color(0xFF4CAF50))
                SummaryItem("Top. Kesinti/Avans", "-${String.format("%.2f", totalCombinedDeduction)} TL", Color(0xFFF44336))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Puantaj Bazlı Detaylar
                if (report.unpaidLeaveDeduction > 0) {
                    DetailRow("Ücretsiz İzin (${report.unpaidLeaveDays} gün)", -report.unpaidLeaveDeduction, Color(0xFFE91E63))
                }
                if (report.sickLeaveDeduction > 0) {
                    DetailRow("Raporlu (${report.sickLeaveDays} gün)", -report.sickLeaveDeduction, Color(0xFFE91E63))
                }
                if (report.missingHoursDeduction > 0) {
                    DetailRow("Eksik Çalışma (${String.format("%.1f", report.missingHours)} sa)", -report.missingHoursDeduction, Color(0xFFE91E63))
                }

                // Manuel Kesinti Detayları (Açıklamalı)
                monthlyAdjustments.filter { it.type == AdjustmentType.KESINTI }.forEach { adj ->
                    val label = if (!adj.description.isNullOrBlank()) adj.description else "Kesinti"
                    DetailRow(label, -adj.amount, Color(0xFFE91E63))
                }

                DetailRow("Avans (${report.totalAdjustments.avansCount} adet)", -avans, Color(0xFFE91E63))
                
                Spacer(modifier = Modifier.height(4.dp))
                
                DetailRow("Yemek (${report.totalAdjustments.yemekCount} adet)", report.totalAdjustments.yemekAmount, Color(0xFF2196F3))
                DetailRow("Yol (${report.totalAdjustments.yolCount} adet)", report.totalAdjustments.yolAmount, Color(0xFF2196F3))
                DetailRow("Prim (${report.totalAdjustments.primCount} adet)", report.totalAdjustments.primAmount, Color(0xFF2196F3))
                
                if (report.totalMealAllowance > 0) {
                    DetailRow("Yemek Ücreti (Günlük)", report.totalMealAllowance, Color(0xFF4CAF50))
                }
                if (report.totalTransportAllowance > 0) {
                    DetailRow("Yol Ücreti (Günlük)", report.totalTransportAllowance, Color(0xFF4CAF50))
                }
                
                if (report.totalAdjustments.maasOdemeAmount > 0) {
                    DetailRow("Yapılan Maaş Ödemesi", -report.totalAdjustments.maasOdemeAmount, Color(0xFF4CAF50))
                }
                
                // Bankadan ve Elden Ödeme
                val bankaOdeme = monthlyAdjustments.filter { it.type == AdjustmentType.BANKA_ODEME }.sumOf { it.amount }
                val eldenOdeme = monthlyAdjustments.filter { it.type == AdjustmentType.ELDEN_ODEME }.sumOf { it.amount }
                
                if (bankaOdeme > 0) {
                    DetailRow("Bankadan Ödenen", -bankaOdeme, Color(0xFF4CAF50))
                }
                if (eldenOdeme > 0) {
                    DetailRow("Elden Ödenen", -eldenOdeme, Color(0xFF4CAF50))
                }
                
                DetailRow("Önceki Dönemden Devir", turnover, if (turnover >= 0) Color(0xFF2196F3) else Color(0xFFF44336))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            val bankaOdemeTotal = monthlyAdjustments.filter { it.type == AdjustmentType.BANKA_ODEME }.sumOf { it.amount }
            val eldenOdemeTotal = monthlyAdjustments.filter { it.type == AdjustmentType.ELDEN_ODEME }.sumOf { it.amount }
            val kalanBakiye = totalReceivable - report.totalAdjustments.maasOdemeAmount - bankaOdemeTotal - eldenOdemeTotal
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("GÜNCEL BAKİYE (KALAN)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${String.format("%,.2f", kalanBakiye)} TL",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (kalanBakiye > 0) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("İzin Durumu", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LeaveStat("Ücretli", report.paidLeaveDays)
                LeaveStat("Ücretsiz", report.unpaidLeaveDays)
                LeaveStat("Rapor", report.sickLeaveDays)
                LeaveStat("Yıllık", report.annualLeaveDays)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kalan Yıllık İzin Hakkı", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${report.remainingAnnualLeave} Gün",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (report.remainingAnnualLeave > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun DetailRow(label: String, amount: Double, color: Color) {
    if (amount != 0.0) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${if(amount > 0) "+" else ""}${String.format("%.2f", amount)} TL", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LeaveStat(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashboardStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier.background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualWorkLogDialog(initialLog: WorkLog?, onDismiss: () -> Unit, onConfirm: (WorkLog) -> Unit) {
    var date by remember { mutableStateOf(initialLog?.date ?: LocalDate.now()) }
    var startTime by remember { mutableStateOf(initialLog?.startTime?.toLocalTime()?.toString() ?: "08:00") }
    var endTime by remember { mutableStateOf(initialLog?.endTime?.toLocalTime()?.toString() ?: "18:00") }
    var isLeave by remember { mutableStateOf(initialLog?.isOnLeave ?: false) }
    var leaveType by remember { mutableStateOf(initialLog?.leaveType ?: "Ücretli İzin") }
    var hasMeal by remember { mutableStateOf(initialLog?.hasMeal ?: true) }
    var hasTransport by remember { mutableStateOf(initialLog?.hasTransport ?: true) }
    var showDatePicker by remember { mutableStateOf(false) }

    val leaveTypes = listOf("Ücretli İzin", "Ücretsiz İzin", "Raporlu", "Yıllık İzin", "Hafta Sonu", "Resmi Tatil")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialLog == null) "Kayıt Ekle" else "Kaydı Düzenle") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    onValueChange = { },
                    label = { Text("Tarih") },
                    readOnly = true,
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLeave, onCheckedChange = { isLeave = it })
                    Text("İzinli Gün")
                }
                if (isLeave) {
                    Text("İzin Türü:", style = MaterialTheme.typography.labelLarge)
                    leaveTypes.forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = leaveType == type, onClick = { leaveType = type })
                            Text(type)
                        }
                    }
                } else {
                    OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Giriş (00:00)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("Çıkış (00:00)") }, modifier = Modifier.fillMaxWidth())
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasMeal, onCheckedChange = { hasMeal = it })
                        Text("Yemek Var")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasTransport, onCheckedChange = { hasTransport = it })
                        Text("Yol Var")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    val start = if (isLeave) null else LocalDateTime.of(date, LocalTime.parse(startTime))
                    val end = if (isLeave) null else LocalDateTime.of(date, LocalTime.parse(endTime))
                    onConfirm(WorkLog(employeeId = 0, date = date, startTime = start, endTime = end, isOnLeave = isLeave, leaveType = if(isLeave) leaveType else null, hasMeal = hasMeal, hasTransport = hasTransport))
                } catch (e: Exception) {}
            }) { Text("Kaydet") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentDialog(initialAdjustment: Adjustment?, onDismiss: () -> Unit, onConfirm: (Adjustment) -> Unit) {
    var amount by remember { mutableStateOf(initialAdjustment?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(initialAdjustment?.type ?: AdjustmentType.AVANS) }
    var desc by remember { mutableStateOf(initialAdjustment?.description ?: "") }
    var date by remember { mutableStateOf(initialAdjustment?.date ?: LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAdjustment == null) "Ödeme/Kesinti Ekle" else "Düzenle") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    onValueChange = { },
                    label = { Text("Tarih") },
                    readOnly = true,
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
                OutlinedTextField(
                    value = amount, 
                    onValueChange = { amount = it }, 
                    label = { Text("Tutar (TL)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                AdjustmentType.entries.forEach { t ->
                    val label = when(t) {
                        AdjustmentType.AVANS -> "Avans"
                        AdjustmentType.YEMEK -> "Yemek"
                        AdjustmentType.YOL -> "Yol"
                        AdjustmentType.PRIM -> "Prim"
                        AdjustmentType.KESINTI -> "Kesinti"
                        AdjustmentType.MAAS_ODEME -> "Maaş Ödemesi"
                        AdjustmentType.TAZMINAT_ODEME -> "Tazminat Ödemesi"
                        AdjustmentType.BANKA_ODEME -> "Bankadan Ödeme"
                        AdjustmentType.ELDEN_ODEME -> "Elden Ödeme"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { type = t }) {
                        RadioButton(selected = type == t, onClick = { type = t })
                        Text(label)
                    }
                }
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Açıklama") }, 
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onConfirm(Adjustment(employeeId = 0, date = date, amount = amt, type = type, description = desc))
            }) { Text("Kaydet") }
        }
    )
}

@Composable
fun EnhancedWorkLogItem(log: WorkLog, dailyHours: Double, onEdit: () -> Unit, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontWeight = FontWeight.Bold)
                if (log.isOnLeave) Text("İzinli (${log.leaveType})", color = MaterialTheme.colorScheme.primary)
                else if (log.startTime != null && log.endTime != null) {
                    val worked = Duration.between(log.startTime, log.endTime).toMinutes() / 60.0
                    val diff = worked - dailyHours
                    Text("${log.startTime.format(formatter)} - ${log.endTime.format(formatter)}")
                    Text(
                        text = if (diff > 0) "+${String.format("%.1f", diff)} sa Mesai" else "${String.format("%.1f", diff)} sa Eksik",
                        color = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Düzenle", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Sil", tint = Color.Red) }
        }
    }
}

@Composable
fun AdjustmentItem(adjustment: Adjustment, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${adjustment.type.name}: ${adjustment.amount} TL", fontWeight = FontWeight.Bold)
                Text("${adjustment.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} - ${adjustment.description ?: ""}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Düzenle", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Sil", tint = Color.Red) }
        }
    }
}
