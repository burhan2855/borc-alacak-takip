package com.burhan2855.borctakip.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.ui.components.TransactionItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtTransactionsScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onNavigateUp: () -> Unit = { navController.navigateUp() }
) {
    val all by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currencySymbol = viewModel.currencySymbol.collectAsState(initial = "₺").value
    val debtTransactions = remember(all) {
        all.filter { tr ->
            // Borç işlemleri: sadece gerçek borç işlemleri, kasa/banka işlemleri hariç
            (tr.isDebt || (tr.category?.contains("borç", ignoreCase = true) == true) || (tr.paymentType?.contains("borç", ignoreCase = true) == true)) &&
            // Kasa ve Banka işlemlerini dışarıda bırak
            tr.category != "Kasa Girişi" && tr.category != "Kasa Çıkışı" &&
            tr.category != "Banka Girişi" && tr.category != "Banka Çıkışı"
        }.sortedBy { it.dueDate ?: it.date }
    }

    val scope = rememberCoroutineScope()
    var toDelete by remember { mutableStateOf<Transaction?>(null) }

    // Silme onayı
    toDelete?.let { tr ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text(stringResource(id = R.string.islemi_sil)) },
            text = { Text(stringResource(id = R.string.emin_misiniz)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.delete(tr); toDelete = null }
                }) { Text(stringResource(id = R.string.sil), color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text(stringResource(id = R.string.iptal)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.borclar), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A1B9A))
            )
        }
    ) { pad ->
        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF6A1B9A),
                Color(0xFF8E24AA),
                Color(0xFFF3E5F5)
            )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(pad)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(debtTransactions) { _, tr ->
                    TransactionItem(
                        transaction = tr,
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(tr) },
                        onEdit = { navController.navigate("detail/${tr.id}") },
                        onDelete = { t -> viewModel.delete(t) },
                        onMarkPaid = { t -> scope.launch { viewModel.update(t.copy(status = "Ödendi")) } },
                        onCashPayment = { t -> navController.navigate("cashPayment/${t.id}?isCashIn=false") },
                        onBankPayment = { t -> navController.navigate("bankPayment/${t.id}?isBankIn=false") }
                    )
                }
            }
        }
    }
}