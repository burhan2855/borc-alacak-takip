package com.burhan2855.borctakip.data.calendar

import kotlinx.coroutines.flow.StateFlow

interface CalendarPermissionHandler {
    val permissionStatus: StateFlow<PermissionStatus>
    fun getInitialStatus(): PermissionStatus
}

enum class PermissionStatus {
    GRANTED,
    DENIED
}
