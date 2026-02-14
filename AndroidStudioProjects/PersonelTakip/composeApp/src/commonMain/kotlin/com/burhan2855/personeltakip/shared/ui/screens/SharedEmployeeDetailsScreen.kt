package com.burhan2855.personeltakip.shared.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burhan2855.personeltakip.shared.data.*
import com.burhan2855.personeltakip.shared.logic.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedEmployeeDetailsScreen(
    employeeId: Int,
    employeeService: IEmployeeService,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val employeeState by employeeService.getEmployeeById(employeeId).collectAsState(initial = null)
    val workLogs by employeeService.getWorkLogsForEmployee(employeeId).collectAsState(initial = emptyList())
    val adjustments by employeeService.getAdjustmentsForEmployee(employeeId).collectAsState(initial = emptyList())
    
    var showLogDialog by remember { mutableStateOf(false) }
    var showAdjustmentDialog by remember { mutableStateOf(false) }
    var showEditEmployeeDialog by remember { mutableStateOf(false) }
    var showIndemnityDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<WorkLog?>(null) }
    var editingAdjustment by remember { mutableStateOf<Adjustment?>(null) }

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectedMonth by remember { mutableStateOf(today.monthNumber) }
    var selectedYear by remember { mutableStateOf(today.year) }

    val filterStartDate = LocalDate(selectedYear, selectedMonth, 1)
    val filterEndDate = if (selectedMonth == 12) LocalDate(selectedYear + 1, 1, 1).minus(1, DateTimeUnit.DAY) 
                        else LocalDate(selectedYear, selectedMonth + 1, 1).minus(1, DateTimeUnit.DAY)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Simple Month selection could be here */ }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Ay Seç")
                    }
                    IconButton(onClick = { showEditEmployeeDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                    }
                    IconButton(onClick = { 
                        scope.launch { 
                            employeeState?.let { employeeService.deleteEmployee(it) }
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingLog = null; showLogDialog = true },
                containerColor = Color(0xFFE8EAF6),
                contentColor = Color(0xFF3F51B5),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Puantaj Ekle")
            }
        }
    ) { padding ->
        val employee = employeeState ?: return@Scaffold
        
        val report = remember(employee, workLogs, adjustments, selectedMonth, selectedYear) {
            SalaryCalculator().calculateRangeSalary(employee, workLogs, adjustments, filterStartDate, filterEndDate)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8EAF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = Color(0xFF3F51B5))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "${employee.firstName} ${employee.lastName}".lowercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            val monthlySalaryVal = (employee.hourlyRate * 30 * employee.dailyWorkingHours) + employee.additionalSalary
                            Text(
                                text = "Maaş: ${monthlySalaryVal.toMoneyString()} TL",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            buildString {
                                append("Başlama: ${employee.startDate?.let { "${it.dayOfMonth.padZero()}.${it.monthNumber.padZero()}.${it.year}" } ?: "Bilinmiyor"}")
                                employee.endDate?.let { 
                                    append(" • ")
                                    append("Çıkış: ${it.dayOfMonth.padZero()}.${it.monthNumber.padZero()}.${it.year}")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (employee.endDate != null) Color.Red else Color.Gray
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFEFF1F7)).padding(12.dp)
                ) {
                    Text("Kişisel Bilgiler", fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                }
            }

            item {
                SharedSalaryDashboard(employee, report)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { editingAdjustment = null; showAdjustmentDialog = true },
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C5E91))
                    ) {
                        Icon(Icons.Default.Payments, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ödeme/Kesinti")
                    }
                    
                    IconButton(
                        onClick = { employeeService.exportReport(employee, report, workLogs, adjustments, ExportType.PDF) },
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE91E63))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = Color.White)
                    }

                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF4CAF50)).clickable { 
                            employeeService.exportReport(employee, report, workLogs, adjustments, ExportType.EXCEL)
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TableChart, null, tint = Color.White)
                    }
                }
            }

            if (employee.endDate != null) {
                item {
                    Button(
                        onClick = { showIndemnityDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                    ) {
                        Icon(Icons.Default.Calculate, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tazminat Hesapla")
                    }
                }
            }

            item {
                Text("Puantaj ve Ödemeler", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            val currentLogs = workLogs.filter { it.date >= filterStartDate && it.date <= filterEndDate }
            val currentAdjs = adjustments.filter { it.date >= filterStartDate && it.date <= filterEndDate }
            val combinedItems = (currentLogs.map { it to "log" } + currentAdjs.map { it to "adj" })
                              .sortedByDescending { (it.first as? WorkLog)?.date ?: (it.first as Adjustment).date }

            items(combinedItems) { itemPair ->
                if (itemPair.second == "log") {
                    val log = itemPair.first as WorkLog
                    WorkLogItem(log, onEdit = { editingLog = log; showLogDialog = true }, 
                                onDelete = { scope.launch { employeeService.deleteWorkLog(log) } })
                } else {
                    val adj = itemPair.first as Adjustment
                    AdjustmentItem(adj, onEdit = { editingAdjustment = adj; showAdjustmentDialog = true },
                                    onDelete = { scope.launch { employeeService.deleteAdjustment(adj) } })
                }
            }
        }
    }

    if (showLogDialog) {
        SharedWorkLogDialog(
            initialLog = editingLog,
            onDismiss = { showLogDialog = false },
            onConfirm = { log ->
                scope.launch {
                    val finalLog = log.copy(employeeId = employeeId)
                    if (editingLog == null) employeeService.insertWorkLog(finalLog)
                    else employeeService.updateWorkLog(finalLog)
                }
                showLogDialog = false
            }
        )
    }

    if (showAdjustmentDialog) {
        SharedAdjustmentDialog(
            initialAdjustment = editingAdjustment,
            onDismiss = { showAdjustmentDialog = false },
            onConfirm = { adj ->
                scope.launch {
                    val finalAdj = adj.copy(employeeId = employeeId)
                    if (editingAdjustment == null) employeeService.insertAdjustment(finalAdj)
                    else employeeService.updateAdjustment(finalAdj)
                }
                showAdjustmentDialog = false
            }
        )
    }

    if (showEditEmployeeDialog) {
        SharedEmployeeDialog(
            initialEmployee = employeeState,
            onDismiss = { showEditEmployeeDialog = false },
            onConfirm = { updatedEmployee ->
                scope.launch {
                    employeeService.updateEmployee(updatedEmployee)
                }
                showEditEmployeeDialog = false
            }
        )
    }

    if (showIndemnityDialog) {
        val currentEmp = employeeState ?: return@SharedEmployeeDetailsScreen
        val indemnityReport = remember(currentEmp, workLogs, adjustments) {
            SalaryCalculator().calculateIndemnity(currentEmp, workLogs, adjustments)
        }
        indemnityReport?.let { report ->
            SharedIndemnityDialog(
                report = report,
                onDismiss = { showIndemnityDialog = false },
                onExportPdf = { employeeService.exportIndemnityReport(report) }
            )
        }
    }
}

@Composable
fun SharedIndemnityDialog(
    report: IndemnityReport,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("İşten Çıkış Tazminatı", fontWeight = FontWeight.Bold)
                IconButton(onClick = onExportPdf) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFFE91E63))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Çalışma Süresi", fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                        Text("${report.years} Yıl ${report.months} Ay ${report.remainingDays} Gün", style = MaterialTheme.typography.titleMedium)
                        
                        val baseDays = (report.years * 365) + report.remainingDays
                        val leapDays = report.totalDaysWorked - baseDays
                        val leapText = if (leapDays > 0) " (+ $leapDays gün artık yıl)" else ""
                        
                        Text("Gün Bazında Toplam: (${report.years} x 365) + ${report.remainingDays} = ${report.totalDaysWorked} gün$leapText", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                // Daily Rates Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1F7))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Günlük Ücretler", fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                        IndemnityRow("Net Maaş", report.dailyNetMaas)
                        IndemnityRow("Yemek", report.dailyNetYemek)
                        IndemnityRow("Yol", report.dailyNetYol)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Toplam Günlük", fontWeight = FontWeight.Bold)
                            Text("${report.totalDaily.toMoneyString()} TL", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Calculation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC).copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Tazminat Hesabı", fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                        IndemnityRow("Kıdem Tazminatı", report.kidemTazminati)
                        IndemnityRow("Kalan İzin (${report.remainingLeaveDays} gün)", report.remainingLeaveAmount)
                        IndemnityRow("Toplam Yemek", report.totalYemek, color = Color(0xFF3F51B5))
                        IndemnityRow("Toplam Yol", report.totalYol, color = Color(0xFF3F51B5))
                        IndemnityRow("Toplam Prim", report.totalPrim, color = Color(0xFF2E7D32))
                        IndemnityRow("Toplam Avanslar", -report.totalAvans, color = Color.Red)
                        IndemnityRow("Toplam Kesintiler", -report.totalKesinti, color = Color.Red)
                        
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("NET TAZMİNAT", fontWeight = FontWeight.ExtraBold, color = Color(0xFF3949AB))
                            Text("${report.netTazminat.toMoneyString()} TL", fontWeight = FontWeight.ExtraBold, color = Color(0xFF3949AB))
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        IndemnityRow("Ödenen Tazminat", report.paidTazminat, color = Color(0xFF2E7D32))
                        
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("KALAN ÖDEME:", fontWeight = FontWeight.ExtraBold, color = Color.Red, style = MaterialTheme.typography.titleMedium)
                            Text("${report.remainingPayment.toMoneyString()} TL", fontWeight = FontWeight.ExtraBold, color = Color.Red, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun IndemnityRow(label: String, amount: Double, color: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("${amount.toMoneyString()} TL", color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SharedSalaryDashboard(employee: Employee, report: SalaryReport) {
    val fixedBase = (employee.hourlyRate * employee.dailyWorkingHours * 30) + employee.additionalSalary
    
    // Sum Yol/Yemek from logs AND adjustments for the TOTAL AMOUNT
    val totalYemek = report.totalMealAllowance + report.totalAdjustments.yemekAmount
    val totalYol = report.totalTransportAllowance + report.totalAdjustments.yolAmount
    
    // Use ACTUAL COUNTS (Logs + Adjustments) for the labels
    val mealCount = report.totalYemekCount
    val transportCount = report.totalYolCount

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Sabit Net Maaş", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${fixedBase.toMoneyString()} TL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Top. Alacak (Genel)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${report.totalEarnings.toMoneyString()} TL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // Period
            Text("Dönem", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("${report.startDate.dayOfMonth.padZero()}.${report.startDate.monthNumber.padZero()} - ${report.endDate.dayOfMonth.padZero()}.${report.endDate.monthNumber.padZero()}", fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))

            Spacer(Modifier.height(12.dp))

            // Details
            DetailRow("Fazla Mesai (${report.overtimeHours.formatHours()} sa)", report.totalOvertimeEarnings, isPositive = true)
            DetailRow("Top. Kesinti/Avans", -(report.totalAdjustments.avansAmount + report.totalAdjustments.kesintiAmount), isPositive = false)
            DetailRow("Eksik Çalışma (${report.missingHours.formatHours()} sa)", -report.totalMissingDeduction, isPositive = false)
            
            if (totalYemek > 0) {
                DetailRow("Yemek (${mealCount.toInt()} adet)", totalYemek, isPositive = true, color = Color(0xFF3F51B5))
            }
            if (totalYol > 0) {
                DetailRow("Yol (${transportCount.toInt()} adet)", totalYol, isPositive = true, color = Color(0xFF3F51B5))
            }
            if (report.totalAdjustments.primAmount > 0) {
                DetailRow("Prim", report.totalAdjustments.primAmount, isPositive = true, color = Color(0xFF4CAF50))
            }
            if (report.totalAdjustments.tazminatAmount > 0) {
                DetailRow("Tazminat Ödemesi", -report.totalAdjustments.tazminatAmount, isPositive = false, color = Color.Red)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            // Current Balance
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("GÜNCEL BAKİYE (KALAN)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF3949AB))
                Text("${report.remainingBalance.toMoneyString()} TL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = Color(0xFF3949AB))
            }

            Spacer(Modifier.height(16.dp))

            // Leave Status
            Text("İzin Durumu", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                LeaveInfoItem("Ücretli", report.paidLeaveDays)
                LeaveInfoItem("Ücretsiz", report.unpaidLeaveDays)
                LeaveInfoItem("Rapor", report.sickLeaveDays)
                LeaveInfoItem("Yıllık", report.annualLeaveDays)
            }

            Spacer(Modifier.height(16.dp))

            // Annual Leave Remaining
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kalan Yıllık İzin Hakkı", fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                Text("${report.remainingAnnualLeave} Gün", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, amount: Double, isPositive: Boolean, color: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        val displayColor = color ?: if (isPositive) Color(0xFF4CAF50) else Color(0xFFE91E63)
        val prefix = if (amount > 0) "+" else if (amount < 0) "" else if (isPositive) "+" else "-"
        Text("$prefix${amount.toMoneyString()} TL", color = displayColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LeaveInfoItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.Gray)
        Text(count.toString(), fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
    }
}

@Composable
fun WorkLogItem(log: WorkLog, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE8EAF6)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.DateRange, null, tint = Color(0xFF3F51B5), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${log.date.dayOfMonth.padZero()}.${log.date.monthNumber.padZero()}.${log.date.year}", fontWeight = FontWeight.Bold)
                Text(if (log.isOnLeave) "İzin: ${log.leaveType}" else "${log.startTime?.hour?.padZero()}:${log.startTime?.minute?.padZero()} - ${log.endTime?.hour?.padZero()}:${log.endTime?.minute?.padZero()}", 
                    style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color(0xFF3F51B5)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        }
    }
}

@Composable
fun AdjustmentItem(adj: Adjustment, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(if(adj.type == AdjustmentType.AVANS || adj.type == AdjustmentType.KESINTI) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)), contentAlignment = Alignment.Center) {
                val icon = if(adj.type == AdjustmentType.AVANS || adj.type == AdjustmentType.KESINTI) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp
                Icon(icon, null, tint = if(adj.type == AdjustmentType.AVANS || adj.type == AdjustmentType.KESINTI) Color.Red else Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val label = when(adj.type) {
                    AdjustmentType.AVANS -> "Avans"
                    AdjustmentType.KESINTI -> "Kesinti"
                    AdjustmentType.PRIM -> "Prim"
                    AdjustmentType.MAAS_ODEME -> "Maaş Ödemesi"
                    AdjustmentType.BANKA_ODEME -> "Banka Ödemesi"
                    AdjustmentType.ELDEN_ODEME -> "Elden Ödeme"
                    AdjustmentType.YEMEK -> "Yemek Ödemesi"
                    AdjustmentType.YOL -> "Yol Ödemesi"
                    AdjustmentType.TAZMINAT_ODEME -> "Tazminat Ödemesi"
                }
                Text(label, fontWeight = FontWeight.Bold)
                Text("${adj.date.dayOfMonth.padZero()}.${adj.date.monthNumber.padZero()} ${adj.description ?: ""}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            val isNegative = adj.type == AdjustmentType.AVANS || adj.type == AdjustmentType.KESINTI
            Text("${if(isNegative) "-" else "+"}${adj.amount.toMoneyString()} TL", 
                 fontWeight = FontWeight.Bold, color = if(isNegative) Color.Red else Color(0xFF4CAF50))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color(0xFF3F51B5)) }
        }
    }
}


fun Int.padZero() = if (this < 10) "0$this" else this.toString()

fun Double.toMoneyString(): String {
    val absolute = if(this < 0) -this else this
    val rounded = (kotlin.math.round(absolute * 100) / 100).toString()
    val parts = rounded.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1].padEnd(2, '0').take(2) else "00"
    
    val reversed = integerPart.reversed()
    val withDots = StringBuilder()
    for (i in reversed.indices) {
        if (i > 0 && i % 3 == 0) withDots.append(".")
        withDots.append(reversed[i])
    }
    return "${withDots.reverse()},$decimalPart"
}

fun Double.formatHours(): String = this.toString().replace(".", ",")
