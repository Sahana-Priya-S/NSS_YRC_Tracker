package com.example.nss_yrctracker

data class AttendanceRecord(
    val eventId: String = "",
    val studentName: String = "",
    val studentId: String = "",
    val status: String = "",
    val timestamp: Long = 0
)