package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        approvalsButton.setOnClickListener {
            // TODO: Open approvals screen
        }

        // Setup for the events list
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
        firestore.collection("events").addSnapshotListener { snapshot, _ ->
            val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
            adminEventAdapter.updateEvents(events)
        }
    }
}