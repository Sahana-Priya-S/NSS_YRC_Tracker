package com.example.nss_yrctracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ManageEventAdapter(
    private var eventList: List<Event>,
    private val onDeleteClick: (Event) -> Unit
) : RecyclerView.Adapter<ManageEventAdapter.ViewHolder>() {

    fun updateEvents(newEvents: List<Event>) {
        this.eventList = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventList[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = eventList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.eventTitleTextView)
        private val editButton: Button = itemView.findViewById(R.id.editEventButton)
        private val viewRegButton: Button = itemView.findViewById(R.id.viewRegistrationsButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteEventButton)

        fun bind(event: Event) {
            titleTextView.text = event.title

            // Set up the "Edit" button to open the EditEventActivity
            editButton.setOnClickListener {
                val intent = Intent(itemView.context, EditEventActivity::class.java).apply {
                    // Pass the unique event ID so the correct document can be edited
                    putExtra("EVENT_ID", event.id)
                }
                itemView.context.startActivity(intent)
            }

            // Set up the "View Registrations" button to open the ViewRegistrationsActivity
            viewRegButton.setOnClickListener {
                val intent = Intent(itemView.context, ViewRegistrationsActivity::class.java).apply {
                    // Pass the title to query for registrations
                    putExtra("EVENT_TITLE", event.title)
                }
                itemView.context.startActivity(intent)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(event)
            }
        }
    }
}