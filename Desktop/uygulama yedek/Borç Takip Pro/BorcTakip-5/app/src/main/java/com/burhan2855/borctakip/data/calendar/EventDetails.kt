package com.burhan2855.borctakip.data.calendar

data class EventDetails(
    val calendarId: Long,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val timezone: String,
    val hasAlarm: Boolean,
    val customAppUri: String
)