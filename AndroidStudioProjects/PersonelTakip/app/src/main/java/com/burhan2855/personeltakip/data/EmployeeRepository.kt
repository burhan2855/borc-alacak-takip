package com.burhan2855.personeltakip.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class EmployeeRepository(
    private val employeeDao: EmployeeDao,
    private val workLogDao: WorkLogDao,
    private val adjustmentDao: AdjustmentDao
) {
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()

    suspend fun insertEmployee(employee: Employee) = employeeDao.insertEmployee(employee)
    suspend fun updateEmployee(employee: Employee) = employeeDao.updateEmployee(employee)
    suspend fun deleteEmployee(employee: Employee) = employeeDao.deleteEmployee(employee)
    suspend fun getEmployeeById(id: Int) = employeeDao.getEmployeeById(id)

    fun getWorkLogsForEmployee(employeeId: Int): Flow<List<WorkLog>> = 
        workLogDao.getWorkLogsForEmployee(employeeId)
    suspend fun insertWorkLog(workLog: WorkLog) = workLogDao.insertWorkLog(workLog)
    suspend fun updateWorkLog(workLog: WorkLog) = workLogDao.updateWorkLog(workLog)
    suspend fun deleteWorkLog(workLog: WorkLog) = workLogDao.deleteWorkLog(workLog)

    fun getAdjustmentsForEmployee(employeeId: Int): Flow<List<Adjustment>> =
        adjustmentDao.getAdjustmentsForEmployee(employeeId)
    suspend fun insertAdjustment(adjustment: Adjustment) = adjustmentDao.insertAdjustment(adjustment)
    suspend fun updateAdjustment(adjustment: Adjustment) = adjustmentDao.updateAdjustment(adjustment)
    suspend fun deleteAdjustment(adjustment: Adjustment) = adjustmentDao.deleteAdjustment(adjustment)
}
