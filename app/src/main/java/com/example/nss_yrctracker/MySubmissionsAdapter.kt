package com.example.nss_yrctracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MySubmissionsAdapter(private val submissions: List<Submission>) :
    RecyclerView.Adapter<MySubmissionsAdapter.SubmissionViewHolder>() {

    class SubmissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEvent: TextView = view.findViewById(R.id.tvSubEventTitle)
        val tvStatus: TextView = view.findViewById(R.id.tvSubStatus)
        val tvSummary: TextView = view.findViewById(R.id.tvSubSummary)
        val imgProof: ImageView = view.findViewById(R.id.imgSubProof)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_submission, parent, false)
        return SubmissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        val submission = submissions[position]

        // These match the master Submission.kt properties
        holder.tvEvent.text = submission.eventTitle
        holder.tvSummary.text = submission.summary
        holder.tvStatus.text = submission.status

        when (submission.status) {
            "APPROVED" -> holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
            "REJECTED" -> holder.tvStatus.setTextColor(Color.parseColor("#F44336"))
            else -> holder.tvStatus.setTextColor(Color.parseColor("#FF9800"))
        }

        Glide.with(holder.itemView.context)
            .load(submission.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.imgProof)
    }

    override fun getItemCount() = submissions.size
}