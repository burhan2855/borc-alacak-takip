package com.burhan2855.borctakip.ui.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Contact
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel
import com.burhan2855.borctakip.ui.components.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactTransactionsScreen(
    contactId: Long,
    viewModel: MainViewModel,
    navController: NavController,
    onNavigateUp: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState(initial = emptyList())
    val contacts by viewModel.allContacts.collectAsState(initial = emptyList())
    val contact = contacts.find { it.id == contactId }
    val contactTransactions = transactions.filter { it.contactId?.toLong() == contactId }
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = contact?.name ?: stringResource(id = R.string.kisi_bulunamadi), 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A1B9A))
            )
        }
    ) { paddingValues ->
        if (contactTransactions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.bu_kisiyle_islem_yok),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contactTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onClick = { navController.navigate("detail/${transaction.id}") },
                        onEdit = { navController.navigate("detail/${transaction.id}") },
                        onDelete = { viewModel.delete(transaction) },
                        onMarkPaid = { viewModel.update(transaction.copy(status = "Ödendi")) },
                        onCashPayment = { navController.navigate("cashPayment/${transaction.id}") },
                        onBankPayment = { navController.navigate("bankPayment/${transaction.id}") }
                    )
                }
            }
        }
    }
}