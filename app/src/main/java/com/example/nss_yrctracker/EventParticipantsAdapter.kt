package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventParticipantsAdapter(private var studentList: List<String>) :
    RecyclerView.Adapter<EventParticipantsAdapter.ParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.nameText.text = studentList[position]
    }

    override fun getItemCount(): Int = studentList.size

    // FIXED: Added this function to resolve Unresolved reference: updateStudents
    fun updateStudents(newList: List<String>) {
        this.studentList = newList
        notifyDataSetChanged()
    }

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(android.R.id.text1)
    }
}