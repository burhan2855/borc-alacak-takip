package com.burhan2855.personeltakip.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "adjustments",
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
data class Adjustment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val employeeId: Int,
    val date: LocalDate,
    val amount: Double,
    val type: AdjustmentType,
    val description: String? = null
)

enum class AdjustmentType {
    AVANS, YEMEK, YOL, PRIM, KESINTI, MAAS_ODEME, TAZMINAT_ODEME, BANKA_ODEME, ELDEN_ODEME
}
