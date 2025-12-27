package com.burhan2855.borctakip.data

import android.util.Log
import kotlinx.coroutines.flow.Flow

class PartialPaymentRepository(private val partialPaymentDao: PartialPaymentDao) {

    suspend fun addPartialPayment(payment: PartialPayment) {
        Log.d("DB_DUMP", "PartialPaymentDao.insert called")
        partialPaymentDao.insert(payment)
        Log.d("DB_DUMP", "PartialPayment inserted")
    }

    fun getPaymentsForTransaction(transactionId: Long): Flow<List<PartialPayment>> {
        return partialPaymentDao.getPaymentsForTransaction(transactionId)
    }

    suspend fun getPaymentsForTransactionOnce(transactionId: Long): List<PartialPayment> {
        return partialPaymentDao.getPaymentsForTransactionOnce(transactionId)
    }

    fun getTotalPaidAmount(transactionId: Long): Flow<Double?> {
        return partialPaymentDao.getTotalPaidAmount(transactionId)
    }
}
