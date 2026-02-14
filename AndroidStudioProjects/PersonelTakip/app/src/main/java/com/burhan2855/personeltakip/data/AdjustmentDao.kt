package com.burhan2855.personeltakip.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AdjustmentDao {
    @Query("SELECT * FROM adjustments WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAdjustmentsForEmployee(employeeId: Int): Flow<List<Adjustment>>

    @Query("SELECT * FROM adjustments WHERE employeeId = :employeeId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAdjustmentsInRange(employeeId: Int, startDate: LocalDate, endDate: LocalDate): List<Adjustment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: Adjustment)

    @Update
    suspend fun updateAdjustment(adjustment: Adjustment)

    @Delete
    suspend fun deleteAdjustment(adjustment: Adjustment)
}
