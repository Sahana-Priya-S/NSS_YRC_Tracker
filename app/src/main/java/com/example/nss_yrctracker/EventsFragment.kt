package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventsFragment : Fragment() {

    private lateinit var eventAdapter: EventAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val registeredEvents = mutableSetOf<String>()
    private val TAG = "EventsFragment" // Tag for logging

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        Log.d(TAG, "Initializing EventAdapter")
        eventAdapter = EventAdapter(
            emptyList(),
            registeredEvents,
            onEventClick = { event ->
                Log.d(TAG, "Event clicked: ${event.title}")
                if (event.id.isEmpty()) {
                    Toast.makeText(context, "Cannot view details: Event data incomplete.", Toast.LENGTH_SHORT).show()
                    return@EventAdapter
                }
                val intent = Intent(context, EventDetailActivity::class.java).apply {
                    putExtra("EVENT_ID", event.id)
                }
                startActivity(intent)
            },
            onRegisterClick = { event ->
                Log.d(TAG, "Register clicked for: ${event.title}")
                registerForEvent(event)
            }
        )
        recyclerView.adapter = eventAdapter
        Log.d(TAG, "Adapter set on RecyclerView")

        loadRegisteredEventsAndThenAllEvents()

        return view
    }

    private fun loadRegisteredEventsAndThenAllEvents() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not logged in, cannot load registrations.")
            return
        }
        Log.d(TAG, "Loading registered events for user: $userId")
        firestore.collection("registrations").whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                registeredEvents.clear() // Clear old data
                snapshot.documents.forEach { doc ->
                    doc.getString("eventTitle")?.let { registeredEvents.add(it) }
                }
                Log.d(TAG, "Registered events loaded: ${registeredEvents.size}. Now loading all events.")
                loadEvents() // Now load all events
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading registered events", e)
                Toast.makeText(context, "Error loading your registrations.", Toast.LENGTH_SHORT).show()
                loadEvents() // Still try to load all events even if registrations fail
            }
    }

    private fun loadEvents() {
        Log.d(TAG, "Setting up Firestore listener for active events.")
        firestore.collection("events")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching events", error)
                    Toast.makeText(context, "Error loading events.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "Snapshot is null.")
                    return@addSnapshotListener
                }

                Log.d(TAG, "Firestore snapshot received. Number of documents: ${snapshot.size()}")
                val events = snapshot.toObjects(Event::class.java)
                Log.d(TAG, "Mapped to Event objects. Number of events: ${events.size}")

                // Log the titles of the events fetched
                if (events.isNotEmpty()) {
                    Log.d(TAG, "Events fetched: ${events.joinToString { it.title }}")
                } else {
                    Log.d(TAG, "No active events found in snapshot.")
                }

                eventAdapter.updateEvents(events, registeredEvents)
                Log.d(TAG, "Adapter updated with ${events.size} events.")
            }
    }

    private fun registerForEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return
        Log.d(TAG, "Attempting registration for event: ${event.title}, user: $userId")
        val registrationDoc = mapOf("userId" to userId, "eventTitle" to event.title)


        // Using event title for registration ID might cause issues if titles change.
        // Consider using event.id if possible, requires changing registration logic/queries.
        firestore.collection("registrations").document("$userId-${event.title}")
            .set(registrationDoc)
            .addOnSuccessListener {
                Log.d(TAG, "Registration successful for: ${event.title}")
                Toast.makeText(context, "Registered for ${event.title}", Toast.LENGTH_SHORT).show()
                registeredEvents.add(event.title)
                eventAdapter.notifyDataSetChanged() // Consider more specific update later
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Registration failed for: ${event.title}", e)
                Toast.makeText(context, "Failed to register", Toast.LENGTH_SHORT).show()
            }
    }
}