package com.example.nss_yrctracker
data class Event(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    val location: String = "",
    val status: String = "STOPPED",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0,
    val isArchived: Boolean = false,
    val allottedHours: Int = 0,
    val startTime: String = "",
    val endTime: String = ""
)