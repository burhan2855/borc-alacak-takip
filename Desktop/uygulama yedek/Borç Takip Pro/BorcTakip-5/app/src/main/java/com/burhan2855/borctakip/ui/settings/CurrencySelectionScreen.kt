package com.burhan2855.borctakip.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.R
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionScreen(
    onCurrencyChange: (String, String) -> Unit,
    onNavigateUp: () -> Unit
) {
    val allCurrencies = remember {
        Currency.getAvailableCurrencies().mapNotNull { 
            try {
                 CurrencyData(
                    code = it.currencyCode,
                    name = it.displayName,
                    symbol = it.symbol,
                    flag = getFlagEmojiForCurrency(it.currencyCode)
                )
            } catch (e: Exception) {
                null // Ignore currencies that cause issues
            }
        }.sortedBy { it.code }
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredCurrencies = allCurrencies.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.para_birimi)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )

            LazyColumn {
                items(filteredCurrencies) { currency ->
                    ListItem(
                        headlineContent = { Text(currency.code) },
                        supportingContent = { Text(currency.name) },
                        leadingContent = { Text(currency.flag, fontSize = MaterialTheme.typography.headlineMedium.fontSize) },
                        trailingContent = { Text(currency.symbol) },
                        modifier = Modifier.clickable { 
                            onCurrencyChange(currency.code, currency.symbol)
                            onNavigateUp()
                        }
                    )
                }
            }
        }
    }
}
