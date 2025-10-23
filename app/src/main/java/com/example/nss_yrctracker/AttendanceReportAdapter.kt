package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceReportAdapter(private var studentEmails: List<String>) :
    RecyclerView.Adapter<AttendanceReportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(studentEmails[position])
    }

    override fun getItemCount(): Int = studentEmails.size

    fun updateStudents(newEmails: List<String>) {
        this.studentEmails = newEmails
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val studentIdTextView: TextView = itemView.findViewById(R.id.studentIdTextView)

        fun bind(email: String) {
            studentIdTextView.text = email
        }
    }
}