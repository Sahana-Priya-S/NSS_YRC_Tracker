package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditEventActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var eventId: String // Change this to eventId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        eventId = intent.getStringExtra("EVENT_ID") ?: "" // Use EVENT_ID
        if (eventId.isEmpty()) {
            finish()
            return
        }

        val titleEditText = findViewById<EditText>(R.id.eventTitleEditText)
        val dateEditText = findViewById<EditText>(R.id.eventDateEditText)
        val descriptionEditText = findViewById<EditText>(R.id.eventDescriptionEditText)
        val saveButton = findViewById<Button>(R.id.saveChangesButton)

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
            val newTitle = titleEditText.text.toString()
            val newDate = dateEditText.text.toString()
            val newDescription = descriptionEditText.text.toString()

            // For simplicity, we assume the title (document ID) doesn't change.
            // A more complex app would handle title changes by deleting the old doc and creating a new one.
            val updatedEvent = Event(newTitle, newDate, newDescription)
            firestore.collection("events").document(eventId).set(updatedEvent)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}