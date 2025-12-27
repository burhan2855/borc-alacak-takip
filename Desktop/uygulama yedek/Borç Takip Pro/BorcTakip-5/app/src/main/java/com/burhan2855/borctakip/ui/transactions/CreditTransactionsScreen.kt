package com.burhan2855.borctakip.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
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
fun CreditTransactionsScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onTransactionClick: (Transaction) -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currencySymbol = viewModel.currencySymbol.collectAsState(initial = "₺").value
    val creditTransactions = allTransactions.filter { 
        !it.isDebt &&
        // Kasa ve Banka işlemlerini dışarıda bırak
        it.category != "Kasa Girişi" && it.category != "Kasa Çıkışı" &&
        it.category != "Banka Girişi" && it.category != "Banka Çıkışı"
    }.sortedBy { it.dueDate ?: it.date }
    val coroutineScope = rememberCoroutineScope()

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6A1B9A),
            Color(0xFF8E24AA),
            Color(0xFFF3E5F5)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.alacaklar), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A1B9A))
            )
        }
    ) { paddingValues ->
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
                items(creditTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(transaction) },
                        onEdit = { navController.navigate("detail/${transaction.id}") },
                        onDelete = { transactionToDelete ->
                            coroutineScope.launch {
                                viewModel.delete(transactionToDelete)
                            }
                        },
                        onMarkPaid = {},
                        onCashPayment = {},
                        onBankPayment = {}
                    )
                }
            }
        }
    }
}