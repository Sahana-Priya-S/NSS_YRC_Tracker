package com.example.nss_yrctracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ManageEventsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ManageEventAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // FIX: Ensure this points to 'fragment_manage_events'
        val view = inflater.inflate(R.layout.fragment_manage_events, container, false)

        // FIX: Matching the ID in the XML below
        recyclerView = view.findViewById(R.id.recyclerManageEvents)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // FIX: Passing the missing parameter (the click logic)
        adapter = ManageEventAdapter(emptyList()) { event, newStatus ->
            updateEventStatus(event, newStatus)
        }

        recyclerView.adapter = adapter

        loadActiveEvents()

        return view
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
                if (context != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }
}