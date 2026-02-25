package com.example.nss_yrctracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null || receiverId == null) {
            Toast.makeText(this, "Session error: User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val rvMessages = findViewById<RecyclerView>(R.id.rvChatMessages)
        val etMessage = findViewById<EditText>(R.id.etChatMessage)
        val btnSend = findViewById<Button>(R.id.btnChatSend)

        chatAdapter = ChatAdapter(messages, currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this)
        rvMessages.adapter = chatAdapter

        // Persistent Two-Way Listener
        db.collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ChatActivity", "Listen failed", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    messages.clear()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(ChatMessage::class.java)
                        if (msg != null && (
                                    (msg.senderId == currentUserId && msg.receiverId == receiverId) ||
                                            (msg.senderId == receiverId && msg.receiverId == currentUserId)
                                    )) {
                            messages.add(msg)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    if (messages.isNotEmpty()) {
                        rvMessages.scrollToPosition(messages.size - 1)
                    }
                }
            }

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty() && receiverId != null) {
                val msg = ChatMessage(currentUserId, receiverId!!, text, System.currentTimeMillis())

                // Safety: Database add with error handling
                db.collection("chats").add(msg)
                    .addOnSuccessListener {
                        etMessage.text.clear()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to send: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}