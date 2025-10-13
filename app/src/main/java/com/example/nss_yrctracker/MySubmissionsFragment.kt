package com.example.nss_yrctracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MySubmissionsFragment : Fragment() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: MySubmissionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_submissions, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.submissionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MySubmissionsAdapter(emptyList())
        recyclerView.adapter = adapter
        loadMySubmissions()
        return view
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