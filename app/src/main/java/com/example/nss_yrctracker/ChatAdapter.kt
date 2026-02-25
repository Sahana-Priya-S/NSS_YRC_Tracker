package com.example.nss_yrctracker

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<ChatMessage>, private val currentUserId: String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    // Using a standard Android simple list item ID to avoid 'unresolved reference'
    // if your custom item_message.xml is missing.
    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(android.R.id.text1)
        val container: LinearLayout = view as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        // Using a standard system layout that definitely exists to prevent crashes
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.tvMessage.text = msg.message

        // Step 6: Logic to distinguish two-way communication visually
        if (msg.senderId == currentUserId) {
            holder.tvMessage.gravity = Gravity.END
            holder.tvMessage.setTextColor(android.graphics.Color.BLUE)
        } else {
            holder.tvMessage.gravity = Gravity.START
            holder.tvMessage.setTextColor(android.graphics.Color.BLACK)
        }
    }

    override fun getItemCount() = messages.size
}