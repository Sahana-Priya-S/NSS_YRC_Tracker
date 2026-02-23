package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ApprovalsAdapter(private val submissions: List<Submission>) :
    RecyclerView.Adapter<ApprovalsAdapter.ApprovalViewHolder>() {

    // FIXED: Inherit directly from RecyclerView.ViewHolder
    class ApprovalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEvent: TextView = view.findViewById(R.id.tvApprovalEvent)
        val tvSummary: TextView = view.findViewById(R.id.tvApprovalStudent)
        val imgProof: ImageView = view.findViewById(R.id.imgApprovalProof)
        val btnApprove: Button = view.findViewById(R.id.btnApprove)
        val btnReject: Button = view.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApprovalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_approval, parent, false)
        return ApprovalViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApprovalViewHolder, position: Int) {
        val sub = submissions[position]

        holder.tvEvent.text = sub.eventTitle
        // Matches the 'summary' field in your Submission data class
        holder.tvSummary.text = "Summary: ${sub.summary}"

        if (sub.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(sub.imageUrl)
                .into(holder.imgProof)
        }

        // Matches 'PENDING' all-caps casing from your database
        if (sub.status == "PENDING") {
            holder.btnApprove.visibility = View.VISIBLE
            holder.btnReject.visibility = View.VISIBLE
        } else {
            holder.btnApprove.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
        }
    }

    override fun getItemCount() = submissions.size
}