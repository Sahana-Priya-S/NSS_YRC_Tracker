package com.example.nss_yrctracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AchievementsAdapter
    private lateinit var noAchievementsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)

        recyclerView = view.findViewById(R.id.achievementsRecyclerView)
        noAchievementsTextView = view.findViewById(R.id.noAchievementsTextView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = AchievementsAdapter(emptyList())
        recyclerView.adapter = adapter

        loadAchievements()

        return view
    }

    private fun loadAchievements() {
        val userId = auth.currentUser?.uid ?: return

        // Assumed Firestore Structure:
        // /users/{userId}/earnedAchievements/{achievementId} -> document with Achievement data

        firestore.collection("users").document(userId).collection("earnedAchievements")
            .get()
            .addOnSuccessListener { snapshot ->
                val achievements = snapshot.toObjects(Achievement::class.java)
                if (achievements.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    noAchievementsTextView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    noAchievementsTextView.visibility = View.GONE
                    adapter.updateAchievements(achievements)
                }
            }
            .addOnFailureListener {
                // Handle error
                noAchievementsTextView.text = "Error loading achievements."
                recyclerView.visibility = View.GONE
                noAchievementsTextView.visibility = View.VISIBLE
            }
    }
}