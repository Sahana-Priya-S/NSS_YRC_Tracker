package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminEventAdapter(private var eventList: List<Event>) : RecyclerView.Adapter<AdminEventAdapter.ViewHolder>() {

    fun updateEvents(newEvents: List<Event>) {
        this.eventList = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventList[position])
    }

    override fun getItemCount(): Int = eventList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.eventTitle)
        private val date: TextView = itemView.findViewById(R.id.eventDate)
        private val description: TextView = itemView.findViewById(R.id.eventDescription)

        fun bind(event: Event) {
            title.text = event.title
            date.text = event.date
            description.text = event.description
        }
    }
}