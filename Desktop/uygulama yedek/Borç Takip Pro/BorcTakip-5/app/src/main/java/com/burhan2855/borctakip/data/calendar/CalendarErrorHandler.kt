package com.burhan2855.borctakip.data.calendar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.pow

interface CalendarErrorHandler {
    suspend fun handleSyncError(error: Throwable, retryCount: Int = 0): ErrorHandlingResult
    suspend fun retryWithBackoff(operation: suspend () -> SyncResult, maxRetries: Int = 3): SyncResult
    suspend fun validateDataConsistency(): ConsistencyResult
    suspend fun cleanupOrphanedEvents(): CleanupResult
}

data class ErrorHandlingResult(
    val shouldRetry: Boolean,
    val delayMs: Long = 0,
    val errorType: ErrorType,
    val message: String
)

data class ConsistencyResult(
    val isConsistent: Boolean,
    val inconsistencies: List<String> = emptyList(),
    val fixedCount: Int = 0
)

data class CleanupResult(
    val success: Boolean,
    val cleanedCount: Int = 0,
    val errorMessage: String? = null
)

enum class ErrorType {
    NETWORK_ERROR,
    PERMISSION_ERROR,
    CALENDAR_PROVIDER_ERROR,
    DATA_CORRUPTION,
    UNKNOWN_ERROR
}

