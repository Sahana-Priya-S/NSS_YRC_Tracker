package com.example.nss_yrctracker

data class Event(
    var id: String = "",
    val title: String = "",
    val date: String = "", // We'll assume a "YYYY-MM-DD" format
    val description: String = "",
    var status: String = "active" // Can be "active", "completed", or "archived"
)