package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminEventAdapter(
    private val events: List<Event>,
    private val onStartClick: (Event) -> Unit,
    private val onStopClick: (Event) -> Unit,
    private val onEditTimeClick: (Event) -> Unit,
    private val onCardClick: (Event) -> Unit,
    private val onArchiveClick: (Event) -> Unit // New Parameter for Permanent End
) : RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder>() {

    class AdminEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.eventTitle)
        val tvDate: TextView = view.findViewById(R.id.eventDate)
        val tvDesc: TextView = view.findViewById(R.id.eventDescription)
        val btnStart: MaterialButton = view.findViewById(R.id.startAttendanceButton)
        val btnStop: MaterialButton = view.findViewById(R.id.stopAttendanceButton)
        val btnEditTime: ImageButton = view.findViewById(R.id.btnEditTime)
        // Ensure this ID btnArchiveEvent exists in your item_admin_event.xml
        val btnArchive: MaterialButton = view.findViewById(R.id.btnArchiveEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminEventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_event, parent, false)
        return AdminEventViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminEventViewHolder, position: Int) {
        val event = events[position]

        holder.tvTitle.text = event.title
        holder.tvDesc.text = event.description

        // Formatting the date
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvDate.text = if (event.timestamp > 0) sdf.format(Date(event.timestamp)) else "No Date"

        // Handle UI states for Start/Stop buttons
        if (event.status == "ACTIVE") {
            holder.btnStart.isEnabled = false
            holder.btnStart.text = "Active"
            holder.btnStop.isEnabled = true
            holder.btnStop.alpha = 1.0f
        } else {
            holder.btnStart.isEnabled = true
            holder.btnStart.text = "Start"
            holder.btnStop.isEnabled = false
            holder.btnStop.alpha = 0.5f
        }

        // Set Click Listeners for standard actions
        holder.btnStart.setOnClickListener { onStartClick(event) }
        holder.btnStop.setOnClickListener { onStopClick(event) }
        holder.btnEditTime.setOnClickListener { onEditTimeClick(event) }

        // Listener for permanent archiving (End Permanently)
        holder.btnArchive.setOnClickListener {
            onArchiveClick(event)
        }

        // Clicking the whole card opens the Attendance Report
        holder.itemView.setOnClickListener {
            onCardClick(event)
        }
    }

    override fun getItemCount() = events.size
}