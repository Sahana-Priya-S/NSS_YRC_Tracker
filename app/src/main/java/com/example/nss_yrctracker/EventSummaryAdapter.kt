package com.example.nss_yrctracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventSummaryAdapter(private val eventList: List<Event>) :
    RecyclerView.Adapter<EventSummaryAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvStatus: TextView = view.findViewById(R.id.tvEventStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_summary, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]

        holder.tvTitle.text = event.title
        holder.tvDate.text = event.date
        holder.tvStatus.text = event.status

        // CLICK LOGIC: Open the Attendance Report for this specific event
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AttendanceReportActivity::class.java)
            intent.putExtra("eventId", event.id)
            intent.putExtra("eventTitle", event.title)
            intent.putExtra("eventDate", event.date)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = eventList.size
}