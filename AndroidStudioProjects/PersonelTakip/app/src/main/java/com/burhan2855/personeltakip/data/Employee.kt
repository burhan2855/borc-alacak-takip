package com.burhan2855.personeltakip.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
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
