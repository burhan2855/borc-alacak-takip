package com.burhan2855.borctakip.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.formatDate
import com.burhan2855.borctakip.ui.components.TransactionItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onNavigateUp: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currencySymbol = viewModel.currencySymbol.collectAsState(initial = "₺").value

    // Tüm işlemleri göster (borç/alacak + kasa/banka işlemleri)
    val filteredTransactions = transactions
    
    val sortedTransactions = filteredTransactions.sortedBy { it.dueDate ?: it.date }
    
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Transaction?>(null) }

    // Silme onay dialogu
    showDeleteDialog?.let { transaction ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(id = R.string.islemi_sil)) },
            text = { Text(stringResource(id = R.string.emin_misiniz)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.delete(transaction)
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.sil), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(id = R.string.iptal))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.tum_islemler), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri), tint = Color.White)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(transaction) },
                        onEdit = { navController.navigate("detail/${transaction.id}") },
                        onDelete = { t ->
                            viewModel.delete(t)
                        },
                        onMarkPaid = { t ->
                            coroutineScope.launch {
                                val updatedTransaction = t.copy(status = "Ödendi")
                                viewModel.update(updatedTransaction)
                            }
                        },
                        onCashPayment = { t -> navController.navigate("cashPayment/${t.id}?isCashIn=false") },
                        onBankPayment = { t -> navController.navigate("bankPayment/${t.id}?isBankIn=false") }
                    )
                }
            }
        }
    }
}