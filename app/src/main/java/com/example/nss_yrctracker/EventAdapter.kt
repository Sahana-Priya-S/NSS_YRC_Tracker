package com.example.nss_yrctracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private var eventList: List<Event>,
    private var registeredEventTitles: Set<String>,
    private val onEventClick: (Event) -> Unit, // Add this for item clicks
    private val onRegisterClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    fun updateEvents(newEvents: List<Event>, newRegistered: Set<String>) {
        this.eventList = newEvents
        this.registeredEventTitles = newRegistered
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        val isRegistered = registeredEventTitles.contains(event.title)
        holder.bind(event, isRegistered)
    }

    override fun getItemCount(): Int = eventList.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
        private val eventDate: TextView = itemView.findViewById(R.id.eventDate)
        private val eventDescription: TextView = itemView.findViewById(R.id.eventDescription)
        private val registerButton: Button = itemView.findViewById(R.id.registerButton)

        fun bind(event: Event, isRegistered: Boolean) {
            eventTitle.text = event.title
            eventDate.text = event.date
            eventDescription.text = event.description

            // **FIX #1**: Make the whole item clickable
            itemView.setOnClickListener {
                onEventClick(event)
            }

            if (isRegistered) {
                registerButton.text = "Registered"
                registerButton.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
                registerButton.isEnabled = false
            } else {
                registerButton.text = "Register"
                registerButton.setBackgroundColor(Color.parseColor("#6200EE")) // Default purple
                registerButton.isEnabled = true
                registerButton.setOnClickListener {
                    onRegisterClick(event)
                }
            }
        }
    }
}