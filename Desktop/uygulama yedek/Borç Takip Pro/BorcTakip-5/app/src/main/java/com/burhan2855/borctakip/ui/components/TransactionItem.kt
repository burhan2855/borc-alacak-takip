package com.burhan2855.borctakip.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import com.burhan2855.borctakip.R
import com.burhan2855.borctakip.data.Transaction

@Composable
fun TransactionItem(
    transaction: Transaction,
    currencySymbol: String,
    contactName: String? = null,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: (Transaction) -> Unit,
    onMarkPaid: (Transaction) -> Unit,
    onCashPayment: (Transaction) -> Unit,
    onBankPayment: (Transaction) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left pink circle with a static icon - tıklanabilir
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCE4EC))
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFFE91E63)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.title.ifEmpty { contactName ?: transaction.category ?: "" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Transaction Type Tag
                    val (tagText, tagBackgroundColor, tagTextColor) = getTransactionTypeDisplay(transaction)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tagBackgroundColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = tagText,
                            style = MaterialTheme.typography.labelSmall,
                            color = tagTextColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(transaction.dueDate ?: transaction.date ?: System.currentTimeMillis())),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                }

                // Amount Text - Kasa/Banka giriş işlemleri yeşil, çıkış ve borçlar kırmızı, alacaklar yeşil
                val amountColor = when {
                    // Kasa/Banka Girişi -> Yeşil
                    transaction.category == "Kasa Girişi" || transaction.category == "Banka Girişi" -> Color(0xFF388E3C)
                    // Kasa/Banka Çıkışı -> Kırmızı
                    transaction.category == "Kasa Çıkışı" || transaction.category == "Banka Çıkışı" -> Color(0xFFD32F2F)
                    // Normal işlemler: Borç -> Kırmızı, Alacak -> Yeşil
                    transaction.isDebt -> Color(0xFFD32F2F)
                    else -> Color(0xFF388E3C)
                }
                
                Text(
                    text = String.format(Locale.getDefault(), "%.2f %s", transaction.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Expand/Collapse Icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(id = R.string.daralt) else stringResource(id = R.string.genislet),
                    tint = Color.Gray,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }

            // Expandable details section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    // Action buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Only show payment buttons for debt/credit transactions, not for cash/bank flow transactions
                        val isCashBankTransaction = transaction.category?.let {
                            it == "Kasa Girişi" || it == "Kasa Çıkışı" ||
                            it == "Banka Girişi" || it == "Banka Çıkışı"
                        } ?: false
                        
                        if (!isCashBankTransaction && transaction.status != "Ödendi") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ModernActionButton(
                                    text = if (transaction.isDebt) "Kasadan Öde" else "Kasadan Tahsilat",
                                    icon = Icons.Default.Payment,
                                    backgroundColor = Color(0xFF4CAF50),
                                    onClick = { onCashPayment(transaction) },
                                    modifier = Modifier.weight(1f)
                                )

                                ModernActionButton(
                                    text = if (transaction.isDebt) "Bankadan Öde" else "Bankadan Tahsilat",
                                    icon = Icons.Default.Payment,
                                    backgroundColor = Color(0xFF2196F3),
                                    onClick = { onBankPayment(transaction) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModernActionButton(
                                text = stringResource(id = R.string.duzenle),
                                icon = Icons.Default.Edit,
                                backgroundColor = Color(0xFF2196F3),
                                onClick = onEdit,
                                modifier = Modifier.weight(1f)
                            )

                            ModernActionButton(
                                text = stringResource(id = R.string.sil),
                                icon = Icons.Default.Delete,
                                backgroundColor = Color(0xFFE53935),
                                onClick = { onDelete(transaction) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingPaymentItem(
    transaction: Transaction,
    currencySymbol: String,
    contactName: String? = null,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: (Transaction) -> Unit = {},
    onMarkPaid: () -> Unit = {},
    onPartialPayment: (Transaction) -> Unit = {},
    onCashPayment: (Transaction) -> Unit = {},
    onBankPayment: (Transaction) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // Use java.time.LocalDate to compute full-day difference (avoids timezone/time-of-day issues)
    val zone = ZoneId.systemDefault()
    val todayLocal = LocalDate.now(zone)
    val transactionDateMillis = transaction.dueDate ?: transaction.date ?: System.currentTimeMillis()
    val dueLocal = Instant.ofEpochMilli(transactionDateMillis).atZone(zone).toLocalDate()
    val daysRemaining = ChronoUnit.DAYS.between(todayLocal, dueLocal).toInt()
    
    // Urgency colors: treat 0 as today (urgent), negative as overdue
    val urgencyColor = when {
        daysRemaining > 7 -> Color(0xFF4CAF50)
        daysRemaining > 3 -> Color(0xFFFF9800)
        daysRemaining >= 0 -> Color(0xFFFF5722)
        else -> Color(0xFFE53935)
    }
    
    val urgencyBackgroundColor = when {
        daysRemaining > 7 -> Color(0xFFE8F5E8)
        daysRemaining > 3 -> Color(0xFFFFF3E0)
        daysRemaining >= 0 -> Color(0xFFFFE0B2)
        else -> Color(0xFFFFEBEE)
    }
    
    Card(
        modifier = Modifier.widthIn(max = 400.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        isExpanded = !isExpanded
                        onClick()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(urgencyBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (daysRemaining >= 0) "$daysRemaining" else "!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = urgencyColor
                        )
                        Text(
                            text = if (daysRemaining >= 0) stringResource(id = R.string.gun) else stringResource(id = R.string.gec),
                            style = MaterialTheme.typography.labelSmall,
                            color = urgencyColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.title.ifEmpty { contactName ?: transaction.category ?: "" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (contactName != null && transaction.title.isNotEmpty()) {
                        Text(
                            text = contactName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (transaction.isDebt) Color(0xFFFFCDD2) else Color(0xFFC8E6C9),
                            modifier = Modifier
                        ) {
                            Text(
                                text = if (transaction.isDebt) "Borç" else "Alacak",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (transaction.isDebt) Color(0xFFD32F2F) else Color(0xFF388E3C),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        if (transaction.category != null && (transaction.category.contains("Kasa") || transaction.category.contains("Banka"))) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (transaction.category.contains("Girişi")) Color(0xFFC8E6C9) else Color(0xFFFFCDD2),
                                modifier = Modifier
                            ) {
                                Text(
                                    text = if (transaction.category.contains("Girişi")) "Giriş" else "Çıkış",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (transaction.category.contains("Girişi")) Color(0xFF388E3C) else Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(transaction.dueDate ?: transaction.date ?: System.currentTimeMillis())),
                        style = MaterialTheme.typography.bodySmall,
                        color = urgencyColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val category = transaction.category ?: ""
                    val amountColor = when {
                        category.contains("Kasa Gir", ignoreCase = true) || 
                        category.contains("Banka Gir", ignoreCase = true) -> Color(0xFF388E3C) // Yeşil (giriş)
                        category.contains("Kasa Çık", ignoreCase = true) || 
                        category.contains("Banka Çık", ignoreCase = true) -> Color(0xFFD32F2F) // Kırmızı (çıkış)
                        else -> if (transaction.isDebt) Color(0xFFD32F2F) else Color(0xFF388E3C) // Borç kırmızı, Alacak yeşil
                    }
                    
                    android.util.Log.d("AMOUNT_COLOR_UPCOMING", "Title: ${transaction.title}, Category: '$category', isDebt: ${transaction.isDebt}, Color: ${if (amountColor == Color(0xFF388E3C)) "GREEN" else "RED"}")
                    
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f %s", transaction.amount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = urgencyColor,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = when {
                                daysRemaining > 7 -> stringResource(id = R.string.normal)
                                daysRemaining > 3 -> stringResource(id = R.string.yaklasiyor)
                                daysRemaining > 0 -> stringResource(id = R.string.acil)
                                else -> stringResource(id = R.string.gecikmis)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(id = R.string.daralt) else stringResource(id = R.string.genislet),
                    tint = urgencyColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(urgencyBackgroundColor.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.kalan_sure),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when {
                                    daysRemaining > 0 -> "$daysRemaining ${stringResource(id = R.string.gun)}"
                                    daysRemaining == 0 -> stringResource(id = R.string.bugun)
                                    else -> "${-daysRemaining} ${stringResource(id = R.string.gun_gecikmis)}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = urgencyColor
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(id = R.string.toplam_tutar_iki_nokta),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f %s", transaction.amount, currencySymbol),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (transaction.isDebt) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Only show payment buttons for debt/credit transactions, not for cash/bank flow transactions
                        val isCashBankTransaction = transaction.category?.let {
                            it == "Kasa Girişi" || it == "Kasa Çıkışı" ||
                            it == "Banka Girişi" || it == "Banka Çıkışı"
                        } ?: false
                        
                        if (!isCashBankTransaction && transaction.status != "Ödendi") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ModernActionButton(
                                    text = if (transaction.isDebt) "Kasadan Öde" else "Kasadan Tahsilat",
                                    icon = Icons.Default.Payment,
                                    backgroundColor = Color(0xFF4CAF50),
                                    onClick = { onCashPayment(transaction) },
                                    modifier = Modifier.weight(1f)
                                )
                                
                                ModernActionButton(
                                    text = if (transaction.isDebt) "Bankadan Öde" else "Bankadan Tahsilat",
                                    icon = Icons.Default.Payment,
                                    backgroundColor = Color(0xFF2196F3),
                                    onClick = { onBankPayment(transaction) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModernActionButton(
                                text = stringResource(id = R.string.duzenle),
                                icon = Icons.Default.Edit,
                                backgroundColor = Color(0xFF2196F3),
                                onClick = onEdit,
                                modifier = Modifier.weight(1f)
                            )
                            
                            ModernActionButton(
                                text = stringResource(id = R.string.sil),
                                icon = Icons.Default.Delete,
                                backgroundColor = Color(0xFFE53935),
                                onClick = { onDelete(transaction) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernActionButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Helper function to determine the display text, background color, and text color
 * based on transaction category
 */
private fun getTransactionTypeDisplay(transaction: Transaction): Triple<String, Color, Color> {
    return when (transaction.category) {
        "Kasa Girişi" -> Triple(
            "Kasa Giriş",
            Color(0xFFC8E6C9), // Light Green background
            Color(0xFF2E7D32)  // Dark Green text
        )
        "Kasa Çıkışı" -> Triple(
            "Kasa Çıkış",
            Color(0xFFFBE9E7), // Light Orange background
            Color(0xFFE65100)  // Dark Orange text
        )
        "Banka Girişi" -> Triple(
            "Banka Giriş",
            Color(0xFFC8E6C9), // Light Green background (yeşil)
            Color(0xFF2E7D32)  // Dark Green text (yeşil)
        )
        "Banka Çıkışı" -> Triple(
            "Banka Çıkış",
            Color(0xFFF3E5F5), // Light Purple background
            Color(0xFF6A1B9A)  // Dark Purple text
        )
        else -> Triple(
            if (transaction.isDebt) "Borç" else "Alacak",
            if (transaction.isDebt) Color(0xFFFFCDD2) else Color(0xFFC8E6C9), // Red or Green (yeşil)
            if (transaction.isDebt) Color(0xFFD32F2F) else Color(0xFF2E7D32)  // Dark red or green (yeşil)
        )
    }
}