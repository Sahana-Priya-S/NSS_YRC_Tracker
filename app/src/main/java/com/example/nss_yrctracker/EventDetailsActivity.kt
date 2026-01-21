package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EventDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        // 1. Get Data from Intent
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "No Title"
        val date = intent.getStringExtra("EXTRA_DATE") ?: "No Date"
        val status = intent.getStringExtra("EXTRA_STATUS") ?: "UNKNOWN"
        val desc = intent.getStringExtra("EXTRA_DESC") ?: "No Description provided."
        val location = intent.getStringExtra("EXTRA_LOC") ?: "TBD"

        // 2. Bind Views
        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailDate).text = date
        findViewById<TextView>(R.id.tvDetailStatus).text = status
        findViewById<TextView>(R.id.tvDetailDesc).text = desc
        findViewById<TextView>(R.id.tvDetailLocation).text = location

        // 3. Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // Close this screen
        }
    }
}