package com.example.nss_yrctracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MySubmissionsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MySubmissionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We can create a simple layout for this in XML
        setContentView(R.layout.activity_my_submissions)

        recyclerView = findViewById(R.id.submissionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MySubmissionsAdapter(emptyList())
        recyclerView.adapter = adapter

        loadMySubmissions()
    }

    private fun loadMySubmissions() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("submissions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val mySubmissions = snapshot?.toObjects(Submission::class.java) ?: emptyList()
                adapter.updateSubmissions(mySubmissions)
            }
    }
}