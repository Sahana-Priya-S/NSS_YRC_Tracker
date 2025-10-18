package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditEventActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var eventId: String
    private var currentEvent: Event? = null

    private lateinit var startAttendanceButton: Button
    private lateinit var stopAttendanceButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        eventId = intent.getStringExtra("EVENT_ID") ?: ""
        if (eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val titleEditText = findViewById<EditText>(R.id.eventTitleEditText)
        val dateEditText = findViewById<EditText>(R.id.eventDateEditText)
        val descriptionEditText = findViewById<EditText>(R.id.eventDescriptionEditText)
        val saveButton = findViewById<Button>(R.id.saveChangesButton)
        startAttendanceButton = findViewById(R.id.startAttendanceButton)
        stopAttendanceButton = findViewById(R.id.stopAttendanceButton)

        // Load current event data
        firestore.collection("events").document(eventId).get()
            .addOnSuccessListener { doc ->
                currentEvent = doc.toObject(Event::class.java)
                currentEvent?.let {
                    titleEditText.setText(it.title)
                    dateEditText.setText(it.date)
                    descriptionEditText.setText(it.description)
                    updateAttendanceButtons(it.attendanceActive)
                }
            }

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString().trim()
            val newDate = dateEditText.text.toString().trim()
            val newDescription = descriptionEditText.text.toString().trim()

            if (newTitle.isEmpty() || newDate.isEmpty() || newDescription.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            updateEventDetails(newTitle, newDate, newDescription)
        }

        startAttendanceButton.setOnClickListener { updateAttendanceStatus(true) }
        stopAttendanceButton.setOnClickListener { updateAttendanceStatus(false) }
    }

    private fun updateAttendanceStatus(isActive: Boolean) {
        firestore.collection("events").document(eventId)
            .update("attendanceActive", isActive)
            .addOnSuccessListener {
                val status = if (isActive) "started" else "stopped"
                Toast.makeText(this, "Attendance has been $status.", Toast.LENGTH_SHORT).show()
                updateAttendanceButtons(isActive)
            }
    }

    private fun updateAttendanceButtons(isActive: Boolean) {
        startAttendanceButton.isEnabled = !isActive
        stopAttendanceButton.isEnabled = isActive
    }

    private fun updateEventDetails(title: String, date: String, description: String) {
        currentEvent?.let {
            val updatedEvent = it.copy(title = title, date = date, description = description)
            firestore.collection("events").document(eventId).set(updatedEvent)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event details updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update event details.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}