package com.burhan2855.personeltakip.shared.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val position: String,
    val hourlyRate: Double,
    val dailyWorkingHours: Double = 8.0,
    val overtimeMultiplier: Double = 1.5,
    val additionalSalary: Double = 0.0,
    val annualLeaveEntitlement: Int = 0,
    val mealAllowance: Double = 0.0,
    val transportAllowance: Double = 0.0,
    val imageUri: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val iban: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val address: String? = null
)

@Serializable
data class WorkLog(
    val id: Int = 0,
    val employeeId: Int,
    val date: LocalDate,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val breakDurationMinutes: Int = 0,
    val isOnLeave: Boolean = false,
    val leaveType: String? = null,
    val hasMeal: Boolean = true,
    val hasTransport: Boolean = true
)

@Serializable
data class Adjustment(
    val id: Int = 0,
    val employeeId: Int,
    val date: LocalDate,
    val amount: Double,
    val type: AdjustmentType,
    val description: String? = null
)

@Serializable
enum class AdjustmentType {
    AVANS, YEMEK, YOL, PRIM, KESINTI, MAAS_ODEME, TAZMINAT_ODEME, BANKA_ODEME, ELDEN_ODEME
}
