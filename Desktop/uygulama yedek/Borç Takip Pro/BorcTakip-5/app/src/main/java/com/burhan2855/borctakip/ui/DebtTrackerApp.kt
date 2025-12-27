package com.burhan2855.borctakip.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.burhan2855.borctakip.data.Transaction
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.burhan2855.borctakip.ui.theme.BorcTakipTheme
import androidx.compose.foundation.clickable
import com.burhan2855.borctakip.ui.components.UpcomingPaymentItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtTrackerApp(
    transactions: List<Transaction>,
    currencySymbol: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    var showAddMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Kasa ve Banka işlemleri - sadece nakit akışı
    val cashTransactions = transactions.filter { 
        it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" 
    }
    val bankTransactions = transactions.filter { 
        it.category == "Banka Girişi" || it.category == "Banka Çıkışı" 
    }
    
    // Borç işlemleri - Kasa/Banka işlemleri HARİÇ
    val debtTransactions = transactions.filter { 
        it.isDebt && 
        it.category != "Kasa Girişi" && 
        it.category != "Kasa Çıkışı" &&
        it.category != "Banka Girişi" &&
        it.category != "Banka Çıkışı"
    }
    
    // Alacak işlemleri - Kasa/Banka işlemleri HARİÇ
    val creditTransactions = transactions.filter { 
        !it.isDebt && 
        it.category != "Kasa Girişi" && 
        it.category != "Kasa Çıkışı" &&
        it.category != "Banka Girişi" &&
        it.category != "Banka Çıkışı"
    }

    // Bakiye hesaplamaları
    val cashTotal = cashTransactions.sumOf { 
        if (it.category == "Kasa Girişi") it.amount else -it.amount 
    }
    val bankTotal = bankTransactions.sumOf { 
        if (it.category == "Banka Girişi") it.amount else -it.amount 
    }
    
    // Borç/Alacak toplamları (sadece ödenmemiş olanlar)
    val debtTotal = debtTransactions.filter { it.status != "Ödendi" }.sumOf { it.amount }
    val creditTotal = creditTransactions.filter { it.status != "Ödendi" }.sumOf { it.amount }
    
    // Net bakiye = Alacak - Borç + Kasa + Banka
    val netTotal = creditTotal - debtTotal + cashTotal + bankTotal
    
    // Bugün ve yarın aralığını hesapla (bugünün 00:00:00.000 - yarının 23:59:59.999)
    val startCalendar = Calendar.getInstance()
    startCalendar.set(Calendar.HOUR_OF_DAY, 0)
    startCalendar.set(Calendar.MINUTE, 0)
    startCalendar.set(Calendar.SECOND, 0)
    startCalendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = startCalendar.timeInMillis
    val endOfTomorrow = startOfToday + TimeUnit.DAYS.toMillis(2) - 1

    val upcomingPaymentsTwoDays = transactions.filter {
        val isCashOrBank = it.category == "Kasa Girişi" || it.category == "Kasa Çıkışı" ||
                it.category == "Banka Girişi" || it.category == "Banka Çıkışı"
        val due = it.dueDate ?: 0
        it.status != "Ödendi" && !isCashOrBank &&
                due in startOfToday..endOfTomorrow
    }.sortedBy { it.dueDate }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Finansal Takip",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                actions = {
                    IconButton(onClick = { navController.navigate("report_screen") }) {
                        Icon(
                            Icons.Default.Assessment, 
                            contentDescription = "Raporlar",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { navController.navigate("settings_screen") }) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = "Ayarlar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    showAddMenu = true
                },
                containerColor = Color(0xFF6A1B9A),
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFF6A1B9A).copy(alpha = 0.3f),
                        spotColor = Color(0xFF6A1B9A).copy(alpha = 0.3f)
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "İşlem Ekle",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        // İki sütunlu + menü
        if (showAddMenu) {
            ModalBottomSheet(onDismissRequest = { showAddMenu = false }) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ActionTile(
                            title = "Borç Ekle",
                            icon = Icons.Default.KeyboardArrowDown,
                            onClick = {
                                showAddMenu = false
                                navController.navigate("add/true")
                            }
                        )
                    }
                    item {
                        ActionTile(
                            title = "Alacak Ekle",
                            icon = Icons.Default.KeyboardArrowUp,
                            onClick = {
                                showAddMenu = false
                                navController.navigate("add/false")
                            }
                        )
                    }
                    item {
                        ActionTile(
                            title = "Kasa Girişi",
                            icon = Icons.Default.AccountBalanceWallet,
                            onClick = {
                                showAddMenu = false
                                navController.navigate("add_cash_transaction/true")
                            }
                        )
                    }
                    item {
                        ActionTile(
                            title = "Kasa Çıkışı",
                            icon = Icons.Default.AccountBalanceWallet,
                            onClick = {
                                showAddMenu = false
                                navController.navigate("add_cash_transaction/false")
                            }
                        )
                    }
                    item {
                        ActionTile(
                            title = "Banka Girişi",
                            icon = Icons.Default.AccountBalance,
                            onClick = {
                                showAddMenu = false
                                navController.navigate("add_bank_transaction/true")
                            }
                        )
                    }
                    item {
                        ActionTile(
                            title = "Banka Çıkışı",
                            icon = Icons.Default.AccountBalance,
                            onClick = {
                                showAddMenu = false
                                navController.navigate("add_bank_transaction/false")
                            }
                        )
                    }
                    item {
                        ActionTile(
                            title = "Kişi Ekle",
                            icon = Icons.Default.PersonAdd,
                            onClick = {
                                showAddMenu = false
                                navController.navigate("add_contact")
                            }
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A),
                            Color(0xFF8E24AA),
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                NonClickableSummaryCard(
                    title = "Kasa",
                    amount = cashTotal,
                    currencySymbol = currencySymbol,
                    backgroundColor = Color(0xFFFFB74D),
                    icon = Icons.Default.AccountBalance
                )
            }
            
            item {
                NonClickableSummaryCard(
                    title = "Banka",
                    amount = bankTotal,
                    currencySymbol = currencySymbol,
                    backgroundColor = Color(0xFF64B5F6),
                    icon = Icons.Default.AccountBalance
                )
            }
            
            item {
                NonClickableSummaryCard(
                    title = "Borç",
                    amount = debtTotal,
                    currencySymbol = currencySymbol,
                    backgroundColor = Color(0xFFE57373),
                    icon = Icons.Default.KeyboardArrowDown
                )
            }
            
            item {
                NonClickableSummaryCard(
                    title = "Alacak",
                    amount = creditTotal,
                    currencySymbol = currencySymbol,
                    backgroundColor = Color(0xFF81C784),
                    icon = Icons.Default.KeyboardArrowUp
                )
            }
            
            item {
                NonClickableSummaryCard(
                    title = "Net",
                    amount = netTotal,
                    currencySymbol = currencySymbol,
                    backgroundColor = Color(0xFF9C27B0),
                    icon = Icons.Default.AttachMoney
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChipButton(
                        text = "Tümü",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("all_transactions_screen") }
                    )
                    ChipButton(
                        text = "Borçlar",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("debt_transactions_screen") }
                    )
                    ChipButton(
                        text = "Alacaklar",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("credit_transactions_screen") }
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChipButton(
                        text = "Kişiler",
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("contacts") }
                    )
                    ChipButton(
                        text = "Kasa",
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("cash_screen") }
                    )
                    ChipButton(
                        text = "Banka",
                        icon = Icons.Default.AccountBalance,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("bank_screen") }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChipButton(
                        text = "Takvim",
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("calendar_screen") }
                    )
                    ChipButton(
                        text = "Yaklaşan Ödemeler",
                        icon = Icons.Default.Notifications,
                        modifier = Modifier.weight(1.5f),
                        onClick = { navController.navigate("upcoming_payments_screen") }
                    )
                }
            }
            if (upcomingPaymentsTwoDays.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Bugün ve Yarın Ödenecekler",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        upcomingPaymentsTwoDays.forEach { transaction ->
                            UpcomingPaymentItem(
                                transaction = transaction,
                                currencySymbol = currencySymbol,
                                onClick = { navController.navigate("detail/${transaction.id}") },
                                onEdit = { navController.navigate("detail/${transaction.id}") },
                                onDelete = { viewModel.delete(it) },
                                onMarkPaid = {
                                    coroutineScope.launch {
                                        viewModel.update(transaction.copy(status = "Ödendi"))
                                    }
                                },
                                onCashPayment = { navController.navigate("cashPayment/${it.id}?isCashIn=${!it.isDebt}") },
                                onBankPayment = { navController.navigate("bankPayment/${it.id}?isBankIn=${!it.isDebt}") }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    backgroundColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${String.format("%.0f", amount)} $currencySymbol",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun NonClickableSummaryCard(
    title: String,
    amount: Double,
    currencySymbol: String,
    backgroundColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = backgroundColor.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.1f),
                            backgroundColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        backgroundColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF424242),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                
                Text(
                    text = "${String.format(Locale("tr", "TR"), "%.0f", amount)} $currencySymbol",
                    color = backgroundColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Trend",
                tint = backgroundColor.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ChipButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(26.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFF8BBD9).copy(alpha = 0.3f),
                            Color(0xFFE1BEE7).copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            Color(0xFF6A1B9A).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = it,
                        contentDescription = text,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF6A1B9A)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF424242)
            )
        }
    }
}

@Composable
private fun FloatingActionButtonWithLabel(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ModernFloatingActionButtonWithLabel(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = containerColor.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .background(
                        containerColor.copy(alpha = 0.05f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            containerColor.copy(alpha = 0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = containerColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Text(
                    text = label,
                    color = Color(0xFF2C2C2C),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            modifier = Modifier
                .size(40.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = containerColor.copy(alpha = 0.3f)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BorcTakipTheme {
        // DebtTrackerApp(
        //     transactions = emptyList(),
        //     currencySymbol = "₺",
        //     navController = rememberNavController(),
        //     viewModel = viewModel()
        // )
    }
}

@Composable
private fun ActionTile(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF6A1B9A).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Color(0xFF6A1B9A))
            }
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}