package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EventSummaryActivity : AppCompatActivity() {

    private lateinit var tvTotalPresent: TextView
    private lateinit var etSummary: EditText
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_summary)

        // 1. Initialize Views
        tvTotalPresent = findViewById(R.id.tvTotalPresent)
        etSummary = findViewById(R.id.etSummary)
        btnSave = findViewById(R.id.btnSaveReport)

        // 2. Get Event ID
        eventId = intent.getStringExtra("EVENT_ID") ?: ""
        if (eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID Not Found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 3. Load Data
        loadAttendanceCount()
        loadExistingSummary()

        // 4. Save Button Logic
        btnSave.setOnClickListener {
            saveSummary()
        }
    }

    private fun loadAttendanceCount() {
        // Query the 'attendance' collection where eventId matches
        db.collection("attendance")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { documents ->
                val count = documents.size()
                tvTotalPresent.text = "$count Students"
            }
            .addOnFailureListener {
                tvTotalPresent.text = "Error"
            }
    }

    private fun loadExistingSummary() {
        // Check if a report was already written
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val summary = document.getString("summaryReport")
                    if (!summary.isNullOrEmpty()) {
                        etSummary.setText(summary)
                    }
                }
            }
    }

    private fun saveSummary() {
        val summaryText = etSummary.text.toString().trim()

        if (summaryText.isEmpty()) {
            Toast.makeText(this, "Please write something first", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.text = "Saving..."
        btnSave.isEnabled = false

        // Update the Event Document with the new summary
        db.collection("events").document(eventId)
            .update("summaryReport", summaryText)
            .addOnSuccessListener {
                Toast.makeText(this, "Report Saved Successfully!", Toast.LENGTH_SHORT).show()
                btnSave.text = "SAVE REPORT"
                btnSave.isEnabled = true
                finish() // Close screen after saving
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Save", Toast.LENGTH_SHORT).show()
                btnSave.text = "SAVE REPORT"
                btnSave.isEnabled = true
            }
    }
}