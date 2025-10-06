package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var eventTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        eventTitle = intent.getStringExtra("EVENT_TITLE") ?: ""
        if (eventTitle.isEmpty()) {
            finish()
            return
        }

        val titleEditText = findViewById<EditText>(R.id.eventTitleEditText)
        val dateEditText = findViewById<EditText>(R.id.eventDateEditText)
        val descriptionEditText = findViewById<EditText>(R.id.eventDescriptionEditText)
        val saveButton = findViewById<Button>(R.id.saveChangesButton)
        val recyclerView = findViewById<RecyclerView>(R.id.registeredStudentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load existing event data
        firestore.collection("events").document(eventTitle).get().addOnSuccessListener { doc ->
            val event = doc.toObject(Event::class.java)
            if (event != null) {
                titleEditText.setText(event.title)
                dateEditText.setText(event.date)
                descriptionEditText.setText(event.description)
            }
        }

        // Save button listener
        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString()
            val newDate = dateEditText.text.toString()
            val newDescription = descriptionEditText.text.toString()
            updateEvent(newTitle, newDate, newDescription)
        }

        // Load registered students
        loadRegisteredStudents(recyclerView)
    }

    private fun updateEvent(title: String, date: String, description: String) {
        val updatedEvent = Event(title, date, description)
        firestore.collection("events").document(eventTitle) // Use original title to find the doc
            .set(updatedEvent)
            .addOnSuccessListener {
                Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun loadRegisteredStudents(recyclerView: RecyclerView) {
        firestore.collection("registrations").whereEqualTo("eventTitle", eventTitle)
            .get()
            .addOnSuccessListener { snapshot ->
                val studentIds = snapshot.documents.mapNotNull { it.getString("userId") }
                // For simplicity, we'll just display the user IDs.
                // In a real app, you would fetch the user's name from a "users" collection.
                val adapter = RegisteredStudentAdapter(studentIds)
                recyclerView.adapter = adapter
            }
    }
}