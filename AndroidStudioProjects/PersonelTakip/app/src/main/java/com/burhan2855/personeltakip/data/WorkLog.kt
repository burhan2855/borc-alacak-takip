package com.burhan2855.personeltakip.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "work_logs",
    foreignKeys = [
        ForeignKey(
            entity = Employee::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId")]
)
data class WorkLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: Int,
    val date: LocalDate,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val breakDurationMinutes: Int = 0,
    val isOnLeave: Boolean = false,
    val leaveType: String? = null, // e.g., "Paid", "Unpaid", "Sick"
    val hasMeal: Boolean = true,
    val hasTransport: Boolean = true
)
