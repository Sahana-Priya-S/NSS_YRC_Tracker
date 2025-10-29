package com.example.nss_yrctracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchivedEventAdapter(
    private var events: List<Event>
) : RecyclerView.Adapter<ArchivedEventAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archived_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<Event>) {
        this.events = newEvents
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.eventTitleTextView)
        private val date: TextView = itemView.findViewById(R.id.eventDateTextView)
        private val status: TextView = itemView.findViewById(R.id.statusTextView)
        private val viewSubmissionsButton: Button = itemView.findViewById(R.id.viewSubmissionsButton)

        fun bind(event: Event) {
            title.text = event.title
            date.text = event.date
            status.text = "Status: ${event.status.replaceFirstChar { it.uppercase() }}"

            // **FIX IS HERE**: Add the click listener
            viewSubmissionsButton.setOnClickListener {
                val intent = Intent(itemView.context, EventParticipantsActivity::class.java).apply {
                    putExtra("EVENT_TITLE", event.title)
                }
                itemView.context.startActivity(intent)
            }
        }
    }
}