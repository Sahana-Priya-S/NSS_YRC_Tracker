package com.example.nss_yrctracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ManageEventAdapter(
    private var events: List<Event>,
    private val onStatusChange: (Event, String) -> Unit // The required parameter
) : RecyclerView.Adapter<ManageEventAdapter.ManageViewHolder>() {

    class ManageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Ensure these IDs match your 'item_manage_event.xml'
        val tvTitle: TextView = view.findViewById(R.id.eventTitle)
        val btnToggle: Button = view.findViewById(R.id.btnToggleAttendance)

        fun bind(event: Event, onStatusChange: (Event, String) -> Unit) {
            tvTitle.text = event.title

            // Visual Logic
            if (event.status == "ACTIVE") {
                btnToggle.text = "Stop"
                btnToggle.setBackgroundColor(Color.RED)
            } else {
                btnToggle.text = "Start"
                btnToggle.setBackgroundColor(Color.parseColor("#065F46"))
            }

            // Click Logic
            btnToggle.setOnClickListener {
                val newStatus = if (event.status == "ACTIVE") "STOPPED" else "ACTIVE"
                onStatusChange(event, newStatus)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageViewHolder {
        // FIX: Using the correct filename 'item_manage_event'
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_event, parent, false)
        return ManageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ManageViewHolder, position: Int) {
        holder.bind(events[position], onStatusChange)
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}