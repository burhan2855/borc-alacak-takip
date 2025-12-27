package com.burhan2855.borctakip.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.ui.components.TransactionItem
import com.burhan2855.borctakip.data.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyDetailScreen(
    month: String,
    transactions: List<Transaction>,
    currencySymbol: String,
    navController: NavController,
    onNavigateUp: () -> Unit
) {
    val filteredTransactions = transactions.filter {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.date ?: System.currentTimeMillis())) == month
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(month) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTransactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    currencySymbol = currencySymbol,
                    onClick = { navController.navigate("detail/${transaction.id}") },
                    onDelete = { },
                    onMarkPaid = { },
                    onCashPayment = { },
                    onBankPayment = { }
                )
            }
        }
    }
}
