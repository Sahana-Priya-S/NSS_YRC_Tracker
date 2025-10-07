package com.example.nss_yrctracker

data class Submission(
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val summary: String = "",
    val imageUrl: String = "",
    var status: String = "Pending" // Can be "Pending", "Approved", or "Rejected"
)