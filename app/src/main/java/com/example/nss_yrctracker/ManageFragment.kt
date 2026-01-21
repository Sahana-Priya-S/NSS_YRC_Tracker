package com.example.nss_yrctracker

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class ManageFragment : Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var tvStatus: TextView
    private lateinit var btnToggle: Button
    private lateinit var btnEnd: Button
    private lateinit var btnDelete: Button

    private val db = FirebaseFirestore.getInstance()
    private var eventsList = mutableListOf<String>()
    private var eventsIds = mutableListOf<String>()
    private var selectedEventId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage, container, false)

        spinner = view.findViewById(R.id.spnEventSelector)
        tvStatus = view.findViewById(R.id.tvCurrentStatus)
        btnToggle = view.findViewById(R.id.btnToggleStatus)
        btnEnd = view.findViewById(R.id.btnEndEvent)
        btnDelete = view.findViewById(R.id.btnDeleteEvent)

        loadEvents()

        btnToggle.setOnClickListener { toggleEventStatus() }
        btnEnd.setOnClickListener { confirmEnd() }
        btnDelete.setOnClickListener { confirmDelete() }

        return view
    }

    private fun loadEvents() {
        db.collection("events")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                eventsList.clear()
                eventsIds.clear()

                for (doc in documents) {
                    eventsList.add(doc.getString("title") ?: "Unknown")
                    eventsIds.add(doc.id)
                }

                if (context != null) {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, eventsList)
                    spinner.adapter = adapter
                }

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedEventId = eventsIds[position]
                        checkEventStatus(selectedEventId!!)
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
    }

    private fun checkEventStatus(docId: String) {
        db.collection("events").document(docId).get().addOnSuccessListener { document ->
            val status = document.getString("status") ?: "STOPPED"
            updateUI(status)
        }
    }

    private fun toggleEventStatus() {
        if (selectedEventId == null) return

        val currentText = tvStatus.text.toString()
        // If it's already ended, don't allow toggling
        if (currentText == "ENDED") {
            Toast.makeText(context, "This event has ended.", Toast.LENGTH_SHORT).show()
            return
        }

        val newStatus = if (currentText == "ACTIVE") "STOPPED" else "ACTIVE"

        db.collection("events").document(selectedEventId!!)
            .update("status", newStatus)
            .addOnSuccessListener {
                updateUI(newStatus)
                Toast.makeText(context, "Event is now $newStatus", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmEnd() {
        if (selectedEventId == null) return

        db.collection("events").document(selectedEventId!!)
            .update("status", "ENDED")
            .addOnSuccessListener {
                updateUI("ENDED")
                Toast.makeText(context, "Event Ended Permanently", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete() {
        if (selectedEventId == null) return

        AlertDialog.Builder(context)
            .setTitle("Delete Event?")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("events").document(selectedEventId!!)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Event Deleted", Toast.LENGTH_SHORT).show()
                        loadEvents() // Refresh list
                        tvStatus.text = "---"
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUI(status: String) {
        when (status) {
            "ACTIVE" -> {
                tvStatus.text = "ACTIVE"
                tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
                btnToggle.text = "STOP EVENT"
                btnToggle.setBackgroundColor(Color.parseColor("#F44336")) // Red
                btnToggle.isEnabled = true
                btnEnd.isEnabled = true
            }
            "STOPPED" -> {
                tvStatus.text = "STOPPED"
                tvStatus.setTextColor(Color.parseColor("#F44336")) // Red
                btnToggle.text = "START EVENT"
                btnToggle.setBackgroundColor(Color.parseColor("#065F46")) // Green
                btnToggle.isEnabled = true
                btnEnd.isEnabled = true
            }
            "ENDED" -> {
                tvStatus.text = "ENDED"
                tvStatus.setTextColor(Color.parseColor("#FF9800")) // Orange
                btnToggle.text = "EVENT ENDED"
                btnToggle.setBackgroundColor(Color.GRAY)
                btnToggle.isEnabled = false
                btnEnd.isEnabled = false
            }
        }
    }
}