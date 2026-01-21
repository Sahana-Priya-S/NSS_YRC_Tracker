package com.example.nss_yrctracker

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ApprovalsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApprovalsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val submissionsList = mutableListOf<Submission>()

    private lateinit var btnPending: Button
    private lateinit var btnApproved: Button
    private lateinit var btnRejected: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_approvals, container, false)

        // 1. Initialize UI
        recyclerView = view.findViewById(R.id.recyclerApprovals)
        recyclerView.layoutManager = LinearLayoutManager(context)

        btnPending = view.findViewById(R.id.tabPending)
        btnApproved = view.findViewById(R.id.tabApproved)
        btnRejected = view.findViewById(R.id.tabRejected)

        // 2. Setup Tab Listeners
// Update these to ALL CAPS to match your database
        btnPending.setOnClickListener { loadSubmissions("PENDING"); updateTabs(btnPending) }
        btnApproved.setOnClickListener { loadSubmissions("APPROVED"); updateTabs(btnApproved) }
        btnRejected.setOnClickListener { loadSubmissions("REJECTED"); updateTabs(btnRejected) }

// 3. Load Default State
        loadSubmissions("PENDING")
        updateTabs(btnPending)

        return view
    }

    private fun loadSubmissions(status: String) {
        db.collection("submissions")
            .whereEqualTo("status", status)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    submissionsList.clear()
                    for (doc in snapshots) {
                        val sub = Submission(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            eventTitle = doc.getString("eventTitle") ?: "",

                            // MATCHED: Database uses "summary"
                            summary = doc.getString("summary") ?: "",

                            // MATCHED: Database uses "imageUrl"
                            imageUrl = doc.getString("imageUrl") ?: "",

                            // MATCHED: Database uses "PENDING"
                            status = doc.getString("status") ?: "PENDING"
                        )
                        submissionsList.add(sub)
                    }
                    adapter = ApprovalsAdapter(submissionsList)
                    recyclerView.adapter = adapter
                }
            }
    }

    private fun updateTabs(activeBtn: Button) {
        val inactiveColor = Color.parseColor("#F5F5F5")
        val tabs = listOf(btnPending, btnApproved, btnRejected)

        tabs.forEach {
            it.backgroundTintList = ColorStateList.valueOf(inactiveColor)
            it.setTextColor(Color.DKGRAY)
        }

        activeBtn.setTextColor(Color.WHITE)
        when (activeBtn) {
            btnPending -> activeBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF9800"))
            btnApproved -> activeBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            btnRejected -> activeBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
        }
    }
}