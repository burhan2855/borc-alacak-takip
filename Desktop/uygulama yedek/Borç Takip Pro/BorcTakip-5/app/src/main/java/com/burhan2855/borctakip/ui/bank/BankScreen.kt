package com.burhan2855.borctakip.ui.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.ui.components.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankScreen(
    transactions: List<Transaction>,
    navController: NavController,
    currencySymbol: String,
    viewModel: MainViewModel
) {
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
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Banka",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            stringResource(id = R.string.banka_islemleri),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri), tint = Color.White)
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
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onClick = { navController.navigate("detail/${transaction.id}") },
                        onEdit = { navController.navigate("detail/${transaction.id}") },
                        onDelete = { viewModel.delete(it) },
                        onMarkPaid = { viewModel.processPayment(it, "Banka") },
                        onCashPayment = { navController.navigate("cashPayment/${it.id}?isCashIn=false") },
                        onBankPayment = { navController.navigate("bankPayment/${it.id}?isBankIn=false") }
                    )
                }
            }
        }
    }
}