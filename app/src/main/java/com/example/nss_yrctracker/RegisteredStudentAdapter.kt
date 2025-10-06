package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RegisteredStudentAdapter(private val studentIds: List<String>) :
    RecyclerView.Adapter<RegisteredStudentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.studentIdTextView.text = studentIds[position]
    }

    override fun getItemCount() = studentIds.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val studentIdTextView: TextView = view.findViewById(R.id.studentIdTextView)
    }
}