class CalendarErrorHandlerImpl(
    private val calendarEventRepository: CalendarEventRepository,
    private val calendarManager: CalendarManager
) : CalendarErrorHandler {

    override suspend fun handleSyncError(error: Throwable, retryCount: Int): ErrorHandlingResult {
        return withContext(Dispatchers.IO) {
            val errorType = classifyError(error)
            
            when (errorType) {
                ErrorType.NETWORK_ERROR -> {
                    if (retryCount < 3) {
                        ErrorHandlingResult(
                            shouldRetry = true,
                            delayMs = calculateBackoffDelay(retryCount),
                            errorType = errorType,
                            message = "Network error, will retry in ${calculateBackoffDelay(retryCount)}ms"
                        )
                    } else {
                        ErrorHandlingResult(
                            shouldRetry = false,
                            errorType = errorType,
                            message = "Network error: Maximum retries exceeded"
                        )
                    }
                }
                
                ErrorType.PERMISSION_ERROR -> {
                    ErrorHandlingResult(
                        shouldRetry = false,
                        errorType = errorType,
                        message = "Calendar permission denied. Please grant calendar access."
                    )
                }
                
                ErrorType.CALENDAR_PROVIDER_ERROR -> {
                    if (retryCount < 2) {
                        ErrorHandlingResult(
                            shouldRetry = true,
                            delayMs = calculateBackoffDelay(retryCount),
                            errorType = errorType,
                            message = "Calendar provider error, retrying..."
                        )
                    } else {
                        ErrorHandlingResult(
                            shouldRetry = false,
                            errorType = errorType,
                            message = "Calendar provider unavailable"
                        )
                    }
                }
                
                ErrorType.DATA_CORRUPTION -> {
                    ErrorHandlingResult(
                        shouldRetry = false,
                        errorType = errorType,
                        message = "Data corruption detected. Manual intervention required."
                    )
                }
                
                ErrorType.UNKNOWN_ERROR -> {
                    if (retryCount < 1) {
                        ErrorHandlingResult(
                            shouldRetry = true,
                            delayMs = 1000,
                            errorType = errorType,
                            message = "Unknown error, retrying once..."
                        )
                    } else {
                        ErrorHandlingResult(
                            shouldRetry = false,
                            errorType = errorType,
                            message = "Unknown error: ${error.message}"
                        )
                    }
                }
            }
        }
    }

    override suspend fun retryWithBackoff(operation: suspend () -> SyncResult, maxRetries: Int): SyncResult {
        return withContext(Dispatchers.IO) {
            var lastResult: SyncResult? = null
            var retryCount = 0
            
            while (retryCount <= maxRetries) {
                try {
                    val result = operation()
                    if (result.success) {
                        return@withContext result
                    }
                    lastResult = result
                } catch (e: Exception) {
                    val errorHandling = handleSyncError(e, retryCount)
                    
                    if (!errorHandling.shouldRetry || retryCount >= maxRetries) {
                        return@withContext SyncResult(
                            success = false,
                            errorMessage = errorHandling.message
                        )
                    }
                    
                    delay(errorHandling.delayMs)
                }
                
                retryCount++
            }
            
            lastResult ?: SyncResult(
                success = false,
                errorMessage = "Operation failed after $maxRetries retries"
            )
        }
    }

    override suspend fun validateDataConsistency(): ConsistencyResult {
        return withContext(Dispatchers.IO) {
            try {
                val allEvents = calendarEventRepository.getAllPendingEvents()
                val inconsistencies = mutableListOf<String>()
                var fixedCount = 0
                
                allEvents.forEach { event ->
                    if (event.startTime <= 0) {
                        inconsistencies.add("Event ${event.id} has invalid start time")
                    }
                    
                    if (event.endTime <= event.startTime) {
                        inconsistencies.add("Event ${event.id} has invalid end time")
                    }
                    
                    if (event.title.isBlank()) {
                        inconsistencies.add("Event ${event.id} has empty title")
                    }
                    
                    if (event.syncStatus == SyncStatus.SYNCED && event.deviceCalendarEventId <= 0) {
                        inconsistencies.add("Event ${event.id} marked as synced but has no device calendar ID")
                        val fixedEvent = event.copy(syncStatus = SyncStatus.PENDING_CREATE)
                        calendarEventRepository.updateCalendarEvent(fixedEvent)
                        fixedCount++
                    }
                }
                
                val duplicates = allEvents.groupBy { "${it.transactionId}_${it.startTime}_${it.eventType}" }
                    .filter { it.value.size > 1 }
                
                duplicates.forEach { (_, events) ->
                    inconsistencies.add("Duplicate events found for key: ${events.first().transactionId}")
                    events.drop(1).forEach { duplicateEvent ->
                        val fixedEvent = duplicateEvent.copy(syncStatus = SyncStatus.PENDING_DELETE)
                        calendarEventRepository.updateCalendarEvent(fixedEvent)
                        fixedCount++
                    }
                }
                
                ConsistencyResult(
                    isConsistent = inconsistencies.isEmpty(),
                    inconsistencies = inconsistencies,
                    fixedCount = fixedCount
                )
            } catch (e: Exception) {
                ConsistencyResult(
                    isConsistent = false,
                    inconsistencies = listOf("Error validating consistency: ${e.message}")
                )
            }
        }
    }

    override suspend fun cleanupOrphanedEvents(): CleanupResult {
        return withContext(Dispatchers.IO) {
            try {
                val allEvents = calendarEventRepository.getAllPendingEvents()
                var cleanedCount = 0
                
                val eventsToClean = allEvents.filter { event ->
                    event.syncStatus == SyncStatus.PENDING_DELETE ||
                    (event.syncStatus == SyncStatus.SYNC_FAILED && 
                     System.currentTimeMillis() - event.updatedAt > 7 * 24 * 60 * 60 * 1000) // 7 days old
                }
                
                eventsToClean.forEach { event ->
                    try {
                        if (event.deviceCalendarEventId > 0) {
                            calendarManager.deleteTransactionEvent(event.transactionId)
                        }
                        
                        calendarEventRepository.deleteCalendarEvent(event.id)
                        cleanedCount++
                    } catch (e: Exception) {
                        // Log error but continue
                    }
                }
                
                CleanupResult(
                    success = true,
                    cleanedCount = cleanedCount
                )
            } catch (e: Exception) {
                CleanupResult(
                    success = false,
                    errorMessage = "Cleanup failed: ${e.message}"
                )
            }
        }
    }

    private fun classifyError(error: Throwable): ErrorType {
        return when {
            error.message?.contains("permission", ignoreCase = true) == true -> ErrorType.PERMISSION_ERROR
            error.message?.contains("network", ignoreCase = true) == true -> ErrorType.NETWORK_ERROR
            error.message?.contains("calendar", ignoreCase = true) == true -> ErrorType.CALENDAR_PROVIDER_ERROR
            error.message?.contains("corrupt", ignoreCase = true) == true -> ErrorType.DATA_CORRUPTION
            error is SecurityException -> ErrorType.PERMISSION_ERROR
            error is IllegalStateException -> ErrorType.CALENDAR_PROVIDER_ERROR
            else -> ErrorType.UNKNOWN_ERROR
        }
    }

    private fun calculateBackoffDelay(retryCount: Int): Long {
        val baseDelay = 1000L
        val maxDelay = 30000L
        val delay = baseDelay * (2.0.pow(retryCount)).toLong()
        return min(delay, maxDelay)
    }
}
