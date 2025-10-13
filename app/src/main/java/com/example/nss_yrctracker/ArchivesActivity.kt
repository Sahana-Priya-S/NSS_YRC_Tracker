package com.example.nss_yrctracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ArchivesActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArchivedEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archives)

        recyclerView = findViewById(R.id.archivesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ArchivedEventAdapter(emptyList())
        recyclerView.adapter = adapter

        loadArchivedEvents()
    }

    private fun loadArchivedEvents() {
        firestore.collection("events")
            .whereIn("status", listOf("completed", "archived"))
            .addSnapshotListener { snapshot, _ ->
                val archivedEvents = snapshot?.toObjects(Event::class.java) ?: emptyList()
                adapter.updateEvents(archivedEvents)
            }
    }
}