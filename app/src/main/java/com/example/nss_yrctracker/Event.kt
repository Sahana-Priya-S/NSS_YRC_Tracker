package com.example.nss_yrctracker

data class Event(
    var id: String = "",
    val title: String = "",
    val date: String = "",
    val description: String = "",
    var status: String = "active",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var attendanceActive: Boolean = false,
    var attendanceStartTime: Long = 0,
    var attendanceEndTime: Long = 0
)