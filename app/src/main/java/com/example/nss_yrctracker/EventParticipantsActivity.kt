package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class EventParticipantsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var eventNameTitle: TextView
    private lateinit var attendedRecyclerView: RecyclerView
    private lateinit var registeredRecyclerView: RecyclerView
    private lateinit var attendedAdapter: AttendanceReportAdapter
    private lateinit var registeredAdapter: AttendanceReportAdapter

    // Add TextViews for titles
    private lateinit var attendedTitleTextView: TextView
    private lateinit var registeredTitleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_participants)

        eventNameTitle = findViewById(R.id.eventNameTitle)
        attendedRecyclerView = findViewById(R.id.attendedStudentsRecyclerView)
        registeredRecyclerView = findViewById(R.id.registeredStudentsRecyclerView)
        // Find the title TextViews by ID
        attendedTitleTextView = findViewById(R.id.attendedTitleTextView)
        registeredTitleTextView = findViewById(R.id.registeredTitleTextView)


        attendedRecyclerView.layoutManager = LinearLayoutManager(this)
        registeredRecyclerView.layoutManager = LinearLayoutManager(this)

        attendedAdapter = AttendanceReportAdapter(emptyList())
        registeredAdapter = AttendanceReportAdapter(emptyList())

        attendedRecyclerView.adapter = attendedAdapter
        registeredRecyclerView.adapter = registeredAdapter

        val eventId = intent.getStringExtra("EVENT_ID")
        val eventTitle = intent.getStringExtra("EVENT_TITLE")

        if (eventId == null || eventTitle == null) {
            Toast.makeText(this, "Error: Event details missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        eventNameTitle.text = "Participants for $eventTitle"
        loadAttendedStudents(eventId)
        loadRegisteredStudents(eventTitle)
    }

    private fun loadAttendedStudents(eventId: String) {
        firestore.collection("attendance")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { attendanceSnapshot ->
                val userIds = attendanceSnapshot.documents.mapNotNull { it.getString("userId") }
                // Update title with count
                attendedTitleTextView.text = "Attended Students: (${userIds.size})"
                fetchAndDisplayEmails(userIds, attendedAdapter, "No attendance records found.")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load attendance.", Toast.LENGTH_SHORT).show()
                attendedTitleTextView.text = "Attended Students: (Error)"
                attendedAdapter.updateStudents(emptyList())
            }
    }

    private fun loadRegisteredStudents(eventTitle: String) {
        firestore.collection("registrations")
            .whereEqualTo("eventTitle", eventTitle)
            .get()
            .addOnSuccessListener { registrationSnapshot ->
                val userIds = registrationSnapshot.documents.mapNotNull { it.getString("userId") }
                // Update title with count
                registeredTitleTextView.text = "Registered Students: (${userIds.size})"
                fetchAndDisplayEmails(userIds, registeredAdapter, "No registrations found.")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load registrations.", Toast.LENGTH_SHORT).show()
                registeredTitleTextView.text = "Registered Students: (Error)"
                registeredAdapter.updateStudents(emptyList())
            }
    }

    private fun fetchAndDisplayEmails(userIds: List<String>, adapter: AttendanceReportAdapter, emptyMessage: String) {
        if (userIds.isNotEmpty()) {
            firestore.collection("users").whereIn(FieldPath.documentId(), userIds).get()
                .addOnSuccessListener { userSnapshot ->
                    val emails = userSnapshot.documents.mapNotNull { it.getString("email") }
                    adapter.updateStudents(emails)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load student emails.", Toast.LENGTH_SHORT).show()
                    adapter.updateStudents(emptyList())
                }
        } else {
            adapter.updateStudents(listOf(emptyMessage))
        }
    }
}