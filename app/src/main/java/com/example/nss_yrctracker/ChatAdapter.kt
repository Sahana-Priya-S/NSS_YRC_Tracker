package com.example.nss_yrctracker

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: List<ChatMessage>, private val currentUserId: String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvChatMessageText)
        val tvTimestamp: TextView = view.findViewById(R.id.tvChatTimestamp)
        val bubble: LinearLayout = view.findViewById(R.id.chatBubble)
        val container: LinearLayout = view.findViewById(R.id.chatMessageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.tvMessage.text = msg.message

        // Format timestamp
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        holder.tvTimestamp.text = sdf.format(Date(msg.timestamp))

        // WhatsApp Style Alignment
        val params = holder.bubble.layoutParams as LinearLayout.LayoutParams
        if (msg.senderId == currentUserId) {
            holder.container.gravity = Gravity.END
            holder.bubble.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame) // Use your own bubble drawable if available
            holder.tvMessage.setTextColor(android.graphics.Color.BLACK)
        } else {
            holder.container.gravity = Gravity.START
            holder.bubble.setBackgroundResource(android.R.drawable.editbox_dropdown_dark_frame)
            holder.tvMessage.setTextColor(android.graphics.Color.WHITE)
        }
        holder.bubble.layoutParams = params
    }

    override fun getItemCount() = messages.size
}