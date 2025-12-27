package com.burhan2855.borctakip.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PartialPaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PartialPayment)

    @Query("SELECT * FROM partial_payments WHERE transactionId = :transactionId")
    fun getPaymentsForTransaction(transactionId: Long): Flow<List<PartialPayment>>

    @Query("SELECT * FROM partial_payments WHERE transactionId = :transactionId")
    suspend fun getPaymentsForTransactionOnce(transactionId: Long): List<PartialPayment>

    @Query("SELECT SUM(amount) FROM partial_payments WHERE transactionId = :transactionId")
    fun getTotalPaidAmount(transactionId: Long): Flow<Double?>
}
