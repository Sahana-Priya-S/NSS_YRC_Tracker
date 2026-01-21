package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class EventParticipantsActivity : AppCompatActivity() {

    private lateinit var attendedAdapter: EventParticipantsAdapter
    private lateinit var registeredAdapter: EventParticipantsAdapter
    private lateinit var attendedTitleTextView: TextView
    private lateinit var registeredTitleTextView: TextView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_participants)

        // Initialize Views and Adapters
        registeredTitleTextView = findViewById(R.id.registeredTitleTextView)
        attendedTitleTextView = findViewById(R.id.attendedTitleTextView)

        val regRecyclerView = findViewById<RecyclerView>(R.id.registeredRecyclerView)
        val attRecyclerView = findViewById<RecyclerView>(R.id.attendedRecyclerView)

        registeredAdapter = EventParticipantsAdapter(emptyList())
        attendedAdapter = EventParticipantsAdapter(emptyList())

        regRecyclerView.layoutManager = LinearLayoutManager(this)
        attRecyclerView.layoutManager = LinearLayoutManager(this)

        regRecyclerView.adapter = registeredAdapter
        attRecyclerView.adapter = attendedAdapter

        val eventId = intent.getStringExtra("EVENT_ID") ?: return
        loadRegisteredAndAttended(eventId)
    }

    private fun loadRegisteredAndAttended(eventId: String) {
        // Load Registrations
        firestore.collection("registrations").whereEqualTo("eventId", eventId).get()
            .addOnSuccessListener { snapshot ->
                val emails = snapshot.documents.map { it.getString("studentEmail") ?: "Unknown" }
                registeredTitleTextView.text = "Registered Students: (${emails.size})"
                registeredAdapter.updateStudents(emails)
            }

        // Load Attendance
        firestore.collection("events").document(eventId).collection("attendance").get()
            .addOnSuccessListener { snapshot ->
                val ids = snapshot.documents.map { it.id }
                attendedTitleTextView.text = "Attended Students: (${ids.size})"
                attendedAdapter.updateStudents(ids)
            }
    }
}