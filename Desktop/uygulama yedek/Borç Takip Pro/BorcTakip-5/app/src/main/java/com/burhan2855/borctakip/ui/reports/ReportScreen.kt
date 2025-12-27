package com.burhan2855.borctakip.ui.reports

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Contact
import com.burhan2855.borctakip.data.FinancialTotals
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.formatCurrency
import com.burhan2855.borctakip.formatDate
import com.burhan2855.borctakip.util.PdfUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    transactions: List<Transaction>,
    currencySymbol: String,
    contacts: List<Contact>,
    navController: NavController,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    
    // Kasa/Banka işlemlerini HARİÇ tut - sadece gerçek borç/alacak işlemleri
    val reportsTransactions = transactions.filter { 
        it.category != "Kasa Girişi" && 
        it.category != "Kasa Çıkışı" &&
        it.category != "Banka Girişi" &&
        it.category != "Banka Çıkışı"
    }
    
    // Aylık raporları oluştururken her ay için
    // - işlem bazlı gelir/gider (kasa/banka işlemleri hariç)
    // - o aya ait kasa ve banka net hareketleri
    // - o aya ait ödenmemiş alacaklar (alacak = isDebt == false && status == "Ödenmedi")
    // ve hesap toplamı (kasa + banka + alacak) hesaplanır. Gelir sütununda işlem geliri + hesap toplamı gösterilir.
    val monthlyReports = transactions
        .groupBy { transaction ->
            // Taksitler VE alacaklar için: dueDate varsa onu kullan, yoksa date kullan
            val dateToUse = if (transaction.dueDate != null) {
                transaction.dueDate  // Vade tarihi (taksitler ve alacaklar için)
            } else {
                transaction.date ?: System.currentTimeMillis()  // İşlem tarihi
            }
            val dateMillis = dateToUse
            monthFormatter.format(Date(dateMillis))
        }
        .let { grouped ->
            // 12 aylık takvim oluştur
            val firstMonthDate = transactions.minOfOrNull { trans ->
                if (trans.dueDate != null) trans.dueDate else trans.date ?: System.currentTimeMillis()
            } ?: System.currentTimeMillis()
            
            val allMonths = mutableListOf<String>()
            var currentCal = Calendar.getInstance().apply { timeInMillis = firstMonthDate }
            
            // 12 ay ekle
            repeat(12) {
                val monthStr = monthFormatter.format(currentCal.time)
                allMonths.add(monthStr)
                currentCal.add(Calendar.MONTH, 1)
            }
            
            // Her ay için veriyi (varsa) al, yoksa boş list
            allMonths.map { month ->
                month to (grouped[month] ?: emptyList())
            }
        }
        .sortedBy { (month, _) ->
            monthFormatter.parse(month)?.time ?: Long.MAX_VALUE
        }
        .let { sortedMonths ->
            // İlk ay'ın SADECE O AYINA AIT kasa+banka net hareketini devir olarak hesapla
            val firstMonthTransAll = sortedMonths.getOrNull(0)?.second ?: emptyList()
            
            // İlk ayın kasa/banka girişleri ve çıkışları (SADECE İLK AYDA)
            val firstMonthCashIn = firstMonthTransAll
                .filter { it.category == "Kasa Girişi" }.sumOf { it.amount }
            val firstMonthCashOut = firstMonthTransAll
                .filter { it.category == "Kasa Çıkışı" }.sumOf { it.amount }
            val carryForwardCash = firstMonthCashIn - firstMonthCashOut
            
            val firstMonthBankIn = firstMonthTransAll
                .filter { it.category == "Banka Girişi" }.sumOf { it.amount }
            val firstMonthBankOut = firstMonthTransAll
                .filter { it.category == "Banka Çıkışı" }.sumOf { it.amount }
            val carryForwardBank = firstMonthBankIn - firstMonthBankOut
            
            val initialCarryForward = carryForwardCash + carryForwardBank
            
            // Her ay için gelir hesapla ve net'i bir sonraki aya devret
            var carryForwardNet = initialCarryForward
            sortedMonths.map { (month, transAll) ->
                val trans = transAll.filter { t ->
                    t.category != "Kasa Girişi" && t.category != "Kasa Çıkışı" &&
                    t.category != "Banka Girişi" && t.category != "Banka Çıkışı"
                }

                val expense = trans.filter { it.isDebt }.sumOf { it.amount }
                val receivablesForMonth = trans.filter { !it.isDebt }.sumOf { it.amount }

                // Gelir = devir + o ayın alacağı
                val income = carryForwardNet + receivablesForMonth
                
                // Net = gelir - gider
                val netAmount = income - expense
                
                // Bir sonraki ay için net'i devret
                val report = MonthlyReport(month, income, expense, 0.0)
                carryForwardNet = netAmount // Sonraki aya devret
                
                report
            }
        }

    val categoryReports = reportsTransactions
        .filter { it.isDebt }
        .groupBy { it.category ?: "Bilinmiyor" }
        .map { (category, trans) ->
            CategoryReport(category, trans.sumOf { it.amount })
        }
        .sortedByDescending { it.total }

    val upcomingPayments = reportsTransactions
        .filter { 
            it.isDebt && it.status == "Ödenmedi" && it.date?.let { date ->
                date >= System.currentTimeMillis() && date <= Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
            } ?: false
        }
        .sortedBy { it.date }

    val cashBalance = transactions.filter { it.category == "Kasa Girişi" }.sumOf { it.amount } - 
                      transactions.filter { it.category == "Kasa Çıkışı" }.sumOf { it.amount }
    val bankBalance = transactions.filter { it.category == "Banka Girişi" }.sumOf { it.amount } - 
                      transactions.filter { it.category == "Banka Çıkışı" }.sumOf { it.amount }
    
    // Borç/Alacak hesaplamalarında Kasa/Banka işlemlerini HARİÇ tut
    val unpaidDebts = reportsTransactions.filter { it.isDebt && it.status == "Ödenmedi" }.sumOf { it.amount }
    val unpaidReceivables = reportsTransactions.filter { !it.isDebt && it.status == "Ödenmedi" }.sumOf { it.amount }
    val netWorth = cashBalance + bankBalance + unpaidReceivables - unpaidDebts

    // FinancialTotals nesnesi oluştur
    val financialTotals = FinancialTotals(
        cashBalance = cashBalance,
        bankBalance = bankBalance,
        unpaidDebts = unpaidDebts,
        unpaidReceivables = unpaidReceivables,
        netWorth = netWorth
    )

    var upcomingExpanded by remember { mutableStateOf(false) }
    var monthlyExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var generalSummaryExpanded by remember { mutableStateOf(false) }
    var showPdfDialog by remember { mutableStateOf(false) }


    if (showPdfDialog) {
        PdfFilterDialog(
            contacts = contacts,
            onDismiss = { showPdfDialog = false },
            onGenerate = { filterType, contact, startDate, endDate ->
                val filteredTransactions = when (filterType) {
                    "Tümü" -> transactions
                    "Kasa" -> transactions.filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }
                    "Banka" -> transactions.filter { it.category == "Banka Girişi" || it.category == "Banka Çıkışı" }
                    "Kişi" -> transactions.filter { it.contactId?.toLong() == contact?.id }
                    else -> transactions
                }.filter { it.transactionDate in startDate..endDate }
                val pdfFile = PdfUtils.createGeneralSummaryPdf(context, cashBalance, bankBalance, unpaidDebts, unpaidReceivables, netWorth, currencySymbol, true, filteredTransactions)
                openPdf(context, pdfFile)
                showPdfDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Raporlar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "Finansal Raporlar",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri), tint = Color.White)
                    }
                },
                actions = {
                    Card(
                        onClick = { showPdfDialog = true },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Download, 
                                contentDescription = "İndirme seçenekleri", 
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "PDF",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A),
                            Color(0xFF8E24AA),
                            Color(0xFFAB47BC),
                            Color(0xFFF3E5F5)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Genel Toplam Kartı
            item {
                GeneralTotalCard(
                    cashBalance = cashBalance,
                    bankBalance = bankBalance,
                    unpaidDebts = unpaidDebts,
                    unpaidReceivables = unpaidReceivables,
                    netWorth = netWorth,
                    currencySymbol = currencySymbol
                )
            }
            
            item {
                ExpandableReportSection(
                    title = stringResource(id = R.string.genel_hesap_ozeti),
                    expanded = generalSummaryExpanded,
                    onToggle = { generalSummaryExpanded = !generalSummaryExpanded },
                    summaryContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(id = R.string.net_varlik), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCurrency(netWorth, currencySymbol),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onDownload = { 
                        showPdfDialog = true
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportColumn(title = stringResource(id = R.string.kasa), amount = cashBalance, currencySymbol = currencySymbol, color = Color(0xFFF9A825))
                        ReportColumn(title = stringResource(id = R.string.banka), amount = bankBalance, currencySymbol = currencySymbol, color = Color(0xFF1E88E5))
                        ReportColumn(title = stringResource(id = R.string.borc), amount = unpaidDebts, currencySymbol = currencySymbol, color = Color(0xFFE53935))
                        ReportColumn(title = stringResource(id = R.string.alacak), amount = unpaidReceivables, currencySymbol = currencySymbol, color = Color(0xFF43A047))
                    }
                }
            }
            item {
                ExpandableReportSection(
                    title = stringResource(id = R.string.yaklasan_odemeler),
                    expanded = upcomingExpanded,
                    onToggle = { upcomingExpanded = !upcomingExpanded },
                    summaryContent = {
                        val totalUpcoming = upcomingPayments.sumOf { it.amount }
                        Text(
                            text = formatCurrency(totalUpcoming, currencySymbol),
                            color = Color.Red,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    onDownload = {
                        val pdfFile = PdfUtils.createTransactionListPdf(context, upcomingPayments, "Yaklaşan Ödemeler", currencySymbol, financialTotals)
                        openPdf(context, pdfFile)
                    }
                ) {
                    UpcomingPaymentsContent(upcomingPayments, currencySymbol)
                }
            }
            item {
                ExpandableReportSection(
                    title = stringResource(id = R.string.aylik_ozet),
                    expanded = monthlyExpanded,
                    onToggle = { monthlyExpanded = !monthlyExpanded },
                    summaryContent = {
                        // Summary Gelir = sadece İLK AYDA kasa+banka bakiyesi
                        // (sonraki aylar aynı bakiyeyi devir ediyor, tekrar toplamamalıyız)
                        val firstMonthIncome = monthlyReports.firstOrNull()?.income ?: 0.0
                        
                        // Gider = TÜM ayların giderleri toplamı
                        val totalExpense = monthlyReports.sumOf { it.expense }
                        
                        // Net = ilk ayın geliri - tüm giderleri
                        val net = firstMonthIncome - totalExpense
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ReportColumn(
                                title = stringResource(id = R.string.gelir),
                                amount = firstMonthIncome,
                                currencySymbol = currencySymbol,
                                color = Color(0xFF43A047)
                            )
                            ReportColumn(
                                title = stringResource(id = R.string.gider),
                                amount = totalExpense,
                                currencySymbol = currencySymbol,
                                color = Color(0xFFE53935)
                            )
                            ReportColumn(
                                title = stringResource(id = R.string.net),
                                amount = net,
                                currencySymbol = currencySymbol,
                                color = Color(0xFF8E44AD)
                            )
                        }
                    },
                    onDownload = {
                        val pdfFile = PdfUtils.createMonthlySummaryPdf(context, monthlyReports, "Aylık Özet", currencySymbol, financialTotals)
                        openPdf(context, pdfFile)
                    }
                ) {
                    MonthlySummaryContent(monthlyReports, currencySymbol, navController)
                }
            }
            item {
                ExpandableReportSection(
                    title = stringResource(id = R.string.kategoriye_gore_harcamalar),
                    expanded = categoryExpanded,
                    onToggle = { categoryExpanded = !categoryExpanded },
                    summaryContent = {
                        val totalSpending = categoryReports.sumOf { it.total }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Toplam Harcama", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCurrency(totalSpending, currencySymbol),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onDownload = {
                        val pdfFile = PdfUtils.createCategoryReportPdf(context, categoryReports, "Kategoriye Göre Harcamalar", currencySymbol, financialTotals)
                        openPdf(context, pdfFile)
                    }
                ) {
                    CategoryReportsContent(categoryReports, currencySymbol)
                }
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfFilterDialog(
    contacts: List<Contact>,
    onDismiss: () -> Unit,
    onGenerate: (String, Contact?, Long, Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Tümü") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var contactDropdownExpanded by remember { mutableStateOf(false) }

    val startDateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showStartDatePicker by remember { mutableStateOf(false) }
    var selectedStartDate by remember { mutableStateOf(startDateState.selectedDateMillis ?: System.currentTimeMillis()) }

    val endDateState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedEndDate by remember { mutableStateOf(endDateState.selectedDateMillis ?: System.currentTimeMillis()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PDF Filtresi Seçin") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Başlangıç: ${formatDate(selectedStartDate)}")
                        Button(onClick = { showStartDatePicker = true }) {
                            Text("Değiştir")
                        }
                    }
                    Column {
                        Text("Bitiş: ${formatDate(selectedEndDate)}")
                        Button(onClick = { showEndDatePicker = true }) {
                            Text("Değiştir")
                        }
                    }
                }
                if (showStartDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showStartDatePicker = false },
                        confirmButton = { 
                            TextButton(onClick = { 
                                selectedStartDate = startDateState.selectedDateMillis!!
                                showStartDatePicker = false 
                            }) { Text("Tamam") }
                        },
                        dismissButton = { 
                            TextButton(onClick = { showStartDatePicker = false }) { Text("İptal") }
                        }
                    ) {
                        DatePicker(state = startDateState)
                    }
                }
                if (showEndDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showEndDatePicker = false },
                        confirmButton = { 
                            TextButton(onClick = { 
                                selectedEndDate = endDateState.selectedDateMillis!!
                                showEndDatePicker = false 
                            }) { Text("Tamam") }
                        },
                        dismissButton = { 
                            TextButton(onClick = { showEndDatePicker = false }) { Text("İptal") }
                        }
                    ) {
                        DatePicker(state = endDateState)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFilter == "Tümü",
                        onClick = { selectedFilter = "Tümü" }
                    )
                    Text("Tümü")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFilter == "Kasa",
                        onClick = { selectedFilter = "Kasa" }
                    )
                    Text("Kasa")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFilter == "Banka",
                        onClick = { selectedFilter = "Banka" }
                    )
                    Text("Banka")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedFilter == "Kişi",
                        onClick = { selectedFilter = "Kişi" }
                    )
                    ExposedDropdownMenuBox(
                        expanded = contactDropdownExpanded,
                        onExpandedChange = { contactDropdownExpanded = !contactDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedContact?.name ?: "Kişi Seçin",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = contactDropdownExpanded,
                            onDismissRequest = { contactDropdownExpanded = false }
                        ) {
                            contacts.forEach { contact ->
                                DropdownMenuItem(
                                    text = { Text(contact.name) },
                                    onClick = { 
                                        selectedContact = contact
                                        contactDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGenerate(selectedFilter, selectedContact, selectedStartDate, selectedEndDate) }) {
                Text("Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun GeneralTotalCard(
    cashBalance: Double,
    bankBalance: Double,
    unpaidDebts: Double,
    unpaidReceivables: Double,
    netWorth: Double,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF6A1B9A).copy(alpha = 0.2f),
                spotColor = Color(0xFF6A1B9A).copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A).copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Kompakt başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF6A1B9A).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Genel Toplam",
                        tint = Color(0xFF6A1B9A),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Genel Finansal Durum",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C)
                    )
                    Text(
                        text = "Toplam Bakiye Özeti",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Kompakt Net Değer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = if (netWorth >= 0) Color(0xFF4CAF50).copy(alpha = 0.2f) 
                                      else Color(0xFFE53935).copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (netWorth >= 0) Color(0xFF4CAF50).copy(alpha = 0.05f) 
                                   else Color(0xFFE53935).copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (netWorth >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = "Trend",
                            tint = if (netWorth >= 0) Color(0xFF4CAF50) else Color(0xFFE53935),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Net Değer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(netWorth, currencySymbol),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (netWorth >= 0) Color(0xFF4CAF50) else Color(0xFFE53935),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (netWorth >= 0) "Pozitif Bakiye" else "Negatif Bakiye",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF777777)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Kompakt Detay Kartları - 2x2 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialDetailCard(
                    title = "Kasa",
                    amount = cashBalance,
                    currencySymbol = currencySymbol,
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f)
                )
                FinancialDetailCard(
                    title = "Banka",
                    amount = bankBalance,
                    currencySymbol = currencySymbol,
                    color = Color(0xFF2196F3),
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinancialDetailCard(
                    title = "Borçlar",
                    amount = unpaidDebts,
                    currencySymbol = currencySymbol,
                    color = Color(0xFFE53935),
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
                FinancialDetailCard(
                    title = "Alacaklar",
                    amount = unpaidReceivables,
                    currencySymbol = currencySymbol,
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FinancialDetailCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = color.copy(alpha = 0.15f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    color.copy(alpha = 0.05f)
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF555555),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = formatCurrency(amount, currencySymbol),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ExpandableReportSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    summaryContent: @Composable () -> Unit,
    onDownload: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF6A1B9A).copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A).copy(alpha = 0.02f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Sol taraf - Download butonu
                    if (onDownload != null) {
                        Card(
                            onClick = onDownload,
                            modifier = Modifier.align(Alignment.CenterStart),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Download, 
                                    contentDescription = "İndir", 
                                    tint = Color(0xFF6A1B9A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "PDF",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6A1B9A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Orta - Başlık
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (title == stringResource(id = R.string.yaklasan_odemeler)) Color(0xFFE53935) else Color(0xFF2C2C2C),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    // Sağ taraf - Expand ikonu
                    Card(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF6A1B9A).copy(alpha = 0.08f)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF6A1B9A),
                            modifier = Modifier.padding(8.dp).size(20.dp)
                        )
                    }
                }
                if (!expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    summaryContent()
                }
            }
            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
                )
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun UpcomingPaymentsContent(upcomingPayments: List<Transaction>, currencySymbol: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (upcomingPayments.isEmpty()) {
            Text(
                text = "Yaklaşan ödeme bulunmamaktadır.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            upcomingPayments.forEach { transaction ->
                UpcomingPaymentCard(transaction, currencySymbol)
            }
        }
    }
}

