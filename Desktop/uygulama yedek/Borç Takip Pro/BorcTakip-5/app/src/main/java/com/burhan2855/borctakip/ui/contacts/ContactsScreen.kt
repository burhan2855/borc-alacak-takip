package com.burhan2855.borctakip.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Contact
import com.burhan2855.borctakip.data.Transaction
import com.burhan2855.borctakip.ui.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    transactions: List<Transaction>,
    contacts: List<Contact>,
    onNavigateUp: () -> Unit,
    onNavigateToContactDetail: (Long) -> Unit,
    viewModel: MainViewModel
) {
    var showDeleteDialog by remember { mutableStateOf<Contact?>(null) }
    var showEditDialog by remember { mutableStateOf<Contact?>(null) }
    val scope = rememberCoroutineScope()

    // Silme onay dialogu
    showDeleteDialog?.let { contact ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Kişiyi Sil") },
            text = { Text("${contact.name} kişisini silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteContact(contact)
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

    // Düzenleme dialogu
    showEditDialog?.let { contact ->
        var editedName by remember { mutableStateOf(contact.name) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Kişiyi Düzenle") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Kişi Adı") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            scope.launch {
                                viewModel.updateContact(contact.copy(name = editedName))
                                showEditDialog = null
                            }
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.kaydet))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text(stringResource(id = R.string.iptal))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.kisiler), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A)
                )
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
            if (contacts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.henuz_kisi_yok),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts) { contact ->
                        ContactCard(
                            contact = contact,
                            transactions = transactions,
                            onEdit = { showEditDialog = contact },
                            onDelete = { showDeleteDialog = contact },
                            onClick = { 
                                // Kişiye ait işlemlere git (isterseniz detay sayfası ekleyebiliriz)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    transactions: List<Transaction>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    // Kişiye ait işlemleri hesapla
    val contactTransactions = transactions.filter { it.contactId?.toLong() == contact.id }
    val balance = contactTransactions.sumOf { 
        if (it.isDebt) -it.amount else it.amount 
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kişi ikonu
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6A1B9A).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF6A1B9A),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Kişi bilgileri
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${contactTransactions.size} işlem",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (balance != 0.0) {
                    Text(
                        text = "Bakiye: ${String.format("%.0f", balance)} ₺",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (balance > 0) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                }
            }
            
            // Düzenle butonu
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Düzenle",
                    tint = Color(0xFF2196F3)
                )
            }
            
            // Sil butonu
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color(0xFFE53935)
                )
            }
        }
    }
}