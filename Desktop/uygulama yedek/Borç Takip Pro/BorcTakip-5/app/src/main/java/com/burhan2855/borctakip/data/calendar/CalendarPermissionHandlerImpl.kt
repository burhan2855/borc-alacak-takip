package com.burhan2855.borctakip.data.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CalendarPermissionHandlerImpl(private val context: Context) : CalendarPermissionHandler {

    private val _permissionStatus = MutableStateFlow(getInitialStatus())
    override val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus

    override fun getInitialStatus(): PermissionStatus {
        val hasReadPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED

        return if (hasReadPermission && hasWritePermission) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }
}
