package com.example.nss_yrctracker

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ApprovalsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApprovalAdapter
    private val db = FirebaseFirestore.getInstance()
    private val submissionsList = mutableListOf<Submission>()

    private lateinit var btnPending: Button
    private lateinit var btnApproved: Button
    private lateinit var btnRejected: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approvals)

        recyclerView = findViewById(R.id.recyclerApprovals)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnPending = findViewById(R.id.tabPending)
        btnApproved = findViewById(R.id.tabApproved)
        btnRejected = findViewById(R.id.tabRejected)

        adapter = ApprovalAdapter(submissionsList)
        recyclerView.adapter = adapter

        btnPending.setOnClickListener { loadSubmissions("PENDING"); updateTabs(btnPending) }
        btnApproved.setOnClickListener { loadSubmissions("APPROVED"); updateTabs(btnApproved) }
        btnRejected.setOnClickListener { loadSubmissions("REJECTED"); updateTabs(btnRejected) }

        loadSubmissions("PENDING")
        updateTabs(btnPending)
    }

    private fun loadSubmissions(status: String) {
        db.collection("submissions")
            .whereEqualTo("status", status)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                submissionsList.clear()
                for (doc in documents) {
                    // Mapping fields manually to ensure 100% match with student side
                    val sub = Submission(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        eventTitle = doc.getString("eventTitle") ?: "",
                        summary = doc.getString("summary") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        status = doc.getString("status") ?: "PENDING"
                    )
                    submissionsList.add(sub)
                }
                adapter.notifyDataSetChanged()
            }
    }

    // --- INTERNAL ADAPTER ---
    inner class ApprovalAdapter(private val list: List<Submission>) :
        RecyclerView.Adapter<ApprovalAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvEvent: TextView = view.findViewById(R.id.tvApprovalEvent)
            val tvStudent: TextView = view.findViewById(R.id.tvApprovalStudent)
            val imgProof: ImageView = view.findViewById(R.id.imgApprovalProof) // Add this to XML!
            val btnApprove: Button = view.findViewById(R.id.btnApprove)
            val btnReject: Button = view.findViewById(R.id.btnReject)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_approval, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sub = list[position]

            holder.tvEvent.text = sub.eventTitle
            holder.tvStudent.text = "Student ID: ${sub.userId}"

            // LOAD IMAGE: Fixes the missing proof issue
            Glide.with(this@ApprovalsActivity)
                .load(sub.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgProof)

            if (sub.status == "PENDING") {
                holder.btnApprove.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
                holder.btnApprove.setOnClickListener { updateStatus(sub, "APPROVED") }
                holder.btnReject.setOnClickListener { updateStatus(sub, "REJECTED") }
            } else {
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
            }
        }

        override fun getItemCount() = list.size
    }

    private fun updateStatus(submission: Submission, newStatus: String) {
        db.collection("submissions").document(submission.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                // REWARD LOGIC: Award 2 hours if approved
                if (newStatus == "APPROVED") {
                    db.collection("users").document(submission.userId)
                        .update("totalHours", FieldValue.increment(2))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Hours Awarded to Student!", Toast.LENGTH_SHORT).show()
                        }
                }
                loadSubmissions("PENDING")
            }
    }

    private fun updateTabs(activeBtn: Button) {
        val inactiveColor = Color.parseColor("#F5F5F5")
        listOf(btnPending, btnApproved, btnRejected).forEach {
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