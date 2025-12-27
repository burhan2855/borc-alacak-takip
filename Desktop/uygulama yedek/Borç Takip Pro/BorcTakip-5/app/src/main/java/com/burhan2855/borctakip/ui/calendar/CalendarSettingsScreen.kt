package com.burhan2855.borctakip.ui.calendar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burhan2855.borctakip.DebtApplication
import com.burhan2855.borctakip.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DebtApplication
    val viewModel: CalendarSettingsViewModel = viewModel(
        factory = CalendarSettingsViewModel.Factory(application)
    )
    val uiState by viewModel.uiState.collectAsState()

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.takvim_ayarlari)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendar Integration Toggle
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.takvim_entegrasyonu),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(id = R.string.otomatik_hatirlatici))
                        Switch(
                            checked = uiState.autoCreateReminders,
                            onCheckedChange = { viewModel.updateAutoCreateReminders(it) }
                        )
                    }
                }
            }

            // Privacy Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.gizlilik_ayarlari),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(id = R.string.gizlilik_modu))
                            Text(
                                text = stringResource(id = R.string.gizlilik_modu_aciklama),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.privacyModeEnabled,
                            onCheckedChange = { viewModel.updatePrivacyMode(it) }
                        )
                    }
                }
            }

            // Reminder Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.hatirlatici_ayarlari),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Default Reminder Time
                    Text(stringResource(id = R.string.varsayilan_hatirlatici_zamani))
                    Spacer(modifier = Modifier.height(8.dp))

                    val reminderOptions = listOf(
                        15 to stringResource(id = R.string.on_bes_dakika_once),
                        60 to stringResource(id = R.string.bir_saat_once),
                        1440 to stringResource(id = R.string.bir_gun_once),
                        2880 to stringResource(id = R.string.iki_gun_once)
                    )

                    reminderOptions.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.defaultReminderMinutes == minutes,
                                onClick = { viewModel.updateDefaultReminderMinutes(minutes) }
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Follow-up Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.takip_ayarlari),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Follow-up Interval
                    Text(stringResource(id = R.string.ilk_takip_suresi))
                    Spacer(modifier = Modifier.height(8.dp))

                    val intervalOptions = listOf(
                        1 to stringResource(id = R.string.bir_gun),
                        3 to stringResource(id = R.string.uc_gun),
                        7 to stringResource(id = R.string.bir_hafta)
                    )

                    intervalOptions.forEach { (days, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.followUpIntervalDays == days,
                                onClick = { viewModel.updateFollowUpInterval(days) }
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Max Follow-up Count
                    Text(stringResource(id = R.string.maksimum_takip_sayisi))
                    Spacer(modifier = Modifier.height(8.dp))

                    val maxCountOptions = listOf(1, 2, 3, 5)

                    maxCountOptions.forEach { count ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.maxFollowUpCount == count,
                                onClick = { viewModel.updateMaxFollowUpCount(count) }
                            )
                            Text(
                                text = "$count ${stringResource(id = R.string.hatirlatici)}",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            // Permission Status
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.izinler),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(id = R.string.takvim_erisimi))
                            Text(
                                text = if (uiState.hasCalendarPermission) stringResource(id = R.string.verildi) else stringResource(id = R.string.verilmedi),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.hasCalendarPermission)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }

                        if (!uiState.hasCalendarPermission) {
                            Button(
                                onClick = {
                                    requestPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_CALENDAR,
                                            Manifest.permission.WRITE_CALENDAR
                                        )
                                    )
                                }
                            ) {
                                Text(stringResource(id = R.string.izin_ver))
                            }
                        }
                    }
                }
            }

            // Test Calendar Integration
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Takvim Entegrasyonu Testi",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Takvim entegrasyonunun çalışıp çalışmadığını test edin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.testCalendarIntegration() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.hasCalendarPermission && !uiState.isLoading
                    ) {
                        Text("Takvim Entegrasyonunu Test Et")
                    }
                }
            }

            // Sync Status
            if (uiState.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(stringResource(id = R.string.ayarlar_guncelleniyor))
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
