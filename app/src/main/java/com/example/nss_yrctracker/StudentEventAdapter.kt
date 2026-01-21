package com.example.nss_yrctracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentEventAdapter(
    private var events: List<Event>,
    private val onMarkClick: (Event) -> Unit,   // Triggers GPS verification
    private val onCardClick: (Event) -> Unit    // Opens Event Details
) : RecyclerView.Adapter<StudentEventAdapter.StudentViewHolder>() {

    // Set to store IDs of events the student has already marked
    private var attendedEventIds: Set<String> = emptySet()

    // Function to refresh the "greyed out" status from Fragment
    fun updateAttendedList(newIds: Set<String>) {
        attendedEventIds = newIds
        notifyDataSetChanged()
    }

    class StudentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvStatus: TextView = view.findViewById(R.id.tvEventStatus)
        val btnMark: Button = view.findViewById(R.id.btnMarkAttendance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_event, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val event = events[position]

        // Basic Data Binding
        holder.tvTitle.text = event.title
        holder.tvDate.text = event.date
        holder.tvStatus.text = event.status

        // 1. Handle Card Click
        holder.itemView.setOnClickListener { onCardClick(event) }

        // 2. Logic for Button State (Grey out if already marked)
        if (attendedEventIds.contains(event.id)) {
            // Already Marked
            holder.btnMark.visibility = View.VISIBLE
            holder.btnMark.isEnabled = false // Disable physical clicking
            holder.btnMark.text = "MARKED"
            holder.btnMark.setBackgroundColor(Color.GRAY) // Visual feedback

            holder.tvStatus.text = "COMPLETED"
            holder.tvStatus.setTextColor(Color.parseColor("#4B5563")) // Muted Text
        }
        else if (event.status.equals("ACTIVE", ignoreCase = true) || event.status.equals("LIVE", ignoreCase = true)) {
            // Event is available to mark
            holder.btnMark.visibility = View.VISIBLE
            holder.btnMark.isEnabled = true
            holder.btnMark.text = "MARK ATTENDANCE"
            holder.btnMark.setBackgroundColor(Color.parseColor("#065F46")) // NSS Green
            holder.btnMark.setOnClickListener { onMarkClick(event) }

            holder.tvStatus.setTextColor(Color.parseColor("#059669")) // Bright Green
        }
        else {
            // Event is Stopped or Hidden
            holder.btnMark.visibility = View.GONE
            holder.tvStatus.setTextColor(Color.RED)
        }
    }

    override fun getItemCount() = events.size
}