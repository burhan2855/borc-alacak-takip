package com.burhan2855.personeltakip.shared.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burhan2855.personeltakip.shared.data.Adjustment
import com.burhan2855.personeltakip.shared.data.AdjustmentType
import com.burhan2855.personeltakip.shared.data.WorkLog
import com.burhan2855.personeltakip.shared.data.Employee
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedWorkLogDialog(
    initialLog: WorkLog?,
    onDismiss: () -> Unit,
    onConfirm: (WorkLog) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var date by remember { mutableStateOf(initialLog?.date ?: today) }
    var startTimeStr by remember { mutableStateOf(initialLog?.startTime?.let { "${it.hour.pad()}:${it.minute.pad()}" } ?: "08:00") }
    var endTimeStr by remember { mutableStateOf(initialLog?.endTime?.let { "${it.hour.pad()}:${it.minute.pad()}" } ?: "18:00") }
    var isLeave by remember { mutableStateOf(initialLog?.isOnLeave ?: false) }
    var leaveType by remember { mutableStateOf(initialLog?.leaveType ?: "Ücretli İzin") }
    var hasMeal by remember { mutableStateOf(initialLog?.hasMeal ?: true) }
    var hasTransport by remember { mutableStateOf(initialLog?.hasTransport ?: true) }
    var showDatePicker by remember { mutableStateOf(false) }

    val leaveTypes = listOf("Ücretli İzin", "Ücretsiz İzin", "Raporlu", "Yıllık İzin", "Hafta Sonu", "Resmi Tatil")

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialLog == null) "Puantaj Kaydı Ekle" else "Kaydı Düzenle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = "${date.dayOfMonth}.${date.monthNumber}.${date.year}",
                    onValueChange = { },
                    label = { Text("Tarih") },
                    readOnly = true,
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLeave, onCheckedChange = { isLeave = it })
                    Text("İzinli Gün")
                }
                
                if (isLeave) {
                    Text("İzin Türü:", style = MaterialTheme.typography.labelLarge)
                    leaveTypes.forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = leaveType == type, onClick = { leaveType = type })
                            Text(type, modifier = Modifier.clickable { leaveType = type })
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = startTimeStr,
                        onValueChange = { if(it.length <= 5) startTimeStr = it },
                        label = { Text("Giriş Saati (Örn: 08:30)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = endTimeStr,
                        onValueChange = { if(it.length <= 5) endTimeStr = it },
                        label = { Text("Çıkış Saati (Örn: 18:00)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasMeal, onCheckedChange = { hasMeal = it })
                        Text("Yemek Yardımı")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasTransport, onCheckedChange = { hasTransport = it })
                        Text("Yol Yardımı")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    val start = if (isLeave) null else parseTime(date, startTimeStr)
                    val end = if (isLeave) null else parseTime(date, endTimeStr)
                    onConfirm(WorkLog(
                        id = initialLog?.id ?: 0,
                        employeeId = initialLog?.employeeId ?: 0,
                        date = date,
                        startTime = start,
                        endTime = end,
                        isOnLeave = isLeave,
                        leaveType = if(isLeave) leaveType else null,
                        hasMeal = hasMeal,
                        hasTransport = hasTransport
                    ))
                } catch (e: Exception) {
                }
            }) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedAdjustmentDialog(
    initialAdjustment: Adjustment?,
    onDismiss: () -> Unit,
    onConfirm: (Adjustment) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var date by remember { mutableStateOf(initialAdjustment?.date ?: today) }
    var amountStr by remember { mutableStateOf(initialAdjustment?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(initialAdjustment?.type ?: AdjustmentType.AVANS) }
    var description by remember { mutableStateOf(initialAdjustment?.description ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                    }
                    showDatePicker = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("İptal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAdjustment == null) "Ödeme/Kesinti Ekle" else "Kaydı Düzenle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = "${date.dayOfMonth}.${date.monthNumber}.${date.year}",
                    onValueChange = { },
                    label = { Text("Tarih") },
                    readOnly = true,
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
                
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Tutar (TL)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                Text("Tür:", style = MaterialTheme.typography.labelLarge)
                Column {
                    AdjustmentType.values().forEach { adjType ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = type == adjType, onClick = { type = adjType })
                            Text(adjType.name, modifier = Modifier.clickable { type = adjType })
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama (Opsiyonel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                onConfirm(Adjustment(
                    id = initialAdjustment?.id ?: 0,
                    employeeId = initialAdjustment?.employeeId ?: 0,
                    date = date,
                    amount = amount,
                    type = type,
                    description = description.ifBlank { null }
                ))
            }) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedEmployeeDialog(
    initialEmployee: Employee? = null,
    onDismiss: () -> Unit,
    onConfirm: (Employee) -> Unit
) {
    var firstName by remember { mutableStateOf(initialEmployee?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialEmployee?.lastName ?: "") }
    var position by remember { mutableStateOf(initialEmployee?.position ?: "") }
    
    // Monthly Net Salary calculation
    val initialMonthlySalary = initialEmployee?.let { (it.hourlyRate * 30 * it.dailyWorkingHours) }?.let { if(it > 0) it.toString() else "" } ?: ""
    var monthlySalary by remember { mutableStateOf(initialMonthlySalary) }
    var dailyHours by remember { mutableStateOf(initialEmployee?.dailyWorkingHours?.toString() ?: "8.0") }
    var overtimeMultiplier by remember { mutableStateOf(initialEmployee?.overtimeMultiplier?.toString() ?: "1.5") }
    
    var addSalary by remember { mutableStateOf(initialEmployee?.additionalSalary?.toString() ?: "0.0") }
    var annualLeave by remember { mutableStateOf(initialEmployee?.annualLeaveEntitlement?.toString() ?: "0") }
    var mealAllowance by remember { mutableStateOf(initialEmployee?.mealAllowance?.toString() ?: "0") }
    var transportAllowance by remember { mutableStateOf(initialEmployee?.transportAllowance?.toString() ?: "0") }
    
    var phoneNumber by remember { mutableStateOf(initialEmployee?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(initialEmployee?.email ?: "") }
    var iban by remember { mutableStateOf(initialEmployee?.iban ?: "") }
    var address by remember { mutableStateOf(initialEmployee?.address ?: "") }
    
    var startDate by remember { mutableStateOf(initialEmployee?.startDate) }
    var endDate by remember { mutableStateOf(initialEmployee?.endDate) }
    
    var showError by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
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

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEmployee == null) "Yeni Personel Ekle" else "Personel Bilgilerini Düzenle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Text("Lütfen Ad, Soyad ve Aylik Maasi doldurun!", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
                
                OutlinedTextField(
                    value = firstName, 
                    onValueChange = { firstName = it; if(it.isNotBlank()) showError = false }, 
                    label = { Text("Ad *") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && firstName.isBlank()
                )
                OutlinedTextField(
                    value = lastName, 
                    onValueChange = { lastName = it; if(it.isNotBlank()) showError = false }, 
                    label = { Text("Soyad *") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && lastName.isBlank()
                )
                OutlinedTextField(value = position, onValueChange = { position = it }, label = { Text("Pozisyon/Görevi") }, modifier = Modifier.fillMaxWidth())
                
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Telefon Numarası") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                OutlinedTextField(value = iban, onValueChange = { iban = it.uppercase() }, label = { Text("IBAN") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                OutlinedTextField(
                    value = monthlySalary, 
                    onValueChange = { monthlySalary = it; if(it.isNotBlank()) showError = false }, 
                    label = { Text("Aylık Net Maaş (TL) *") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showError && monthlySalary.isBlank()
                )

                OutlinedTextField(
                    value = addSalary, 
                    onValueChange = { addSalary = it }, 
                    label = { Text("Ek Maaş (Aylık)") }, 
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dailyHours, 
                        onValueChange = { dailyHours = it }, 
                        label = { Text("Günlük Saat") }, 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = overtimeMultiplier, 
                        onValueChange = { overtimeMultiplier = it }, 
                        label = { Text("Mesai Çarpanı") }, 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = startDate?.let { "${it.dayOfMonth}.${it.monthNumber}.${it.year}" } ?: "",
                    onValueChange = { },
                    label = { Text("İşe Başlama Tarihi") },
                    readOnly = true,
                    trailingIcon = { IconButton(onClick = { showStartDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true }
                )

                OutlinedTextField(
                    value = endDate?.let { "${it.dayOfMonth}.${it.monthNumber}.${it.year}" } ?: "",
                    onValueChange = { },
                    label = { Text("İşten Çıkış Tarihi (Opsiyonel)") },
                    readOnly = true,
                    trailingIcon = { IconButton(onClick = { showEndDatePicker = true }) { Icon(Icons.Default.CalendarToday, null) } },
                    modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && monthlySalary.isNotBlank()) {
                        val mSalary = monthlySalary.toDoubleOrNull() ?: 0.0
                        val dHours = dailyHours.toDoubleOrNull() ?: 8.0
                        val calculatedHourlyRate = if (dHours > 0) mSalary / (30 * dHours) else 0.0
                        
                        onConfirm(Employee(
                            id = initialEmployee?.id ?: 0,
                            firstName = firstName,
                            lastName = lastName,
                            position = position,
                            hourlyRate = calculatedHourlyRate,
                            dailyWorkingHours = dHours,
                            overtimeMultiplier = overtimeMultiplier.toDoubleOrNull() ?: 1.5,
                            additionalSalary = addSalary.toDoubleOrNull() ?: 0.0,
                            annualLeaveEntitlement = annualLeave.toIntOrNull() ?: 0,
                            mealAllowance = mealAllowance.toDoubleOrNull() ?: 0.0,
                            transportAllowance = transportAllowance.toDoubleOrNull() ?: 0.0,
                            phoneNumber = phoneNumber,
                            email = email,
                            iban = iban,
                            address = address,
                            startDate = startDate,
                            endDate = endDate
                        ))
                    } else {
                        showError = true
                    }
                }
            ) { Text("Kaydet") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

fun Int.pad() = if (this < 10) "0$this" else this.toString()

fun parseTime(date: LocalDate, timeStr: String): LocalDateTime {
    val parts = timeStr.split(":")
    val hour = parts[0].toInt()
    val min = parts.getOrNull(1)?.toInt() ?: 0
    return LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, min)
}
