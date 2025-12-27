package com.burhan2855.borctakip.ui.cash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashScreen(
    transactions: List<Transaction>,
    navController: NavController,
    currencySymbol: String,
    viewModel: MainViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.kasa_islemleri), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.geri),
                            tint = Color.White
                        )
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
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onClick = { navController.navigate("detail/${transaction.id}") },
                        onEdit = { navController.navigate("detail/${transaction.id}") },
                        onDelete = { viewModel.delete(it) },
                        onMarkPaid = { viewModel.processPayment(it, "Kasa") },
                        onCashPayment = { navController.navigate("cashPayment/${it.id}?isCashIn=false") },
                        onBankPayment = { navController.navigate("bankPayment/${it.id}?isBankIn=false") }
                    )
                }
            }
        }
    }
}