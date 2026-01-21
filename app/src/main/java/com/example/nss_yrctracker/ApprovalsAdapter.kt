package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ApprovalsAdapter(private val submissions: MutableList<Submission>) :
    RecyclerView.Adapter<ApprovalsAdapter.ApprovalViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    // ViewHolder matches your XML IDs exactly now
    class ApprovalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEvent: TextView = view.findViewById(R.id.tvApprovalEvent)
        val tvStudent: TextView = view.findViewById(R.id.tvApprovalStudent) // FIXED ID
        val imgProof: ImageView = view.findViewById(R.id.imgApprovalProof) // FIXED ID
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalViewHolder {
        // FIXED: Using 'item_approval' instead of 'item_approval_row'
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_approval, parent, false)
        return ApprovalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApprovalViewHolder, position: Int) {
        val submission = submissions[position]

        holder.tvEvent.text = submission.eventTitle
        holder.tvStudent.text = "Student ID: ${submission.userId}"

        // Load the image proof using Glide
        Glide.with(holder.itemView.context)
            .load(submission.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.imgProof)

        // Handle Approve Click
        holder.btnApprove.setOnClickListener {
            updateStatus(submission, "APPROVED", position, holder.itemView.context)
        }

        // Handle Reject Click
        holder.btnReject.setOnClickListener {
            updateStatus(submission, "REJECTED", position, holder.itemView.context)
        }
    }

    private fun updateStatus(submission: Submission, newStatus: String, position: Int, context: android.content.Context) {
        db.collection("submissions").document(submission.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                // If approved, increment student hours
                if (newStatus == "APPROVED") {
                    db.collection("users").document(submission.userId)
                        .update("totalHours", FieldValue.increment(2))
                        .addOnSuccessListener {
                            Toast.makeText(context, "Hours Awarded!", Toast.LENGTH_SHORT).show()
                        }
                }

                // Remove item from list and update UI
                submissions.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, submissions.size)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount() = submissions.size
}