package com.burhan2855.personeltakip.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.burhan2855.personeltakip.data.Employee
import com.burhan2855.personeltakip.ui.EmployeeViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeScreen(
    viewModel: EmployeeViewModel,
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var monthlySalary by remember { mutableStateOf("") }
    var monthlyAdditionalSalary by remember { mutableStateOf("") }
    var annualLeave by remember { mutableStateOf("0") }
    var mealAllowance by remember { mutableStateOf("0") }
    var transportAllowance by remember { mutableStateOf("0") }
    var dailyHours by remember { mutableStateOf("8.0") }
    var overtimeMultiplier by remember { mutableStateOf("1.5") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personel Ekle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profil Resmi Alanı
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Resim Seç", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Ad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Soyad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = position, onValueChange = { position = it }, label = { Text("Pozisyon") }, modifier = Modifier.fillMaxWidth())
            
            OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Telefon Numarası") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(value = iban, onValueChange = { iban = it.uppercase() }, label = { Text("IBAN") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            
            OutlinedTextField(
                value = monthlySalary, 
                onValueChange = { monthlySalary = it }, 
                label = { Text("Aylık Net Maaş (TL)") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = monthlyAdditionalSalary,
                onValueChange = { monthlyAdditionalSalary = it },
                label = { Text("Ek Maaş (TL)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = mealAllowance,
                    onValueChange = { mealAllowance = it },
                    label = { Text("Günlük Yemek (TL)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = transportAllowance,
                    onValueChange = { transportAllowance = it },
                    label = { Text("Günlük Yol (TL)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = annualLeave,
                onValueChange = { annualLeave = it },
                label = { Text("Yıllık İzin Hakkı (Gün)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = dailyHours, 
                onValueChange = { dailyHours = it }, 
                label = { Text("Günlük Çalışma Saati") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = overtimeMultiplier, 
                onValueChange = { overtimeMultiplier = it }, 
                label = { Text("Mesai Çarpanı (Örn: 1.5)") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // İşe Başlama Tarihi
            OutlinedTextField(
                value = startDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "",
                onValueChange = { },
                label = { Text("İşe Başlama Tarihi") },
                readOnly = true,
                trailingIcon = { 
                    IconButton(onClick = { showStartDatePicker = true }) { 
                        Icon(Icons.Default.CalendarToday, null) 
                    } 
                },
                modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true }
            )

            // İşten Çıkış Tarihi
            OutlinedTextField(
                value = endDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "",
                onValueChange = { },
                label = { Text("İşten Çıkış Tarihi (Opsiyonel)") },
                readOnly = true,
                trailingIcon = { 
                    IconButton(onClick = { showEndDatePicker = true }) { 
                        Icon(Icons.Default.CalendarToday, null) 
                    } 
                },
                modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val salary = monthlySalary.toDoubleOrNull() ?: 0.0
                    val additionalSalaryVal = monthlyAdditionalSalary.toDoubleOrNull() ?: 0.0
                    val annualLeaveVal = annualLeave.toIntOrNull() ?: 0
                    val mealVal = mealAllowance.toDoubleOrNull() ?: 0.0
                    val transportVal = transportAllowance.toDoubleOrNull() ?: 0.0
                    val hours = dailyHours.toDoubleOrNull() ?: 8.0
                    val multiplier = overtimeMultiplier.toDoubleOrNull() ?: 1.5
                    val calculatedHourlyRate = if (hours > 0) salary / (30 * hours) else 0.0

                    val internalImageUri = imageUri?.let { uri ->
                        com.burhan2855.personeltakip.util.ImageUtils.saveImageToInternalStorage(context, uri)
                    }

                    viewModel.addEmployee(
                        Employee(
                            firstName = firstName,
                            lastName = lastName,
                            position = position,
                            hourlyRate = calculatedHourlyRate,
                            dailyWorkingHours = hours,
                            overtimeMultiplier = multiplier,
                            additionalSalary = additionalSalaryVal,
                            annualLeaveEntitlement = annualLeaveVal,
                            mealAllowance = mealVal,
                            transportAllowance = transportVal,
                            imageUri = internalImageUri,
                            startDate = startDate,
                            endDate = endDate,
                            iban = iban,
                            phoneNumber = phoneNumber,
                            email = email,
                            address = address
                        )
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && monthlySalary.isNotBlank()
            ) {
                Text("Kaydet")
            }
        }

        // Start Date Picker Dialog
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showStartDatePicker = false
                    }) { Text("Tamam") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("İptal") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // End Date Picker Dialog
        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showEndDatePicker = false
                    }) { Text("Tamam") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("İptal") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
