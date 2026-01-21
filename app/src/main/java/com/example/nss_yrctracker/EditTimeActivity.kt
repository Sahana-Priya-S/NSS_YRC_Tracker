package com.example.nss_yrctracker

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class EditTimeActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var btnSave: Button

    // Quick Extend Buttons
    private lateinit var btn15: Button
    private lateinit var btn30: Button
    private lateinit var btn60: Button

    // Data & Logic
    private val db = FirebaseFirestore.getInstance()
    private var eventId: String = ""
    private var startTime: String = ""
    private var endTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_time)

        // 1. Get Event ID
        eventId = intent.getStringExtra("eventId") ?: ""

        // 2. Initialize Views
        tvStartTime = findViewById(R.id.tvStartTime)
        tvEndTime = findViewById(R.id.tvEndTime)
        btnSave = findViewById(R.id.btnSaveTime)

        btn15 = findViewById(R.id.btnExtend15)
        btn30 = findViewById(R.id.btnExtend30)
        btn60 = findViewById(R.id.btnExtend60)

        // 3. Load existing times from Firebase
        loadEventTimes()

        // 4. Setup Time Pickers (Clicking the text opens the clock)
        tvStartTime.setOnClickListener { showTimePicker(isStart = true) }
        tvEndTime.setOnClickListener { showTimePicker(isStart = false) }

        // 5. Setup Quick Extend Buttons
        btn15.setOnClickListener { extendEndTime(15) }
        btn30.setOnClickListener { extendEndTime(30) }
        btn60.setOnClickListener { extendEndTime(60) }

        // 6. Setup Save Button
        btnSave.setOnClickListener { saveTimes() }
    }

    private fun loadEventTimes() {
        if (eventId.isEmpty()) return

        db.collection("events").document(eventId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val event = document.toObject(Event::class.java)
                    if (event != null) {
                        startTime = event.startTime
                        endTime = event.endTime

                        // Update UI if data exists
                        if (startTime.isNotEmpty()) tvStartTime.text = startTime
                        if (endTime.isNotEmpty()) tvEndTime.text = endTime
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showTimePicker(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val picker = TimePickerDialog(this,
            { _, hour, minute ->
                val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

                if (isStart) {
                    startTime = timeStr
                    tvStartTime.text = timeStr
                } else {
                    endTime = timeStr
                    tvEndTime.text = timeStr
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )
        picker.show()
    }

    // --- NEW FEATURE: Quick Extend Logic ---
    private fun extendEndTime(minutesToAdd: Int) {
        if (endTime.isEmpty()) {
            Toast.makeText(this, "Set an End Time first", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // 1. Parse current End Time (e.g., "14:30")
            val parts = endTime.split(":")
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            calendar.set(Calendar.MINUTE, parts[1].toInt())

            // 2. Add minutes
            calendar.add(Calendar.MINUTE, minutesToAdd)

            // 3. Format back to String (e.g., "14:45")
            val newHour = calendar.get(Calendar.HOUR_OF_DAY)
            val newMinute = calendar.get(Calendar.MINUTE)
            val newTimeStr = String.format(Locale.getDefault(), "%02d:%02d", newHour, newMinute)

            // 4. Update UI and Variable
            endTime = newTimeStr
            tvEndTime.text = newTimeStr

            Toast.makeText(this, "Extended by $minutesToAdd mins", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error calculating time", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTimes() {
        if (eventId.isEmpty()) return

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val updates = mapOf(
            "startTime" to startTime,
            "endTime" to endTime
        )

        db.collection("events").document(eventId).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Time Updated Successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close screen
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
                btnSave.text = "Update Time"
            }
    }
}