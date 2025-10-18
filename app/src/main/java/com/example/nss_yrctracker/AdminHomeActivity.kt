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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdminHomeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adminEventAdapter: AdminEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        setupButtons()
        setupRecyclerView()
        loadEvents()
    }

    private fun setupButtons() {
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val addEventButton = findViewById<Button>(R.id.addEventButton)
        val manageEventsButton = findViewById<Button>(R.id.manageEventsButton)
        val approvalsButton = findViewById<Button>(R.id.approvalsButton)
        val archivesButton = findViewById<Button>(R.id.archivesButton)

        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        addEventButton.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }
        manageEventsButton.setOnClickListener {
            startActivity(Intent(this, ManageEventsActivity::class.java))
        }
        approvalsButton.setOnClickListener {
            startActivity(Intent(this, ApprovalsActivity::class.java))
        }
        archivesButton.setOnClickListener {
            startActivity(Intent(this, ArchivesActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.adminEventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adminEventAdapter = AdminEventAdapter(
            emptyList(),
            onStartClick = { event -> updateAttendanceStatus(event, true) },
            onStopClick = { event -> updateAttendanceStatus(event, false) }
        )
        recyclerView.adapter = adminEventAdapter
    }

    private fun updateAttendanceStatus(event: Event, isActive: Boolean) {
        if (event.id.isEmpty()) {
            Toast.makeText(this, "Cannot update event without an ID.", Toast.LENGTH_SHORT).show()
            return
        }
        firestore.collection("events").document(event.id)
            .update("attendanceActive", isActive)
            .addOnSuccessListener {
                val status = if (isActive) "started" else "stopped"
                Toast.makeText(this, "Attendance ${status} for '${event.title}'", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update attendance status.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadEvents() {
        firestore.collection("events")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, _ ->
                val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
                adminEventAdapter.updateEvents(events)
                checkForPastEventsAndArchive(events)
            }
    }

    private fun checkForPastEventsAndArchive(events: List<Event>) {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (event in events) {
            if (event.id.isEmpty() || event.date.isEmpty()) continue

            try {
                val eventDate = dateFormat.parse(event.date)
                if (eventDate != null && eventDate.before(startOfToday)) {
                    firestore.collection("events").document(event.id)
                        .update("status", "archived")
                }
            } catch (e: Exception) {
                // Ignore events with incorrectly formatted dates
            }
        }
    }
}