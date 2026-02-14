package com.burhan2855.personeltakip.shared.logic

import com.burhan2855.personeltakip.shared.data.Employee
import com.burhan2855.personeltakip.shared.data.WorkLog
import com.burhan2855.personeltakip.shared.data.Adjustment
import kotlinx.coroutines.flow.Flow

interface IEmployeeService {
    fun getAllEmployees(): Flow<List<Employee>>
    fun getEmployeeById(id: Int): Flow<Employee?>

    suspend fun insertEmployee(employee: Employee)
    suspend fun updateEmployee(employee: Employee)
    suspend fun deleteEmployee(employee: Employee)

    suspend fun insertWorkLog(workLog: WorkLog)
    suspend fun updateWorkLog(workLog: WorkLog)
    suspend fun deleteWorkLog(workLog: WorkLog)
    fun getWorkLogsForEmployee(employeeId: Int): Flow<List<WorkLog>>

    suspend fun insertAdjustment(adjustment: Adjustment)
    suspend fun updateAdjustment(adjustment: Adjustment)
    suspend fun deleteAdjustment(adjustment: Adjustment)
    fun getAdjustmentsForEmployee(employeeId: Int): Flow<List<Adjustment>>
    
    fun exportReport(employee: Employee, report: SalaryReport, logs: List<WorkLog>, adjustments: List<Adjustment>, type: ExportType)
    fun exportIndemnityReport(report: IndemnityReport)
    
    fun exportBackup()
    fun importBackup(onComplete: (Boolean) -> Unit)
}

enum class ExportType {
    PDF, EXCEL
}
