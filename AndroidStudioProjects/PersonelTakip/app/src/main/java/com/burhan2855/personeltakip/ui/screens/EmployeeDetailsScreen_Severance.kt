package com.burhan2855.personeltakip.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.burhan2855.personeltakip.data.Adjustment
import com.burhan2855.personeltakip.data.Employee
import com.burhan2855.personeltakip.logic.SalaryCalculator
import com.burhan2855.personeltakip.data.WorkLog
import com.burhan2855.personeltakip.logic.IndemnityReport

@Composable
fun SeveranceCalculationDialog(
    employee: Employee,
    workLogs: List<WorkLog>,
    adjustments: List<Adjustment>,
    onDismiss: () -> Unit
) {
    val report = remember(employee, workLogs, adjustments) {
        SalaryCalculator().calculateIndemnity(employee, workLogs, adjustments)
    }
    
    if (report == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Hata") },
            text = { Text("Tazminat hesaplanamadı. İşe başlama ve çıkış tarihleri kontrol edilmelidir.") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Tamam") }
            }
        )
        return
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("İşten Çıkış Tazminatı", fontWeight = FontWeight.Bold)
                IconButton(onClick = { /* PDF Export logic if needed on Android */ }) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFFE91E63))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Çalışma Süresi", fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                        Text("${report.years} Yıl ${report.months} Ay ${report.remainingDays} Gün", style = MaterialTheme.typography.titleMedium)
                        
                        val baseDays = (report.years * 365) + report.remainingDays
                        val leapDays = report.totalDaysWorked - baseDays
                        val leapText = if (leapDays > 0) " (+ $leapDays gün artık yıl)" else ""
                        
                        Text("Gün Bazında Toplam: (${report.years} x 365) + ${report.remainingDays} = ${report.totalDaysWorked} gün$leapText", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                // Daily Rates Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1F7))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Günlük Ücretler", fontWeight = FontWeight.Bold, color = Color(0xFF3949AB))
                        IndemnityRow("Net Maaş", report.dailyNetMaas)
                        IndemnityRow("Yemek", report.dailyNetYemek)
                        IndemnityRow("Yol", report.dailyNetYol)
                        HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Toplam Günlük", fontWeight = FontWeight.Bold)
                            Text("${String.format("%,.2f", report.totalDaily)} TL", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Calculation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC).copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Tazminat Hesabı", fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
                        IndemnityRow("Kıdem Tazminatı", report.kidemTazminati)
                        IndemnityRow("Kalan İzin (${report.remainingLeaveDays} gün)", report.remainingLeaveAmount)
                        IndemnityRow("Toplam Yemek", report.totalYemek, color = Color(0xFF3F51B5))
                        IndemnityRow("Toplam Yol", report.totalYol, color = Color(0xFF3F51B5))
                        IndemnityRow("Toplam Prim", report.totalPrim, color = Color(0xFF2E7D32))
                        IndemnityRow("Toplam Avanslar", -report.totalAvans, color = Color.Red)
                        IndemnityRow("Toplam Kesintiler", -report.totalKesinti, color = Color.Red)
                        
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("NET TAZMİNAT", fontWeight = FontWeight.ExtraBold, color = Color(0xFF3949AB))
                            Text("${String.format("%,.2f", report.netTazminat)} TL", fontWeight = FontWeight.ExtraBold, color = Color(0xFF3949AB))
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        IndemnityRow("Ödenen Tazminat", report.paidTazminat, color = Color(0xFF2E7D32))
                        
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("KALAN ÖDEME:", fontWeight = FontWeight.ExtraBold, color = Color.Red, style = MaterialTheme.typography.titleMedium)
                            Text("${String.format("%,.2f", report.remainingPayment)} TL", fontWeight = FontWeight.ExtraBold, color = Color.Red, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun IndemnityRow(label: String, amount: Double, color: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("${String.format("%,.2f", amount)} TL", color = color, fontWeight = FontWeight.Medium)
    }
}
