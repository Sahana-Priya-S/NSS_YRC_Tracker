package com.example.nss_yrctracker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ArchivesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventSummaryAdapter
    private val archivedEvents = mutableListOf<Event>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archives)

        // 1. Setup Toolbar/UI
        val btnBack = findViewById<android.view.View>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerArchives)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 2. Load only Archived Data
        loadArchivedEvents()
    }

    private fun loadArchivedEvents() {
        // Query filter: isArchived must be true
        db.collection("events")
            .whereEqualTo("isArchived", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                archivedEvents.clear()
                for (doc in documents) {
                    try {
                        // Map to Event model and include the document ID
                        val event = doc.toObject(Event::class.java).copy(id = doc.id)
                        archivedEvents.add(event)
                    } catch (e: Exception) {
                        Log.e("Archives", "Error mapping event", e)
                    }
                }

                // 3. Initialize Adapter
                adapter = EventSummaryAdapter(archivedEvents)
                recyclerView.adapter = adapter

                if (archivedEvents.isEmpty()) {
                    Toast.makeText(this, "No archived events found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Archives", "Error fetching data", e)
                Toast.makeText(this, "Failed to load archives", Toast.LENGTH_SHORT).show()
            }
    }
}