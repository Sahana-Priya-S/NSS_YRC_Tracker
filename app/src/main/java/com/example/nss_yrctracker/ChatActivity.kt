package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private var receiverId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        receiverId = intent.getStringExtra("RECEIVER_ID")
        val currentUserId = auth.currentUser?.uid ?: return

        val rvMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        val etMessage = findViewById<EditText>(R.id.etChatMessage)
        val btnSend = findViewById<Button>(R.id.btnChatSend)

        chatAdapter = ChatAdapter(messages, currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = chatAdapter

        // REAL-TIME LISTENER: This keeps the chat updated instantly
        db.collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(ChatMessage::class.java)
                        // Show only messages between these two specific users
                        if (msg != null && (
                                    (msg.senderId == currentUserId && msg.receiverId == receiverId) ||
                                            (msg.senderId == receiverId && msg.receiverId == currentUserId)
                                    )) {
                            messages.add(msg)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    rvMessages.scrollToPosition(messages.size - 1)
                }
            }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val msg = ChatMessage(currentUserId, receiverId!!, text, System.currentTimeMillis())
                db.collection("chats").add(msg) // Stored permanently in Firestore
                etMessage.text.clear()
            }
        }
    }
}