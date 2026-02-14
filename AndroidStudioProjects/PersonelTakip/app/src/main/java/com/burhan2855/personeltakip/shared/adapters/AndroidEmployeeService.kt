package com.burhan2855.personeltakip.shared.adapters

import com.burhan2855.personeltakip.data.EmployeeRepository
import com.burhan2855.personeltakip.shared.data.Employee
import com.burhan2855.personeltakip.shared.data.WorkLog
import com.burhan2855.personeltakip.shared.data.Adjustment
import com.burhan2855.personeltakip.shared.data.AdjustmentType
import com.burhan2855.personeltakip.shared.logic.IEmployeeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime

class AndroidEmployeeService(private val repository: EmployeeRepository) : IEmployeeService {

    override fun getAllEmployees(): Flow<List<Employee>> = repository.allEmployees.map { list ->
        list.map { it.toShared() }
    }

    override fun getEmployeeById(id: Int): Flow<Employee?> = flow {
        emit(repository.getEmployeeById(id)?.toShared())
    }

    override suspend fun insertEmployee(employee: Employee) {
        repository.insertEmployee(employee.toAndroid())
    }

    override suspend fun updateEmployee(employee: Employee) {
        repository.updateEmployee(employee.toAndroid())
    }

    override suspend fun deleteEmployee(employee: Employee) {
        repository.deleteEmployee(employee.toAndroid())
    }

    override suspend fun insertWorkLog(workLog: WorkLog) {
        repository.insertWorkLog(workLog.toAndroid())
    }

    override suspend fun updateWorkLog(workLog: WorkLog) {
        repository.updateWorkLog(workLog.toAndroid())
    }

    override suspend fun deleteWorkLog(workLog: WorkLog) {
        repository.deleteWorkLog(workLog.toAndroid())
    }

    override fun getWorkLogsForEmployee(employeeId: Int): Flow<List<WorkLog>> {
        return repository.getWorkLogsForEmployee(employeeId).map { list ->
            list.map { it.toShared() }
        }
    }

    override suspend fun insertAdjustment(adjustment: Adjustment) {
        repository.insertAdjustment(adjustment.toAndroid())
    }

    override suspend fun updateAdjustment(adjustment: Adjustment) {
        repository.updateAdjustment(adjustment.toAndroid())
    }

    override suspend fun deleteAdjustment(adjustment: Adjustment) {
        repository.deleteAdjustment(adjustment.toAndroid())
    }

    override fun getAdjustmentsForEmployee(employeeId: Int): Flow<List<Adjustment>> {
        return repository.getAdjustmentsForEmployee(employeeId).map { list ->
            list.map { it.toShared() }
        }
    }

    override fun exportReport(employee: Employee, report: com.burhan2855.personeltakip.shared.logic.SalaryReport, logs: List<WorkLog>, adjustments: List<Adjustment>, type: com.burhan2855.personeltakip.shared.logic.ExportType) {
        // Android implementation stub
    }

    override fun exportIndemnityReport(report: com.burhan2855.personeltakip.shared.logic.IndemnityReport) {
        // Android implementation stub
    }

    override fun exportBackup() {
        // Android implementation stub
    }

    override fun importBackup(onComplete: (Boolean) -> Unit) {
        // Android implementation stub
        onComplete(false)
    }

    // --- Mappers ---

    private fun com.burhan2855.personeltakip.data.Employee.toShared(): Employee {
        return Employee(
            id = id,
            firstName = firstName,
            lastName = lastName,
            position = position,
            hourlyRate = hourlyRate,
            dailyWorkingHours = dailyWorkingHours,
            overtimeMultiplier = overtimeMultiplier,
            additionalSalary = additionalSalary,
            annualLeaveEntitlement = annualLeaveEntitlement,
            mealAllowance = mealAllowance,
            transportAllowance = transportAllowance,
            imageUri = imageUri,
            startDate = startDate?.toKotlinLocalDate(),
            endDate = endDate?.toKotlinLocalDate(),
            iban = iban,
            phoneNumber = phoneNumber,
            email = email,
            address = address
        )
    }

    private fun Employee.toAndroid(): com.burhan2855.personeltakip.data.Employee {
        return com.burhan2855.personeltakip.data.Employee(
            id = id,
            firstName = firstName,
            lastName = lastName,
            position = position,
            hourlyRate = hourlyRate,
            dailyWorkingHours = dailyWorkingHours,
            overtimeMultiplier = overtimeMultiplier,
            additionalSalary = additionalSalary,
            annualLeaveEntitlement = annualLeaveEntitlement,
            mealAllowance = mealAllowance,
            transportAllowance = transportAllowance,
            imageUri = imageUri,
            startDate = startDate?.toJavaLocalDate(),
            endDate = endDate?.toJavaLocalDate(),
            iban = iban,
            phoneNumber = phoneNumber,
            email = email,
            address = address
        )
    }

    private fun com.burhan2855.personeltakip.data.WorkLog.toShared(): WorkLog {
        return WorkLog(
            id = id,
            employeeId = employeeId,
            date = date.toKotlinLocalDate(),
            startTime = startTime?.toKotlinLocalDateTime(),
            endTime = endTime?.toKotlinLocalDateTime(),
            breakDurationMinutes = breakDurationMinutes,
            isOnLeave = isOnLeave,
            leaveType = leaveType,
            hasMeal = hasMeal,
            hasTransport = hasTransport
        )
    }

    private fun WorkLog.toAndroid(): com.burhan2855.personeltakip.data.WorkLog {
        return com.burhan2855.personeltakip.data.WorkLog(
            id = id,
            employeeId = employeeId,
            date = date.toJavaLocalDate(),
            startTime = startTime?.toJavaLocalDateTime(),
            endTime = endTime?.toJavaLocalDateTime(),
            breakDurationMinutes = breakDurationMinutes,
            isOnLeave = isOnLeave,
            leaveType = leaveType,
            hasMeal = hasMeal,
            hasTransport = hasTransport
        )
    }

    private fun com.burhan2855.personeltakip.data.Adjustment.toShared(): Adjustment {
        return Adjustment(
            id = id,
            employeeId = employeeId,
            date = date.toKotlinLocalDate(),
            amount = amount,
            type = AdjustmentType.valueOf(type.name),
            description = description
        )
    }

    private fun Adjustment.toAndroid(): com.burhan2855.personeltakip.data.Adjustment {
        return com.burhan2855.personeltakip.data.Adjustment(
            id = id,
            employeeId = employeeId,
            date = date.toJavaLocalDate(),
            amount = amount,
            type = com.burhan2855.personeltakip.data.AdjustmentType.valueOf(type.name),
            description = description
        )
    }
}
