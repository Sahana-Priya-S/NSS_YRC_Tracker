package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddEventActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        val titleEditText = findViewById<EditText>(R.id.eventTitleEditText)
        val dateEditText = findViewById<EditText>(R.id.eventDateEditText)
        val descriptionEditText = findViewById<EditText>(R.id.eventDescriptionEditText)
        val saveButton = findViewById<Button>(R.id.saveEventButton)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val date = dateEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (title.isNotEmpty() && date.isNotEmpty() && description.isNotEmpty()) {
                saveEvent(title, date, description)
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveEvent(title: String, date: String, description: String) {
        val eventsCollection = firestore.collection("events")
        val eventDoc = eventsCollection.document()

        val newEvent = Event(
            id = eventDoc.id,
            title = title,
            date = date,
            description = description
        )

        // Set the data for the new document
        eventDoc.set(newEvent)
            .addOnSuccessListener {
                Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after saving
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add event: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}