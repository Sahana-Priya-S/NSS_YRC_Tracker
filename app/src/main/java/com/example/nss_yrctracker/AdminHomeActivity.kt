package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHomeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adminEventAdapter: AdminEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        // Logout button
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Navigation buttons
        val addEventButton = findViewById<Button>(R.id.addEventButton)
        val manageEventsButton = findViewById<Button>(R.id.manageEventsButton)
        val approvalsButton = findViewById<Button>(R.id.approvalsButton)

        addEventButton.setOnClickListener {
            startActivity(Intent(this, AddEventActivity::class.java))
        }

        manageEventsButton.setOnClickListener {
            startActivity(Intent(this, ManageEventsActivity::class.java))
        }

        val archivesButton = findViewById<Button>(R.id.archivesButton)
        archivesButton.setOnClickListener {
            startActivity(Intent(this, ArchivesActivity::class.java))
        }

        approvalsButton.setOnClickListener {
            startActivity(Intent(this, ApprovalsActivity::class.java))
        }

        setupRecyclerView()
        loadEvents()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.adminEventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adminEventAdapter = AdminEventAdapter(emptyList())
        recyclerView.adapter = adminEventAdapter
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
        val today = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Use "dd" for day

        for (event in events) {
            if (event.id.isEmpty() || event.date.isEmpty()) continue // Skip if data is invalid

            try {
                val eventDate = dateFormat.parse(event.date)
                if (eventDate != null && eventDate.before(today)) {
                    firestore.collection("events").document(event.id)
                        .update("status", "archived")
                }
            } catch (e: Exception) {
                // This will catch any events with incorrectly formatted dates
            }
        }
    }
}