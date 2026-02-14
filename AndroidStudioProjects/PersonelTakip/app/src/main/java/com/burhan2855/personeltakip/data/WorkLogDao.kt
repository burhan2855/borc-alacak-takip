package com.burhan2855.personeltakip.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkLogDao {
    @Query("SELECT * FROM work_logs WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getWorkLogsForEmployee(employeeId: Int): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs WHERE employeeId = :employeeId AND date BETWEEN :startDate AND :endDate")
    suspend fun getWorkLogsInRange(employeeId: Int, startDate: LocalDate, endDate: LocalDate): List<WorkLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(workLog: WorkLog)

    @Update
    suspend fun updateWorkLog(workLog: WorkLog)

    @Delete
    suspend fun deleteWorkLog(workLog: WorkLog)
}
