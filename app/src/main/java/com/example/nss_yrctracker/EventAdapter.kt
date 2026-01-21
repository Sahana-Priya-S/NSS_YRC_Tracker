package com.example.nss_yrctracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventAdapter(
    private var eventList: List<Event>,
    private val isAdmin: Boolean = false,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    // --- FIX IS HERE: Removed the extra 'holder.' typo ---
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(eventList[position])
    }

    override fun getItemCount(): Int = eventList.size

    fun updateEvents(newEvents: List<Event>) {
        this.eventList = newEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.eventTitle)
        private val date: TextView = itemView.findViewById(R.id.eventDate)
        private val btnRegister: Button = itemView.findViewById(R.id.registerButton)

        fun bind(event: Event) {
            title.text = event.title
            date.text = event.date

            itemView.setOnClickListener { onItemClick(event) }

            if (isAdmin) {
                btnRegister.visibility = View.GONE
            } else {
                btnRegister.visibility = View.VISIBLE

                // Check if already registered to disable button
                checkRegistrationStatus(event.id)

                btnRegister.setOnClickListener {
                    registerForEvent(event)
                }
            }
        }

        private fun checkRegistrationStatus(eventId: String) {
            if (currentUserId == null) return

            db.collection("registrations")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        btnRegister.text = "Registered"
                        btnRegister.isEnabled = false
                        btnRegister.setBackgroundColor(Color.GRAY)
                    } else {
                        btnRegister.text = "Register Now"
                        btnRegister.isEnabled = true
                        btnRegister.setBackgroundColor(Color.parseColor("#065F46"))
                    }
                }
        }

        private fun registerForEvent(event: Event) {
            if (currentUserId == null) return

            val regData = hashMapOf(
                "eventId" to event.id,
                "userId" to currentUserId,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("registrations").add(regData)
                .addOnSuccessListener {
                    Toast.makeText(itemView.context, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                    btnRegister.text = "Registered"
                    btnRegister.isEnabled = false
                    btnRegister.setBackgroundColor(Color.GRAY)
                }
        }
    }
}