@Composable
fun MonthlySummaryContent(monthlyReports: List<MonthlyReport>, currencySymbol: String, navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        monthlyReports.forEach { report ->
            MonthlySummaryCard(
                report = report,
                currencySymbol = currencySymbol,
                onClick = { 
                    val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    val cal = Calendar.getInstance()
                    cal.time = dateFormatter.parse(report.month) ?: Date()
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH) + 1
                    navController.navigate("monthly_detail_screen/$year/$month")
                }
            )
        }
    }
}

@Composable
fun CategoryReportsContent(categoryReports: List<CategoryReport>, currencySymbol: String) {
    val totalSpending = categoryReports.sumOf { it.total }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categoryReports.forEach { report ->
            CategoryReportCard(report, currencySymbol, totalSpending)
        }
    }
}


@Composable
fun UpcomingPaymentCard(transaction: Transaction, currencySymbol: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFFE53935).copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE53935).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(transaction.date ?: System.currentTimeMillis()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Text(
                text = formatCurrency(transaction.amount, currencySymbol),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53935)
            )
        }
    }
}

@Composable
fun MonthlySummaryCard(report: MonthlyReport, currencySymbol: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF6A1B9A).copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Ay",
                    tint = Color(0xFF6A1B9A),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = report.month,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(id = R.string.gelir), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(report.income, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF43A047)
                    )
                    if (report.accountTotal != 0.0) {
                        Text(text = "+ Kasa+Banka+Alacak: ${formatCurrency(report.accountTotal, currencySymbol)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                ReportColumn(title = stringResource(id = R.string.gider), amount = report.expense, currencySymbol = currencySymbol, color = Color(0xFFE53935))

                ReportColumn(title = stringResource(id = R.string.net), amount = (report.income + report.accountTotal) - report.expense, currencySymbol = currencySymbol, color = Color(0xFF1E88E5))
            }
        }
    }
}

