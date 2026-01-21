package com.example.nss_yrctracker
data class Submission(
    val id: String = "",
    val userId: String = "",
    val eventTitle: String = "",
    val summary: String = "",
    val imageUrl: String = "",
    val status: String = "PENDING",
    val timestamp: Long = 0
)