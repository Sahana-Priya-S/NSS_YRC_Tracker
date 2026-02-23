package com.example.nss_yrctracker

import com.google.firebase.firestore.PropertyName

data class AttendanceRecord(
    val eventId: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var studentName: String = "",

    val studentId: String = "",
    val status: String = "",
    val timestamp: Long = 0
)