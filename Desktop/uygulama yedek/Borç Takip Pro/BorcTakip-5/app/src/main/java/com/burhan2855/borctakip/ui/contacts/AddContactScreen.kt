package com.burhan2855.borctakip.ui.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    onSave: (Contact) -> Unit,
    onNavigateUp: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.yeni_kisi_adi)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text(stringResource(id = R.string.yeni_kisi_adi)) },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                singleLine = true
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(Contact(name = name))
                    } else {
                        nameError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.kaydet))
            }
        }
    }
}