@Composable
fun CategoryReportCard(report: CategoryReport, currencySymbol: String, totalSpending: Double) {
    val percentage = if (totalSpending > 0) (report.total / totalSpending).toFloat() else 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF6A1B9A).copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A).copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = report.category, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )
                Text(
                    text = formatCurrency(report.total, currencySymbol), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(
                        Color(0xFF6A1B9A).copy(alpha = 0.1f),
                        RoundedCornerShape(5.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percentage)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6A1B9A),
                                    Color(0xFF8E24AA)
                                )
                            ),
                            RoundedCornerShape(5.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${(percentage * 100).toInt()}% of total",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ReportColumn(title: String, amount: Double, currencySymbol: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount, currencySymbol),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
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
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "PDF görüntüleyici bulunamadı.", Toast.LENGTH_SHORT).show()
    }
}


data class MonthlyReport(val month: String, val income: Double, val expense: Double, val accountTotal: Double)
data class CategoryReport(val category: String, val total: Double)

@Composable
fun MonthlyDetailScreen(
    month: String,
    transactions: List<Transaction>,
    currencySymbol: String,
    onNavigateUp: () -> Unit
) {
    val monthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    
    // Month string'inden year ve month numarası çıkar
    val cal = Calendar.getInstance()
    val parsedDate = monthFormatter.parse(month)
    val (targetYear, targetMonth) = if (parsedDate != null) {
        cal.time = parsedDate
        cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    } else {
        0 to 0
    }
    
    val monthTransactions = transactions.filter { trans ->
        val dateToUse = trans.dueDate ?: trans.date ?: System.currentTimeMillis()
        val transCal = Calendar.getInstance().apply { timeInMillis = dateToUse }
        transCal.get(Calendar.YEAR) == targetYear && transCal.get(Calendar.MONTH) == targetMonth
    }
    
    val debts = monthTransactions.filter { it.isDebt && (it.category != "Kasa Girişi" && it.category != "Kasa Çıkışı" && it.category != "Banka Girişi" && it.category != "Banka Çıkışı") }
    val receivables = monthTransactions.filter { !it.isDebt && (it.category != "Kasa Girişi" && it.category != "Kasa Çıkışı" && it.category != "Banka Girişi" && it.category != "Banka Çıkışı") }
    val cashTransactions = monthTransactions.filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }
    val bankTransactions = monthTransactions.filter { it.category == "Banka Girişi" || it.category == "Banka Çıkışı" }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF6A1B9A)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
            }
            Text(text = month, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (debts.isNotEmpty()) {
                item { Text("Borçlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE53935)) }
                items(debts.size) { index -> TransactionDetailCard(debts[index], currencySymbol) }
            }
            if (receivables.isNotEmpty()) {
                item { Text("Alacaklar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF43A047)) }
                items(receivables.size) { index -> TransactionDetailCard(receivables[index], currencySymbol) }
            }
            if (cashTransactions.isNotEmpty()) {
                item { Text("Kasa İşlemleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)) }
                items(cashTransactions.size) { index -> TransactionDetailCard(cashTransactions[index], currencySymbol) }
            }
            if (bankTransactions.isNotEmpty()) {
                item { Text("Banka İşlemleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00796B)) }
                items(bankTransactions.size) { index -> TransactionDetailCard(bankTransactions[index], currencySymbol) }
            }
            if (monthTransactions.isEmpty()) {
                item { Text("Bu ayda işlem yok", modifier = Modifier.padding(16.dp), color = Color.Gray) }
            }
        }
    }
}

@Composable
fun TransactionDetailCard(transaction: Transaction, currencySymbol: String) {
    Card(modifier = Modifier.fillMaxWidth().shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = transaction.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = transaction.category ?: "Bilinmiyor", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (transaction.dueDate != null) {
                        Text(text = "Vade: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(transaction.dueDate))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                // Borç kırmızı, Alacak yeşil
                val amountColor = if (transaction.isDebt) Color(0xFFE53935) else Color(0xFF43A047)
                Text(text = formatCurrency(transaction.amount, currencySymbol), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = amountColor)
            }
            if (transaction.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = transaction.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

// Yeni MonthlyDetailScreen overload - Int parametreleriyle
@Composable
fun MonthlyDetailScreen(
    year: Int,
    month: Int,
    transactions: List<Transaction>,
    currencySymbol: String,
    onNavigateUp: () -> Unit
) {
    val targetMonth = month - 1
    val cal = Calendar.getInstance().apply { set(year, targetMonth, 1) }
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    
    val monthTransactions = transactions.filter { trans ->
        val dateToUse = trans.dueDate ?: trans.date ?: System.currentTimeMillis()
        val transCal = Calendar.getInstance().apply { timeInMillis = dateToUse }
        transCal.get(Calendar.YEAR) == year && transCal.get(Calendar.MONTH) == targetMonth
    }
    
    val debts = monthTransactions.filter { it.isDebt && (it.category != "Kasa Girişi" && it.category != "Kasa Çıkışı" && it.category != "Banka Girişi" && it.category != "Banka Çıkışı") }
    val receivables = monthTransactions.filter { !it.isDebt && (it.category != "Kasa Girişi" && it.category != "Kasa Çıkışı" && it.category != "Banka Girişi" && it.category != "Banka Çıkışı") }
    val cashTransactions = monthTransactions.filter { it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" }
    val bankTransactions = monthTransactions.filter { it.category == "Banka Girişi" || it.category == "Banka Çıkışı" }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF6A1B9A)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
            }
            Text(text = monthName, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (debts.isNotEmpty()) {
                item { Text("Borçlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE53935)) }
                items(debts.size) { index -> TransactionDetailCard(debts[index], currencySymbol) }
            }
            if (receivables.isNotEmpty()) {
                item { Text("Alacaklar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF43A047)) }
                items(receivables.size) { index -> TransactionDetailCard(receivables[index], currencySymbol) }
            }
            if (cashTransactions.isNotEmpty()) {
                item { Text("Kasa İşlemleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2)) }
                items(cashTransactions.size) { index -> TransactionDetailCard(cashTransactions[index], currencySymbol) }
            }
            if (bankTransactions.isNotEmpty()) {
                item { Text("Banka İşlemleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00796B)) }
                items(bankTransactions.size) { index -> TransactionDetailCard(bankTransactions[index], currencySymbol) }
            }
            if (monthTransactions.isEmpty()) {
                item { Text("Bu ayda işlem yok", modifier = Modifier.padding(16.dp), color = Color.Gray) }
            }
        }
    }
}