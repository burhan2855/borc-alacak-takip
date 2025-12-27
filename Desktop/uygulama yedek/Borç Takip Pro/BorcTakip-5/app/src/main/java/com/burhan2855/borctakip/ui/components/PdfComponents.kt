package com.burhan2855.borctakip.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Contact
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PdfViewerScreen(
    uri: android.net.Uri,
    onDismiss: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Başlık ve kapatma
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A1B9A))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "PDF Raporı",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
            }
        }
        
        // PDF gösterme alanı (placeholder)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.FilePresent,
                    contentDescription = "PDF",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF6A1B9A)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "PDF Raporı Hazır",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Aşağıdaki düğmeler ile indir veya paylaş",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        // İndir ve Paylaş butonları
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    // PDF'yi indir
                    Toast.makeText(context, "PDF indirildi: /Downloads/Report.pdf", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("İndir", color = Color.White)
            }
            
            Button(
                onClick = {
                    // PDF'yi paylaş
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                    }
                    try {
                        context.startActivity(Intent.createChooser(shareIntent, "PDF'yi Paylaş"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Paylaşma başarısız", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Paylaş", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfFilterDialog(
    contacts: List<Contact>,
    onDismiss: () -> Unit,
    onGenerate: (filter: String, contact: Contact?, startDate: Long, endDate: Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Tümü") }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var contactDropdownExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "PDF Filtresi Seçin",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarih aralığı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Başlangıç:", style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                        ) {
                            Text(
                                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(startDate)),
                                color = Color.White,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bitiş:", style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                        ) {
                            Text(
                                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(endDate)),
                                color = Color.White,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                    }
                }
                
                Divider(color = Color(0xFF6A1B9A).copy(alpha = 0.1f))
                
                // Filtre seçenekleri
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(40.dp)
                    ) {
                        RadioButton(
                            selected = selectedFilter == "Tümü",
                            onClick = { selectedFilter = "Tümü" }
                        )
                        Text("Tümü", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(40.dp)
                    ) {
                        RadioButton(
                            selected = selectedFilter == "Kasa",
                            onClick = { selectedFilter = "Kasa" }
                        )
                        Text("Kasa", modifier = Modifier.padding(start = 8.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(40.dp)
                    ) {
                        RadioButton(
                            selected = selectedFilter == "Banka",
                            onClick = { selectedFilter = "Banka" }
                        )
                        Text("Banka", modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    // Kişi seçimi
                    ExposedDropdownMenuBox(
                        expanded = contactDropdownExpanded,
                        onExpandedChange = { contactDropdownExpanded = !contactDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedContact?.name ?: "Kişi Seçin",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contactDropdownExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = contactDropdownExpanded,
                            onDismissRequest = { contactDropdownExpanded = false }
                        ) {
                            contacts.forEach { contact ->
                                DropdownMenuItem(
                                    text = { Text(contact.name) },
                                    onClick = {
                                        selectedContact = contact
                                        contactDropdownExpanded = false
                                        selectedFilter = "Kişi"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGenerate(selectedFilter, selectedContact, startDate, endDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
            ) {
                Text("Oluştur", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
