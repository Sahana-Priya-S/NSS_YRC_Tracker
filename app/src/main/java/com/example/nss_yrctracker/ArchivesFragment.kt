package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ArchivesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: EventAdapter
    private val db = FirebaseFirestore.getInstance()
    private val eventList = mutableListOf<Event>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_archives, container, false)

        // Sync IDs with XML
        recyclerView = view.findViewById(R.id.recyclerArchives)
        emptyView = view.findViewById(R.id.emptyArchiveView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        loadArchivedEvents()
        return view
    }

    private fun openAttendanceReport(eventId: String) {
        val intent = android.content.Intent(requireContext(), AttendanceReportActivity::class.java)
        intent.putExtra("EVENT_ID", eventId) // Optional: Pass the event ID to show a specific report
        startActivity(intent)
    }

    private fun loadArchivedEvents() {
        db.collection("events")
            .whereEqualTo("isArchived", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    eventList.clear()
                    for (doc in snapshot.documents) {
                        val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                        if (event != null) eventList.add(event)
                    }

                    // Force isAdmin = true to hide the registration button
                    adapter = EventAdapter(eventList, isAdmin = true) { clickedEvent ->
                        // Calling the function here makes it "used" and functional!
                        openAttendanceReport(clickedEvent.id)
                    }
                    recyclerView.adapter = adapter
                }
            }
    }
}