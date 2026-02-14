package com.burhan2855.personeltakip.shared.adapters

import com.burhan2855.personeltakip.shared.data.Employee
import com.burhan2855.personeltakip.shared.data.WorkLog
import com.burhan2855.personeltakip.shared.data.Adjustment
import com.burhan2855.personeltakip.shared.logic.IEmployeeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.*
import kotlin.js.*

@kotlinx.serialization.Serializable
data class BackupData(
    val employees: List<Employee>,
    val logs: List<WorkLog>,
    val adjustments: List<Adjustment>
)

class WebEmployeeService : IEmployeeService {
    private val employeeKey = "pt_v2_employees"
    private val logKey = "pt_v2_logs"
    private val adjustmentKey = "pt_v2_adjustments"

    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
    }

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    private val _logs = MutableStateFlow<List<WorkLog>>(emptyList())
    private val _adjustments = MutableStateFlow<List<Adjustment>>(emptyList())

    init {
        try {
            println("WebEmployeeService: Başlatılıyor...")
            _employees.value = loadEmployees()
            _logs.value = loadLogs()
            _adjustments.value = loadAdjustments()
            println("WebEmployeeService: Yüklendi. Personel: ${_employees.value.size}, Log: ${_logs.value.size}")
        } catch (e: Exception) {
            println("WebEmployeeService: Başlatma hatası: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getAllEmployees(): Flow<List<Employee>> = _employees.asStateFlow()
    
    override fun getEmployeeById(id: Int): Flow<Employee?> = _employees.asStateFlow().map { list ->
        list.find { it.id == id }
    }

    override suspend fun insertEmployee(employee: Employee) {
        val current = _employees.value.toMutableList()
        val newId = if (current.isEmpty()) 1 else (current.maxOf { it.id } + 1)
        val newEmp = employee.copy(id = newId)
        current.add(newEmp)
        _employees.value = current
        saveEmployees(current)
    }

    override suspend fun updateEmployee(employee: Employee) {
        val current = _employees.value.toMutableList()
        val index = current.indexOfFirst { it.id == employee.id }
        if (index != -1) {
            current[index] = employee
            _employees.value = current
            saveEmployees(current)
        }
    }

    override suspend fun deleteEmployee(employee: Employee) {
        val current = _employees.value.filter { it.id != employee.id }
        _employees.value = current
        saveEmployees(current)
    }

    override suspend fun insertWorkLog(workLog: WorkLog) {
        val current = _logs.value.toMutableList()
        val newId = if (current.isEmpty()) 1 else (current.maxOf { it.id } + 1)
        current.add(workLog.copy(id = newId))
        _logs.value = current
        saveLogs(current)
    }

    override suspend fun updateWorkLog(workLog: WorkLog) {
        val current = _logs.value.toMutableList()
        val index = current.indexOfFirst { it.id == workLog.id }
        if (index != -1) {
            current[index] = workLog
            _logs.value = current
            saveLogs(current)
        }
    }

    override suspend fun deleteWorkLog(workLog: WorkLog) {
        val current = _logs.value.filter { it.id != workLog.id }
        _logs.value = current
        saveLogs(current)
    }

    override fun getWorkLogsForEmployee(employeeId: Int): Flow<List<WorkLog>> = _logs.asStateFlow().map { list ->
        list.filter { it.employeeId == employeeId }
    }

    override suspend fun insertAdjustment(adjustment: Adjustment) {
        val current = _adjustments.value.toMutableList()
        val newId = if (current.isEmpty()) 1 else (current.maxOf { it.id } + 1)
        current.add(adjustment.copy(id = newId))
        _adjustments.value = current
        saveAdjustments(current)
    }

    override suspend fun updateAdjustment(adjustment: Adjustment) {
        val current = _adjustments.value.toMutableList()
        val index = current.indexOfFirst { it.id == adjustment.id }
        if (index != -1) {
            current[index] = adjustment
            _adjustments.value = current
            saveAdjustments(current)
        }
    }

    override suspend fun deleteAdjustment(adjustment: Adjustment) {
        val current = _adjustments.value.filter { it.id != adjustment.id }
        _adjustments.value = current
        saveAdjustments(current)
    }

    override fun getAdjustmentsForEmployee(employeeId: Int): Flow<List<Adjustment>> = _adjustments.asStateFlow().map { list ->
        list.filter { it.employeeId == employeeId }
    }

    private fun loadEmployees(): List<Employee> {
        val data = storageGetItem(employeeKey)
        if (data == null || data == "null" || data.isEmpty()) return emptyList()
        return try { 
            val list = json.decodeFromString<List<Employee>>(data)
            println("WebEmployeeService: Loaded ${list.size} employees")
            list
        } catch (e: Exception) { 
            println("WebEmployeeService: Error loading employees: ${e.message}")
            e.printStackTrace()
            emptyList() 
        }
    }

    private fun saveEmployees(list: List<Employee>) {
        try { 
            val str = json.encodeToString(list)
            storageSetItem(employeeKey, str)
            println("WebEmployeeService: Saved ${list.size} employees")
        } catch (e: Exception) {
            println("WebEmployeeService: Error saving employees: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadLogs(): List<WorkLog> {
        val data = storageGetItem(logKey)
        if (data == null || data == "null" || data.isEmpty()) return emptyList()
        return try { 
            json.decodeFromString(data)
        } catch (e: Exception) { 
            println("WebEmployeeService: Error loading logs: ${e.message}")
            e.printStackTrace()
            emptyList() 
        }
    }

    private fun saveLogs(list: List<WorkLog>) {
        try { storageSetItem(logKey, json.encodeToString(list)) } catch (e: Exception) {
            println("WebEmployeeService: Error saving logs: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun loadAdjustments(): List<Adjustment> {
        val data = storageGetItem(adjustmentKey)
        if (data == null || data == "null" || data.isEmpty()) return emptyList()
        return try { 
            json.decodeFromString(data)
        } catch (e: Exception) { 
            println("WebEmployeeService: Error loading adjustments: ${e.message}")
            e.printStackTrace()
            emptyList() 
        }
    }

    private fun saveAdjustments(list: List<Adjustment>) {
        try { storageSetItem(adjustmentKey, json.encodeToString(list)) } catch (e: Exception) {
            println("WebEmployeeService: Error saving adjustments: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun exportIndemnityReport(report: com.burhan2855.personeltakip.shared.logic.IndemnityReport) {
        val emp = report.employee
        
        val baseDays = (report.years * 365) + report.remainingDays
        val leapDays = report.totalDaysWorked - baseDays
        val leapText = if (leapDays > 0) " (+ $leapDays gün artık yıl)" else ""
        val dayBreakdown = "Gün Bazında Toplam: (${report.years} x 365) + ${report.remainingDays} = ${report.totalDaysWorked} gün$leapText"

        val html = """
            <html>
            <head>
                <title>Kıdem ve İhbar Tazminatı - ${emp.firstName} ${emp.lastName}</title>
                <style>
                    body { font-family: sans-serif; padding: 40px; color: #333; line-height: 1.6; }
                    .header { border-bottom: 2px solid #FF9800; padding-bottom: 10px; margin-bottom: 20px; }
                    .card { background: #f5f5f5; border-radius: 8px; padding: 15px; margin-bottom: 20px; }
                    .card h3 { margin-top: 0; color: #3949AB; border-bottom: 1px solid #ddd; padding-bottom: 5px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 10px; }
                    td { padding: 8px 0; border-bottom: 1px solid #eee; }
                    .label { color: #666; font-size: 0.9em; }
                    .value { font-weight: bold; text-align: right; }
                    .total-box { background: #FFF3E0; padding: 15px; border-radius: 8px; border: 1px solid #FFE0B2; }
                    .net-row { font-size: 1.2em; font-weight: bold; color: #E65100; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h2>İŞTEN ÇIKIŞ TAZMİNATI RAPORU</h2>
                    <p><strong>Personel:</strong> ${emp.firstName} ${emp.lastName}</p>
                    <p><strong>Dönem:</strong> ${emp.startDate} - ${emp.endDate}</p>
                </div>

                <div class="card">
                    <h3>Çalışma Süresi</h3>
                    <table>
                        <tr><td class="label">Toplam Çalışılan Gün</td><td class="value">${report.totalDaysWorked} Gün</td></tr>
                        <tr><td class="label">Hizmet Süresi</td><td class="value">${report.years} Yıl ${report.months} Ay ${report.remainingDays} Gün</td></tr>
                        <tr><td class="label" colspan="2" style="font-size: 0.85em; color: #888; text-align: right;">$dayBreakdown</td></tr>
                    </table>
                </div>

                <div class="card">
                    <h3>Günlük Ücretler</h3>
                    <table>
                        <tr><td class="label">Net Maaş</td><td class="value">${formatMoney(report.dailyNetMaas)} TL</td></tr>
                        <tr><td class="label">Yemek</td><td class="value">${formatMoney(report.dailyNetYemek)} TL</td></tr>
                        <tr><td class="label">Yol</td><td class="value">${formatMoney(report.dailyNetYol)} TL</td></tr>
                        <tr style="border-top: 2px solid #ccc"><td class="label"><strong>Toplam Günlük</strong></td><td class="value"><strong>${formatMoney(report.totalDaily)} TL</strong></td></tr>
                    </table>
                </div>

                <div class="total-box">
                    <h3>Tazminat Hesap Detayları</h3>
                    <table>
                        <tr><td class="label">Kıdem Tazminatı</td><td class="value">${formatMoney(report.kidemTazminati)} TL</td></tr>
                        <tr><td class="label">Yıllık İzin Ücreti (${report.remainingLeaveDays} gün)</td><td class="value">${formatMoney(report.remainingLeaveAmount)} TL</td></tr>
                        <tr><td class="label">Toplam Yemek Alacağı</td><td class="value" style="color: #3949AB">${formatMoney(report.totalYemek)} TL</td></tr>
                        <tr><td class="label">Toplam Yol Alacağı</td><td class="value" style="color: #3949AB">${formatMoney(report.totalYol)} TL</td></tr>
                        <tr><td class="label">Toplam Prim Alacağı</td><td class="value" style="color: green">+${formatMoney(report.totalPrim)} TL</td></tr>
                        <tr><td class="label">Toplam Avans (Düşülecek)</td><td class="value" style="color: red">-${formatMoney(report.totalAvans)} TL</td></tr>
                        <tr><td class="label">Toplam Kesinti (Düşülecek)</td><td class="value" style="color: red">-${formatMoney(report.totalKesinti)} TL</td></tr>
                        <tr class="net-row"><td class="label">NET TAZMİNAT TUTARI</td><td class="value">${formatMoney(report.netTazminat)} TL</td></tr>
                        <tr><td class="label">Ödenen Tazminat</td><td class="value" style="color: #666">-${formatMoney(report.paidTazminat)} TL</td></tr>
                        <tr style="border-top: 2px solid #E65100"><td class="label"><strong>KALAN ÖDENECEK</strong></td><td class="value" style="color: #E65100"><strong>${formatMoney(report.remainingPayment)} TL</strong></td></tr>
                    </table>
                </div>

                <div style="margin-top: 40px; font-size: 0.8em; color: #888; text-align: center;">
                    Bu rapor personelin işten ayrılış işlemleri için sistem tarafından otomatik üretilmiştir.<br>
                    Tarih: ${kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())}
                </div>
            </body>
            </html>
        """.trimIndent()
        jsPrintReport(html)
    }

    override fun exportBackup() {
        try {
            val backup = BackupData(
                employees = _employees.value,
                logs = _logs.value,
                adjustments = _adjustments.value
            )
            val jsonStr = json.encodeToString(backup)
            val date = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            val filename = "PersonelTakip_Yedek_${date.date}_${date.hour}-${date.minute}.json"
            jsDownloadFile(filename, jsonStr)
            println("WebEmployeeService: Backup exported successfully.")
        } catch (e: Exception) {
            println("WebEmployeeService: Backup error: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun importBackup(onComplete: (Boolean) -> Unit) {
        jsReadFile { content ->
            if (content.isNullOrEmpty()) {
                onComplete(false)
                return@jsReadFile
            }
            try {
                val backup = json.decodeFromString<BackupData>(content)
                _employees.value = backup.employees
                _logs.value = backup.logs
                _adjustments.value = backup.adjustments
                
                saveEmployees(backup.employees)
                saveLogs(backup.logs)
                saveAdjustments(backup.adjustments)
                
                println("WebEmployeeService: Backup imported successfully. Employees: ${backup.employees.size}")
                onComplete(true)
            } catch (e: Exception) {
                println("WebEmployeeService: Import error: ${e.message}")
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    override fun exportReport(
        employee: com.burhan2855.personeltakip.shared.data.Employee, 
        report: com.burhan2855.personeltakip.shared.logic.SalaryReport, 
        logs: List<com.burhan2855.personeltakip.shared.data.WorkLog>, 
        adjustments: List<com.burhan2855.personeltakip.shared.data.Adjustment>,
        type: com.burhan2855.personeltakip.shared.logic.ExportType
    ) {
        if (type == com.burhan2855.personeltakip.shared.logic.ExportType.EXCEL) {
            val sb = StringBuilder()
            sb.append("Personel:;${employee.firstName} ${employee.lastName}\r\n")
            sb.append("Donem:;${report.startDate} - ${report.endDate}\r\n\r\n")
            sb.append("HAKEDIS DETAYLARI\r\n")
            sb.append("Aciklama;Tutar\r\n")
            val fixedBase = (employee.hourlyRate * 30 * employee.dailyWorkingHours) + employee.additionalSalary
            sb.append("Sabit Net Maas:;${fixedBase} TL\r\n")
            sb.append("Fazla Mesai:;${report.totalOvertimeEarnings} TL\r\n")
            sb.append("Eksik Calisma Kesintisi:;-${report.totalMissingDeduction} TL\r\n")
            sb.append("Prim:;${report.totalAdjustments.primAmount} TL\r\n")
            sb.append("Yemek (${report.totalYemekCount} adet):;${report.totalMealAllowance + report.totalAdjustments.yemekAmount} TL\r\n")
            sb.append("Yol (${report.totalYolCount} adet):;${report.totalTransportAllowance + report.totalAdjustments.yolAmount} TL\r\n")
            sb.append("TOPLAM HAKEDIS:;${report.totalEarnings} TL\r\n\r\n")
            sb.append("ODEMELER\r\n")
            sb.append("Maas/Banka/Elden Odemeler:;${report.totalPayments} TL\r\n")
            sb.append("KALAN BAKIYE:;${report.remainingBalance} TL\r\n")
            
            jsDownloadFile("${employee.firstName}_${employee.lastName}_Rapor.csv", sb.toString())
        } else {
            // PDF -> HTML Print version
            val html = """
                <html>
                <head>
                    <title>Maaş Bordrosu - ${employee.firstName} ${employee.lastName}</title>
                    <style>
                        body { font-family: sans-serif; padding: 40px; color: #333; }
                        .header { border-bottom: 2px solid #3F51B5; padding-bottom: 10px; margin-bottom: 20px; }
                        .section { margin-bottom: 30px; }
                        table { width: 100%; border-collapse: collapse; }
                        th, td { text-align: left; padding: 10px; border-bottom: 1px solid #eee; }
                        .total { font-weight: bold; background: #f9f9f9; }
                        .footer { margin-top: 50px; font-size: 0.8em; color: #777; }
                        .positive { color: green; }
                        .negative { color: red; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h2>PERSONEL MAAŞ RAPORU</h2>
                        <p><strong>Personel:</strong> ${employee.firstName} ${employee.lastName} | <strong>Dönem:</strong> ${report.startDate} - ${report.endDate}</p>
                    </div>
                    <div class="section">
                        <h3>Hakediş Detayları</h3>
                        <table>
                            <tr><td>Sabit Net Maaş</td><td>${formatMoney(fixedBase(employee))} TL</td></tr>
                            <tr><td>Fazla Mesai</td><td class="positive">+${formatMoney(report.totalOvertimeEarnings)} TL</td></tr>
                            <tr><td>Eksik Çalışma Kesintisi</td><td class="negative">-${formatMoney(report.totalMissingDeduction)} TL</td></tr>
                            <tr><td>Yemek Yardımı (${report.totalYemekCount} adet)</td><td class="positive">+${formatMoney(report.totalMealAllowance + report.totalAdjustments.yemekAmount)} TL</td></tr>
                            <tr><td>Yol Yardımı (${report.totalYolCount} adet)</td><td class="positive">+${formatMoney(report.totalTransportAllowance + report.totalAdjustments.yolAmount)} TL</td></tr>
                            <tr><td>Primalar</td><td class="positive">+${formatMoney(report.totalAdjustments.primAmount)} TL</td></tr>
                            <tr class="total"><td>TOPLAM HAKEDİŞ (Genel)</td><td>${formatMoney(report.totalEarnings)} TL</td></tr>
                        </table>
                    </div>
                    <div class="section">
                        <h3>Ödemeler ve Kalan</h3>
                        <table>
                            <tr><td>Toplam Yapılan Ödemeler</td><td>${formatMoney(report.totalPayments)} TL</td></tr>
                            <tr class="total"><td>GÜNCEL BAKİYE (KALAN)</td><td>${formatMoney(report.remainingBalance)} TL</td></tr>
                        </table>
                    </div>
                    <div class="footer">
                        <p>Bu rapor sistem tarafından otomatik oluşturulmuştur. Tarih: ${kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())}</p>
                    </div>
                </body>
                </html>
            """.trimIndent()
            jsPrintReport(html)
        }
    }

    private fun fixedBase(e: com.burhan2855.personeltakip.shared.data.Employee) = (e.hourlyRate * 30 * e.dailyWorkingHours) + e.additionalSalary
    
    private fun formatMoney(d: Double): String {
        val rounded = (kotlin.math.round(d * 100) / 100).toString()
        return rounded.replace(".", ",")
    }
}

@JsFun("""(filename, content) => {
    var type = 'text/plain';
    if (filename.endsWith('.csv')) type = 'text/csv;charset=utf-8;';
    if (filename.endsWith('.json')) type = 'application/json;charset=utf-8;';
    
    var blob = new Blob([content], { type: type });
    var link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}""")
private external fun jsDownloadFile(filename: String, content: String)

@JsFun("(html) => window.printReport(html)")
private external fun jsPrintReport(html: String)

@JsFun("(key) => window.localStorage.getItem(key)")
private external fun storageGetItem(key: String): String?

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun storageSetItem(key: String, value: String)

@JsFun("(callback) => { var input = document.createElement('input'); input.type = 'file'; input.accept = '.json'; input.onchange = e => { var file = e.target.files[0]; if (!file) return; var reader = new FileReader(); reader.onload = evt => callback(evt.target.result); reader.readAsText(file); }; input.click(); }")
private external fun jsReadFile(callback: (String?) -> Unit)
