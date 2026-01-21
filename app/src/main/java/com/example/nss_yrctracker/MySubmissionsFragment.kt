package com.example.nss_yrctracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MySubmissionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MySubmissionsAdapter
    private val submissionsList = mutableListOf<Submission>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_submissions, container, false)

        // 1. Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerMySubmissions)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 2. Load the current user's data
        loadUserSubmissions()

        return view
    }

    private fun loadUserSubmissions() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Query Firestore for submissions belonging to this user
        db.collection("submissions")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MySubmissions", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    submissionsList.clear()
                    for (doc in snapshot.documents) {
                        // Using .copy() to safely set the ID for 'val' properties
                        val submission = doc.toObject(Submission::class.java)?.copy(id = doc.id)
                        if (submission != null) {
                            submissionsList.add(submission)
                        }
                    }

                    // 3. Update the Adapter
                    adapter = MySubmissionsAdapter(submissionsList)
                    recyclerView.adapter = adapter
                }
            }
    }
}