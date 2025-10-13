package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditEventActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var eventId: String // We will use the unique ID

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

        // Load current event data using the unique ID
        firestore.collection("events").document(eventId).get()
            .addOnSuccessListener { doc ->
                val event = doc.toObject(Event::class.java)
                event?.let {
                    titleEditText.setText(it.title)
                    dateEditText.setText(it.date)
                    descriptionEditText.setText(it.description)
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

            // Create the updated event object, keeping the original ID
            val updatedEvent = Event(eventId, newTitle, newDate, newDescription)

            // **FIX #3**: Use the unique ID to update the document in Firestore
            firestore.collection("events").document(eventId).set(updatedEvent)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update event.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}