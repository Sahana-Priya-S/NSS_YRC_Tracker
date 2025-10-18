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
    private val onCompleteClick: (Event) -> Unit,
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
        private val editEventButton: Button = itemView.findViewById(R.id.editEventButton)
        private val viewRegistrationsButton: Button = itemView.findViewById(R.id.viewRegistrationsButton)
        private val deleteEventButton: Button = itemView.findViewById(R.id.deleteEventButton)
        private val completeEventButton: Button = itemView.findViewById(R.id.completeEventButton)

        fun bind(event: Event) {
            titleTextView.text = event.title

            // Navigation is handled here
            editEventButton.setOnClickListener {
                val intent = Intent(itemView.context, EditEventActivity::class.java).apply {
                    putExtra("EVENT_ID", event.id)
                }
                itemView.context.startActivity(intent)
            }

            viewRegistrationsButton.setOnClickListener {
                val intent = Intent(itemView.context, ViewRegistrationsActivity::class.java).apply {
                    putExtra("EVENT_TITLE", event.title)
                }
                itemView.context.startActivity(intent)
            }

            // Actions are passed up to the Activity
            completeEventButton.setOnClickListener { onCompleteClick(event) }
            deleteEventButton.setOnClickListener { onDeleteClick(event) }
        }
    }
}