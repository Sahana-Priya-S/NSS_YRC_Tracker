package com.example.nss_yrctracker

data class Achievement(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "" // URL to an icon image stored perhaps in Firebase Storage
)