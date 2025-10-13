package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ApprovalsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubmissionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approvals)

        recyclerView = findViewById(R.id.approvalsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SubmissionAdapter(
            emptyList(),
            onApprove = { submission ->
                // When approving, no comments are needed
                updateSubmissionStatus(submission, "Approved")
            },
            onReject = { submission ->
                // When rejecting, show the dialog to get comments
                showRejectCommentDialog(submission)
            }
        )
        recyclerView.adapter = adapter

        loadPendingSubmissions()
    }

    private fun loadPendingSubmissions() {
        firestore.collection("submissions")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val submissions = snapshot?.toObjects(Submission::class.java) ?: emptyList()
                adapter.updateSubmissions(submissions)
            }
    }

    private fun showRejectCommentDialog(submission: Submission) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Comments for Rejection")

        // Set up the input
        val input = EditText(this)
        input.hint = "e.g., Please upload a clearer image."
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Submit") { dialog, _ ->
            val comments = input.text.toString().trim()
            if (comments.isNotEmpty()) {
                updateSubmissionStatus(submission, "Rejected", comments)
            } else {
                Toast.makeText(this, "Please provide a reason for rejection.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun updateSubmissionStatus(submission: Submission, newStatus: String, comments: String? = null) {
        val submissionRef = firestore.collection("submissions").document(submission.id)

        val updates = mutableMapOf<String, Any>(
            "status" to newStatus
        )

        // Only add comments to the update if they exist
        if (comments != null) {
            updates["comments"] = comments
        }

        submissionRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Submission has been ${newStatus.toLowerCase()}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }
}