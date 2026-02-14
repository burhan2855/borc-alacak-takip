package com.burhan2855.personeltakip.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.burhan2855.personeltakip.data.*
import com.burhan2855.personeltakip.logic.SalaryCalculator
import com.burhan2855.personeltakip.logic.SalaryReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EmployeeViewModel(private val repository: EmployeeRepository) : ViewModel() {
    private val calculator = SalaryCalculator()

    val allEmployees: Flow<List<Employee>> = repository.allEmployees

    fun addEmployee(employee: Employee) {
        viewModelScope.launch { repository.insertEmployee(employee) }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch { repository.updateEmployee(employee) }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch { repository.deleteEmployee(employee) }
    }

    fun addWorkLog(workLog: WorkLog) {
        viewModelScope.launch { repository.insertWorkLog(workLog) }
    }

    fun updateWorkLog(workLog: WorkLog) {
        viewModelScope.launch { repository.updateWorkLog(workLog) }
    }

    fun deleteWorkLog(workLog: WorkLog) {
        viewModelScope.launch { repository.deleteWorkLog(workLog) }
    }

    fun getWorkLogs(employeeId: Int): Flow<List<WorkLog>> {
        return repository.getWorkLogsForEmployee(employeeId)
    }

    fun getAdjustments(employeeId: Int): Flow<List<Adjustment>> {
        return repository.getAdjustmentsForEmployee(employeeId)
    }

    fun addAdjustment(adjustment: Adjustment) {
        viewModelScope.launch { repository.insertAdjustment(adjustment) }
    }

    fun updateAdjustment(adjustment: Adjustment) {
        viewModelScope.launch { repository.updateAdjustment(adjustment) }
    }

    fun deleteAdjustment(adjustment: Adjustment) {
        viewModelScope.launch { repository.deleteAdjustment(adjustment) }
    }

    fun getSalaryReport(
        employee: Employee, 
        logs: List<WorkLog>,
        adjustments: List<Adjustment>,
        year: Int = LocalDate.now().year, 
        month: Int = LocalDate.now().monthValue
    ): SalaryReport {
        return calculator.calculateMonthlySalary(employee, logs, adjustments, year, month)
    }
}

class EmployeeViewModelFactory(private val repository: EmployeeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmployeeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
