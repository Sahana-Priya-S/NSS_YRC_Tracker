package com.example.nss_yrctracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ArchivedEventAdapter(private var events: List<Event>) :
    RecyclerView.Adapter<ArchivedEventAdapter.ViewHolder>() {

    fun updateEvents(newEvents: List<Event>) {
        this.events = newEvents
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // FIXED: Using archiveTitle and archiveDate from your XML
        val title: TextView = itemView.findViewById(R.id.archiveTitle)
        val date: TextView = itemView.findViewById(R.id.archiveDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archive_glass, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        holder.title.text = event.title

        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        holder.date.text = if (event.timestamp > 0) sdf.format(Date(event.timestamp)) else event.date

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AttendanceReportActivity::class.java)
            intent.putExtra("eventId", event.id)
            intent.putExtra("eventTitle", event.title)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = events.size
}