package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // **FIX #2**: Update the adapter initialization to pass all click handlers
        eventAdapter = EventAdapter(
            emptyList(),
            registeredEvents,
            onEventClick = { event ->
                // Open the detail screen when an event is clicked
                val intent = Intent(context, EventDetailActivity::class.java).apply {
                    putExtra("EVENT_ID", event.id)
                }
                startActivity(intent)
            },
            onRegisterClick = { event ->
                // Handle the registration click
                registerForEvent(event)
            }
        )
        recyclerView.adapter = eventAdapter

        loadRegisteredEventsAndThenAllEvents()

        return view
    }

    private fun loadRegisteredEventsAndThenAllEvents() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("registrations").whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    doc.getString("eventTitle")?.let { registeredEvents.add(it) }
                }
                loadEvents()
            }
    }

    private fun loadEvents() {
        firestore.collection("events")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, _ ->
                val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
                eventAdapter.updateEvents(events, registeredEvents)
            }
    }

    private fun registerForEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return
        val registrationDoc = mapOf("userId" to userId, "eventTitle" to event.title)

        firestore.collection("registrations").document("$userId-${event.title}")
            .set(registrationDoc)
            .addOnSuccessListener {
                Toast.makeText(context, "Registered for ${event.title}", Toast.LENGTH_SHORT).show()
                registeredEvents.add(event.title)
                eventAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to register", Toast.LENGTH_SHORT).show()
            }
    }
}