package com.burhan2855.borctakip.data.calendar

import com.burhan2855.borctakip.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface InstallmentTracker {
    suspend fun updateInstallmentStatus(transactionId: Long, installmentNumber: Int, isPaid: Boolean): InstallmentUpdateResult
    suspend fun checkSeriesCompletion(transactionId: Long): SeriesCompletionResult
    suspend fun syncInstallmentWithCalendar(transactionId: Long, installmentNumber: Int): SyncResult
}

data class InstallmentUpdateResult(
    val success: Boolean,
    val updatedInstallment: Installment? = null,
    val errorMessage: String? = null
)

data class SeriesCompletionResult(
    val isCompleted: Boolean,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val remainingInstallments: Int
)

class InstallmentTrackerImpl(
    private val calendarEventRepository: CalendarEventRepository,
    private val calendarManager: CalendarManager
) : InstallmentTracker {

    override suspend fun updateInstallmentStatus(
        transactionId: Long, 
        installmentNumber: Int, 
        isPaid: Boolean
    ): InstallmentUpdateResult {
        return withContext(Dispatchers.IO) {
            try {
                // Get all calendar events for this transaction
                val events = calendarEventRepository.getEventsByTransactionId(transactionId)
                val installmentEvents = events.filter { it.eventType == CalendarEventType.INSTALLMENT_PAYMENT }
                
                // Find the specific installment event
                val targetEvent = installmentEvents.find { event ->
                    // Extract installment number from title or description
                    extractInstallmentNumber(event.title) == installmentNumber
                }
                
                if (targetEvent != null) {
                    // Update the event status
                    val updatedTitle = if (isPaid) {
                        "${targetEvent.title} ✓ PAID"
                    } else {
                        targetEvent.title.replace(" ✓ PAID", "")
                    }
                    
                    val updates = EventUpdates(
                        title = updatedTitle,
                        description = updateInstallmentDescription(targetEvent.description, isPaid)
                    )
                    
                    val updateResult = calendarManager.updateTransactionEvent(transactionId, updates)
                    
                    if (updateResult.success) {
                        // Create updated installment object
                        val updatedInstallment = createInstallmentFromEvent(targetEvent, installmentNumber, isPaid)
                        
                        InstallmentUpdateResult(
                            success = true,
                            updatedInstallment = updatedInstallment
                        )
                    } else {
                        InstallmentUpdateResult(
                            success = false,
                            errorMessage = updateResult.errorMessage
                        )
                    }
                } else {
                    InstallmentUpdateResult(
                        success = false,
                        errorMessage = "Installment event not found"
                    )
                }
            } catch (e: Exception) {
                InstallmentUpdateResult(
                    success = false,
                    errorMessage = "Error updating installment status: ${e.message}"
                )
            }
        }
    }

    override suspend fun checkSeriesCompletion(transactionId: Long): SeriesCompletionResult {
        return withContext(Dispatchers.IO) {
            try {
                val events = calendarEventRepository.getEventsByTransactionId(transactionId)
                val installmentEvents = events.filter { it.eventType == CalendarEventType.INSTALLMENT_PAYMENT }
                
                val totalInstallments = installmentEvents.size
                val paidInstallments = installmentEvents.count { event ->
                    event.title.contains("✓ PAID") || event.description.contains("Status: Paid")
                }
                
                SeriesCompletionResult(
                    isCompleted = paidInstallments == totalInstallments && totalInstallments > 0,
                    totalInstallments = totalInstallments,
                    paidInstallments = paidInstallments,
                    remainingInstallments = totalInstallments - paidInstallments
                )
            } catch (e: Exception) {
                SeriesCompletionResult(
                    isCompleted = false,
                    totalInstallments = 0,
                    paidInstallments = 0,
                    remainingInstallments = 0
                )
            }
        }
    }

    override suspend fun syncInstallmentWithCalendar(transactionId: Long, installmentNumber: Int): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                val events = calendarEventRepository.getEventsByTransactionId(transactionId)
                val targetEvent = events.find { event ->
                    event.eventType == CalendarEventType.INSTALLMENT_PAYMENT &&
                    extractInstallmentNumber(event.title) == installmentNumber
                }
                
                if (targetEvent != null) {
                    // Update sync status
                    val updatedEvent = targetEvent.copy(
                        syncStatus = SyncStatus.SYNCED,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    calendarEventRepository.updateCalendarEvent(updatedEvent)
                    
                    SyncResult(
                        success = true,
                        syncedCount = 1,
                        failedCount = 0
                    )
                } else {
                    SyncResult(
                        success = false,
                        syncedCount = 0,
                        failedCount = 1,
                        errorMessage = "Installment event not found"
                    )
                }
            } catch (e: Exception) {
                SyncResult(
                    success = false,
                    syncedCount = 0,
                    failedCount = 1,
                    errorMessage = "Sync failed: ${e.message}"
                )
            }
        }
    }

    private fun extractInstallmentNumber(title: String): Int {
        // Extract installment number from title like "Installment 2/5: 1000"
        val regex = Regex("Installment (\\d+)/\\d+")
        val matchResult = regex.find(title)
        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun updateInstallmentDescription(description: String, isPaid: Boolean): String {
        return if (isPaid) {
            if (description.contains("Status: Pending")) {
                description.replace("Status: Pending", "Status: Paid")
            } else if (!description.contains("Status: Paid")) {
                "$description\nStatus: Paid"
            } else {
                description
            }
        } else {
            description.replace("Status: Paid", "Status: Pending")
        }
    }

    private fun createInstallmentFromEvent(event: CalendarEvent, installmentNumber: Int, isPaid: Boolean): Installment {
        // Extract amount from title or description
        val amountRegex = Regex("(\\d+(?:\\.\\d+)?)")
        val amount = amountRegex.find(event.title)?.value?.toDoubleOrNull() ?: 0.0
        
        // Extract total installments from title
        val totalRegex = Regex("Installment \\d+/(\\d+)")
        val totalInstallments = totalRegex.find(event.title)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        
        return Installment(
            transactionId = event.transactionId,
            installmentNumber = installmentNumber,
            totalInstallments = totalInstallments,
            amount = amount,
            dueDate = event.startTime,
            isPaid = isPaid,
            remainingBalance = if (isPaid) 0.0 else amount
        )
    }
}
