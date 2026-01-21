package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminEventsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminEventAdapter
    private val eventsList = mutableListOf<Event>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_events, container, false)

        recyclerView = view.findViewById(R.id.recyclerAdminEvents)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Adapter with the 5 specific action handlers
        adapter = AdminEventAdapter(
            events = eventsList,
            onStartClick = { event -> updateEventStatus(event, "ACTIVE") },
            onStopClick = { event -> updateEventStatus(event, "STOPPED") },
            onEditTimeClick = { event ->
                val intent = Intent(context, EditTimeActivity::class.java)
                intent.putExtra("eventId", event.id)
                startActivity(intent)
            },
            onCardClick = { event ->
                // Ensure IDs match for report generation
                val intent = Intent(context, AttendanceReportActivity::class.java)
                intent.putExtra("eventId", event.id)
                intent.putExtra("eventTitle", event.title)
                intent.putExtra("eventDate", event.date)
                startActivity(intent)
            },
            onArchiveClick = { event ->
                showArchiveConfirmation(event)
            }
        )
        recyclerView.adapter = adapter

        val fab: FloatingActionButton = view.findViewById(R.id.fabCreateEvent)
        fab.setOnClickListener {
            startActivity(Intent(context, AddEventActivity::class.java))
        }

        loadEvents()

        return view
    }

    private fun loadEvents() {
        // Query only active/stopped events (not archived)
        db.collection("events")
            .whereEqualTo("isArchived", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminEvents", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    eventsList.clear()
                    for (doc in snapshots) {
                        try {
                            // Map to Event.kt model ensuring ID is captured
                            val event = doc.toObject(Event::class.java).copy(id = doc.id)
                            eventsList.add(event)
                        } catch (err: Exception) {
                            Log.e("AdminEvents", "Mapping Error", err)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun updateEventStatus(event: Event, newStatus: String) {
        if (event.id.isEmpty()) return

        db.collection("events").document(event.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                val msg = if (newStatus == "ACTIVE") "Attendance Started" else "Attendance Stopped"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showArchiveConfirmation(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("End Event Permanently?")
            .setMessage("This will archive '${event.title}' and award ${event.allottedHours} hours to all present volunteers. This cannot be undone.")
            .setPositiveButton("Archive") { _, _ -> archiveEventPermanently(event) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun archiveEventPermanently(event: Event) {
        // Mark as archived so it leaves this fragment list
        db.collection("events").document(event.id)
            .update(
                "status", "ARCHIVED",
                "isArchived", true
            )
            .addOnSuccessListener {
                Toast.makeText(context, "Event Archived & Rewarding Hours...", Toast.LENGTH_SHORT).show()
                rewardHoursToStudents(event)
            }
    }

    private fun rewardHoursToStudents(event: Event) {
        val hoursToAward = event.allottedHours
        if (hoursToAward <= 0) return

        // Award only to students present in this specific event
        db.collection("attendance")
            .whereEqualTo("eventId", event.id)
            .whereEqualTo("status", "PRESENT")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val studentId = doc.getString("studentId") ?: continue
                    val userRef = db.collection("users").document(studentId)

                    // Transaction ensures data accuracy
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(userRef)
                        val currentHours = snapshot.getLong("totalHours") ?: 0
                        val currentEvents = snapshot.getLong("eventsAttended") ?: 0

                        transaction.update(userRef, "totalHours", currentHours + hoursToAward)
                        transaction.update(userRef, "eventsAttended", currentEvents + 1)
                    }.addOnFailureListener { e ->
                        Log.e("Reward", "Failed for student: $studentId", e)
                    }
                }
            }
    }
}