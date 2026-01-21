package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        val rvUserList = findViewById<RecyclerView>(R.id.rvUserList)
        rvUserList.layoutManager = LinearLayoutManager(this)

        // Listen for all users and show their online/offline status
        db.collection("users").addSnapshotListener { snapshot, _ ->
            val userList = mutableListOf<Triple<String, String, String>>() // Name, UID, Status
            if (snapshot != null) {
                for (doc in snapshot.documents) {
                    val uid = doc.id
                    if (uid == currentUserId) continue // Don't show yourself

                    val name = doc.getString("name") ?: "User"
                    val status = doc.getString("status") ?: "offline"
                    userList.add(Triple(name, uid, status))
                }

                rvUserList.adapter = UserAdapter(userList) { selectedUid ->
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("RECEIVER_ID", selectedUid)
                    startActivity(intent)
                }
            }
        }
    }
}