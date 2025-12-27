package com.burhan2855.borctakip.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.ui.components.UpcomingPaymentItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingPaymentsScreen(
    viewModel: MainViewModel,
    navController: NavController,
    onTransactionClick: (Transaction) -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val currencySymbol = viewModel.currencySymbol.collectAsState(initial = "₺").value
    val upcomingPayments = allTransactions.filter { it.status != "Ödendi" && (it.dueDate ?: it.date ?: Long.MAX_VALUE) > System.currentTimeMillis() }.sortedBy { it.dueDate ?: it.date }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf<Transaction?>(null) }

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
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (upcomingPayments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = stringResource(id = R.string.yaklasan_odemeler),
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.yaklasan_odeme_bulunmuyor),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.yaklasan_odeme_bulunmuyor_aciklama),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(upcomingPayments) { transaction ->
                    UpcomingPaymentItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(transaction) },
                        onEdit = { navController.navigate("detail/${transaction.id}") },
                        onDelete = {
                            showDeleteDialog = transaction
                        },
                        onMarkPaid = {
                            coroutineScope.launch {
                                val updatedTransaction = transaction.copy(status = "Ödendi", amount = 0.0)
                                viewModel.update(updatedTransaction)
                            }
                        },
                        onCashPayment = {
                            navController.navigate("cashPayment/${it.id}")
                        },
                        onBankPayment = {
                            navController.navigate("bankPayment/${it.id}")
                        }
                    )
                }
            }
        }
    }
}