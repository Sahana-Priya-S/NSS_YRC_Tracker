package com.example.nss_yrctracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EventsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventSummaryAdapter
    private val eventsList = mutableListOf<Event>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)

        recyclerView = view.findViewById(R.id.recyclerEvents)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadActiveEvents()

        return view
    }

    private fun loadActiveEvents() {
        // Query only events that are NOT archived
        db.collection("events")
            .whereEqualTo("isArchived", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("EventsFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // FIXED: Using .copy() instead of .apply to fix 'val cannot be reassigned'
                    val list = snapshot?.documents?.mapNotNull { doc ->
                        // USE PARENTHESES () for copy, not curly braces {}
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    eventsList.clear()
                    eventsList.addAll(list)

                    // Using your existing EventSummaryAdapter
                    adapter = EventSummaryAdapter(eventsList)
                    recyclerView.adapter = adapter
                }
            }
    }
}