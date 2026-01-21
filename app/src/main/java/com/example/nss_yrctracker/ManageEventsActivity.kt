package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ManageEventsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageEventAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ERROR FIX: Ensure this XML file exists and has the RecyclerView ID
        setContentView(R.layout.activity_manage_events)

        // ERROR FIX: Make sure the ID in XML is actually 'recyclerManageEvents'
        recyclerView = findViewById(R.id.recyclerManageEvents)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ERROR FIX: We MUST pass the { logic } block here now!
        adapter = ManageEventAdapter(emptyList()) { event, newStatus ->
            updateEventStatus(event, newStatus)
        }

        recyclerView.adapter = adapter

        loadActiveEvents()
    }

    private fun loadActiveEvents() {
        db.collection("events")
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val eventList = mutableListOf<Event>()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val event = doc.toObject(Event::class.java).copy(id = doc.id)
                        eventList.add(event)
                    }
                    adapter.updateEvents(eventList)
                }
            }
    }

    private fun updateEventStatus(event: Event, newStatus: String) {
        if (event.id.isEmpty()) return

        db.collection("events").document(event.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                val msg = if (newStatus == "ACTIVE") "Attendance Started" else "Attendance Stopped"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }
}