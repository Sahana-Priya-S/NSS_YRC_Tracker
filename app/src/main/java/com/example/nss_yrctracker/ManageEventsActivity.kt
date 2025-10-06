package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ManageEventsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_events)

        recyclerView = findViewById(R.id.manageEventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ManageEventAdapter(emptyList()) { event ->
            deleteEvent(event)
        }
        recyclerView.adapter = adapter

        loadEvents()
    }

    private fun loadEvents() {
        firestore.collection("events").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
            adapter.updateEvents(events)
        }
    }

    private fun deleteEvent(event: Event) {
        if (event.id.isEmpty()) {
            Toast.makeText(this, "Cannot delete event with missing ID.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("events").document(event.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "'${event.title}' deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show()
            }
    }
}