package com.example.nss_yrctracker

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class AttendanceReportActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var eventSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var reportAdapter: AttendanceReportAdapter
    private var allEvents = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_report)

        eventSpinner = findViewById(R.id.eventSpinnerReport)
        recyclerView = findViewById(R.id.attendanceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        reportAdapter = AttendanceReportAdapter(emptyList())
        recyclerView.adapter = reportAdapter

        loadAllEventsIntoSpinner()
    }

    private fun loadAllEventsIntoSpinner() {
        firestore.collection("events").get()
            .addOnSuccessListener { snapshot ->
                allEvents = snapshot.toObjects(Event::class.java).toMutableList()
                // Sort events perhaps by date or title if needed
                val eventTitles = allEvents.map { it.title }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventTitles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                eventSpinner.adapter = adapter

                eventSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val selectedEvent = allEvents[position]
                        loadAttendanceForEvent(selectedEvent.id)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
    }

    private fun loadAttendanceForEvent(eventId: String) {
        if (eventId.isEmpty()) return

        firestore.collection("attendance")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { attendanceSnapshot ->
                val userIds = attendanceSnapshot.documents.mapNotNull { it.getString("userId") }

                if (userIds.isNotEmpty()) {
                    // Fetch user emails based on the user IDs
                    firestore.collection("users").whereIn(FieldPath.documentId(), userIds).get()
                        .addOnSuccessListener { userSnapshot ->
                            val emails = userSnapshot.documents.mapNotNull { it.getString("email") }
                            reportAdapter.updateStudents(emails)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to load student details.", Toast.LENGTH_SHORT).show()
                            reportAdapter.updateStudents(emptyList()) // Clear list on error
                        }
                } else {
                    reportAdapter.updateStudents(listOf("No attendance records found."))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load attendance records.", Toast.LENGTH_SHORT).show()
                reportAdapter.updateStudents(emptyList()) // Clear list on error
            }
    }
}