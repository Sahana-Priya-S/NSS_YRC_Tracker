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
    private val registeredEvents = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_home)

        setupRecyclerView()

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        val submitProofButton = findViewById<Button>(R.id.submitProofButton)
        submitProofButton.setOnClickListener {
            startActivity(Intent(this, SubmitProofActivity::class.java))
        }

        loadRegisteredEventsAndThenAllEvents()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventAdapter(emptyList(), registeredEvents) { event ->
            registerForEvent(event)
        }
        recyclerView.adapter = eventAdapter
    }

    private fun loadRegisteredEventsAndThenAllEvents() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("registrations").whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    doc.getString("eventTitle")?.let { registeredEvents.add(it) }
                }
                loadEvents() // Now load all events
            }
    }


    private fun loadEvents() {
        firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { it.toObject(Event::class.java) } ?: emptyList()
                eventAdapter.updateEvents(events, registeredEvents)
            }
    }

    private fun registerForEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return
        val registrationDoc = mapOf("userId" to userId, "eventTitle" to event.title)

        firestore.collection("registrations")
            .document("$userId-${event.title}")
            .set(registrationDoc)
            .addOnSuccessListener {
                Toast.makeText(this, "Registered for ${event.title}", Toast.LENGTH_SHORT).show()
                // Add to our local list and update the adapter
                registeredEvents.add(event.title)
                eventAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to register", Toast.LENGTH_SHORT).show()
            }
    }
}