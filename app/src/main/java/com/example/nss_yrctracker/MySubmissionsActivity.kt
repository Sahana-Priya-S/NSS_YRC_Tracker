package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MySubmissionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MySubmissionsAdapter
    private val list = mutableListOf<Submission>()
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_submissions)

        recyclerView = findViewById(R.id.recyclerMySubmissions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MySubmissionsAdapter(list)
        recyclerView.adapter = adapter

        loadMySubmissions()
    }

    private fun loadMySubmissions() {
        if (userId == null) return

        db.collection("submissions")
            .whereEqualTo("studentId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                list.clear()
                for (doc in documents) {
                    val sub = doc.toObject(Submission::class.java)
                    list.add(sub)
                }
                adapter.notifyDataSetChanged()

                if (list.isEmpty()) {
                    Toast.makeText(this, "No submissions found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
            }
    }
}