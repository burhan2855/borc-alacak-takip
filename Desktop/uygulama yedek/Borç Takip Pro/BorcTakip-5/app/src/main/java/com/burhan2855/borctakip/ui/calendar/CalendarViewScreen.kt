package com.burhan2855.borctakip.ui.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.data.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarViewScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    
    // ViewModeldan işlemleri al
    val allTransactions by mainViewModel.allTransactions.collectAsState()
    
    // Takvimde gösterilecek işlemler
    // Kasa Çıkışı/Banka Çıkışı HARİÇ her şeyi göster (Borç, Alacak, Kasa Girişi, Banka Girişi vb.)
    val transactions = remember(allTransactions) {
        val filtered = allTransactions.filter { transaction ->
            // Kasa/Banka ÇIKIŞI işlemleri hariç (bunlar borç/alacak ödemesi değil)
            !(transaction.category == "Kasa Çıkışı" || transaction.category == "Banka Çıkışı")
        }
        Log.d("DB_DUMP", "===== CalendarViewScreen DEBUG =====")
        Log.d("DB_DUMP", "Total transactions: ${allTransactions.size}")
        Log.d("DB_DUMP", "After filtering: ${filtered.size}")
        filtered.forEach { transaction ->
            Log.d("DB_DUMP", "  ✓ Tx: title='${transaction.title}', category='${transaction.category}', isDebt=${transaction.isDebt}, status='${transaction.status}'")
        }
        Log.d("DB_DUMP", "===== END DEBUG =====")
        filtered
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takvim (Toplam: ${transactions.size} işlem)", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A1B9A))
            )
        }
    ) { paddingValues ->
        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6A1B9A),
                Color(0xFF8E24AA),
                Color(0xFFF3E5F5)
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Ay/Yıl Seçimi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { 
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }) {
                    Text("< Geri")
                }
                
                Text(
                    "${getMonthName(currentMonth)} $currentYear",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Button(onClick = { 
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }) {
                    Text("İleri >")
                }
            }
            
            // Takvim Görünümü
            CalendarGrid(
                month = currentMonth,
                year = currentYear,
                transactions = transactions
            )
            
            // Seçili gün için işlemler
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Bu ay işlemleri:",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (transactions.isEmpty()) {
                    item {
                        Text(
                            "Bu ay hiçbir işlem yok",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    items(transactions.size) { index ->
                        val transaction = transactions[index]
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = transaction.date ?: System.currentTimeMillis()
                        }
                        
                        if (cal.get(Calendar.MONTH) == currentMonth && 
                            cal.get(Calendar.YEAR) == currentYear) {
                            TransactionItem(transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    month: Int,
    year: Int,
    transactions: List<Transaction>
) {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val transactionsByDay = mutableMapOf<Int, Boolean>()
    transactions.forEach { trans ->
        // Vade tarihini kullan (dueDate), yoksa işlem tarihini (date)
        val dateToUse = trans.dueDate ?: trans.date ?: System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = dateToUse }
        if (cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year) {
            transactionsByDay[cal.get(Calendar.DAY_OF_MONTH)] = true
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Gün başlıkları
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dayNames = listOf("P", "S", "Ç", "P", "C", "C", "P")
            dayNames.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        
        // Takvim hücreleri
        var day = 1
        repeat((firstDayOfWeek + daysInMonth + 6) / 7) { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { dayOfWeek ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .background(
                                color = if (week == 0 && dayOfWeek < firstDayOfWeek) {
                                    Color.Transparent
                                } else if (day > daysInMonth) {
                                    Color.Transparent
                                } else {
                                    Color(0xFFF5F5F5)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek) {
                            // Boş hücre
                        } else if (day <= daysInMonth) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "$day",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                
                                // Bu gün için işlemleri göster
                                if (transactionsByDay.containsKey(day)) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color.Red, shape = RoundedCornerShape(3.dp))
                                            .padding(top = 2.dp)
                                    )
                                }
                            }
                            day++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
    val date = dateFormat.format(transaction.date ?: System.currentTimeMillis())
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    transaction.title,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Text(
                "${transaction.amount} ₺",
                style = MaterialTheme.typography.labelMedium,
                color = if (transaction.isDebt) Color.Red else Color.Green
            )
        }
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        0 -> "Ocak"
        1 -> "Şubat"
        2 -> "Mart"
        3 -> "Nisan"
        4 -> "Mayıs"
        5 -> "Haziran"
        6 -> "Temmuz"
        7 -> "Ağustos"
        8 -> "Eylül"
        9 -> "Ekim"
        10 -> "Kasım"
        11 -> "Aralık"
        else -> ""
    }
}