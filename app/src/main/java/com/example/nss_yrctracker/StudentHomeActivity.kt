package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentHomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_home)

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val eventList = mutableListOf<Event>()

        eventAdapter = EventAdapter(
            eventList,
            registeredTitles = mutableSetOf()
        ) { event ->
            Toast.makeText(this, "Registered for ${event.title}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = eventAdapter


        // Logout button
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        loadEvents()
    }

    private fun loadEvents() {
        firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                eventAdapter.updateEvents(events)
            }
    }

    private fun registerForEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("registrations")
            .document("$userId-${event.title}")
            .set(mapOf("userId" to userId, "eventTitle" to event.title))
            .addOnSuccessListener {
                Toast.makeText(this, "Registered for ${event.title}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEvents(events: List<Event>) {
        eventAdapter = EventAdapter(
            events,
            registeredTitles
        ) { event ->
            Toast.makeText(this, "Registered for ${event.title}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = eventAdapter
    }

}
