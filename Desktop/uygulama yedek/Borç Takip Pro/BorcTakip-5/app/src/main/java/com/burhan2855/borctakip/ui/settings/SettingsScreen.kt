package com.burhan2855.borctakip.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.burhan2855.borctakip.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedCurrencyCode: String,
    selectedLanguage: String,
    onNavigateToCurrencySelection: () -> Unit,
    onNavigateToLanguageSelection: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateUp: () -> Unit,
    onSignOut: (() -> Unit)? = null,
    userDisplayName: String? = null,
    userEmail: String? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.ayarlar)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.geri))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            
            // User Info Section (if signed in)
            if (userDisplayName != null || userEmail != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            userDisplayName?.let { name ->
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            userEmail?.let { email ->
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
            
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.para_birimi)) },
                supportingContent = { Text(selectedCurrencyCode) },
                modifier = Modifier.clickable(onClick = onNavigateToCurrencySelection)
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.dil)) },
                supportingContent = { Text(Locale.forLanguageTag(selectedLanguage).displayName) },
                modifier = Modifier.clickable(onClick = onNavigateToLanguageSelection)
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Takvim Entegrasyonu") },
                supportingContent = { Text("Ödeme hatırlatıcıları ve takvim ayarları") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable(onClick = onNavigateToCalendarSettings)
            )
            HorizontalDivider()
            
            // Sign Out Option (if signed in)
            onSignOut?.let { signOutAction ->
                var showSignOutDialog by remember { mutableStateOf(false) }
                
                ListItem(
                    headlineContent = { 
                        Text(
                            text = "Çıkış Yap",
                            color = Color.Red
                        ) 
                    },
                    supportingContent = { 
                        Text(
                            text = "Hesabından çıkış yap ve başka hesapla giriş yap",
                            color = Color.Red.copy(alpha = 0.7f)
                        ) 
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color.Red
                        )
                    },
                    modifier = Modifier.clickable { showSignOutDialog = true }
                )
                
                // Sign Out Confirmation Dialog
                if (showSignOutDialog) {
                    AlertDialog(
                        onDismissRequest = { showSignOutDialog = false },
                        title = { Text("Çıkış Yap") },
                        text = { Text("Hesabınızdan çıkış yapmak istediğinizden emin misiniz? Başka bir hesapla giriş yapabilirsiniz.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    signOutAction()
                                    showSignOutDialog = false
                                }
                            ) {
                                Text("Çıkış Yap", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSignOutDialog = false }) {
                                Text("İptal")
                            }
                        }
                    )
                }
            }
        }
    }
}
