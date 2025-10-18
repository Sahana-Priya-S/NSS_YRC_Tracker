package com.example.nss_yrctracker

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTimeActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var eventId: String
    private var startTime: Long = 0
    private var endTime: Long = 0

    private lateinit var currentTimeWindowTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_time)

        eventId = intent.getStringExtra("EVENT_ID") ?: ""
        if (eventId.isEmpty()) {
            finish()
            return
        }

        currentTimeWindowTextView = findViewById(R.id.currentTimeWindowTextView)
        val editStartTimeButton = findViewById<Button>(R.id.editStartTimeButton)
        val editEndTimeButton = findViewById<Button>(R.id.editEndTimeButton)
        val extendTimeEditText = findViewById<EditText>(R.id.extendTimeEditText)
        val extendTimeButton = findViewById<Button>(R.id.extendTimeButton)
        val saveTimeButton = findViewById<Button>(R.id.saveTimeButton)

        // Load the current times
        loadEventTimes()

        editStartTimeButton.setOnClickListener { showTimePicker(true) }
        editEndTimeButton.setOnClickListener { showTimePicker(false) }

        extendTimeButton.setOnClickListener {
            val minutes = extendTimeEditText.text.toString().toIntOrNull()
            if (minutes != null) {
                extendEndTime(minutes)
            } else {
                Toast.makeText(this, "Please enter a valid number of minutes.", Toast.LENGTH_SHORT).show()
            }
        }

        saveTimeButton.setOnClickListener {
            saveTimeChanges()
        }
    }

    private fun loadEventTimes() {
        firestore.collection("events").document(eventId).get().addOnSuccessListener { doc ->
            val event = doc.toObject(Event::class.java)
            if (event != null) {
                startTime = event.attendanceStartTime
                endTime = event.attendanceEndTime
                updateTimeWindowText()
            }
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }.timeInMillis

            if (isStartTime) {
                startTime = selectedTime
            } else {
                endTime = selectedTime
            }
            updateTimeWindowText()
        }, hour, minute, false).show()
    }

    private fun extendEndTime(minutes: Int) {
        endTime += minutes * 60 * 1000 // Convert minutes to milliseconds
        updateTimeWindowText()
        Toast.makeText(this, "End time extended by $minutes minutes.", Toast.LENGTH_SHORT).show()
    }

    private fun updateTimeWindowText() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startText = if (startTime > 0) timeFormat.format(Date(startTime)) else "Not Set"
        val endText = if (endTime > 0) timeFormat.format(Date(endTime)) else "Not Set"
        currentTimeWindowTextView.text = "Current Window: $startText - $endText"
    }

    private fun saveTimeChanges() {
        if (endTime <= startTime) {
            Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("events").document(eventId)
            .update(mapOf(
                "attendanceStartTime" to startTime,
                "attendanceEndTime" to endTime
            ))
            .addOnSuccessListener {
                Toast.makeText(this, "Time window updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update time window.", Toast.LENGTH_SHORT).show()
            }
    }
}