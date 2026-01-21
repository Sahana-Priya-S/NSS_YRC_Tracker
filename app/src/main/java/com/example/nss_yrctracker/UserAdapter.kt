package com.example.nss_yrctracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val users: List<Triple<String, String, String>>, // Name, UID, Status
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // We use a built-in Android layout for simplicity
        val tvName: TextView = view.findViewById(android.R.id.text1)
        val tvStatus: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // Use 'simple_list_item_2' so we can show the name AND the status
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val (name, uid, status) = users[position]
        holder.tvName.text = name
        holder.tvStatus.text = status.uppercase()

        // Make "ONLINE" green and "OFFLINE" grey
        if (status.lowercase() == "online") {
            holder.tvStatus.setTextColor(Color.GREEN)
        } else {
            holder.tvStatus.setTextColor(Color.GRAY)
        }

        holder.itemView.setOnClickListener { onClick(uid) }
    }

    override fun getItemCount() = users.size
}