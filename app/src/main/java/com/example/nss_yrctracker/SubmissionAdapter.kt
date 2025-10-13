package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SubmissionAdapter(
    private var submissions: List<Submission>,
    private val onApprove: (Submission) -> Unit,
    private val onReject: (Submission) -> Unit
) : RecyclerView.Adapter<SubmissionAdapter.ViewHolder>() {

    fun updateSubmissions(newSubmissions: List<Submission>) {
        this.submissions = newSubmissions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_submission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(submissions[position])
    }

    override fun getItemCount(): Int = submissions.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitle: TextView = itemView.findViewById(R.id.eventTitleTextView)
        private val studentEmail: TextView = itemView.findViewById(R.id.studentEmailTextView)
        private val summary: TextView = itemView.findViewById(R.id.summaryTextView)
        private val imageView: ImageView = itemView.findViewById(R.id.proofImageView)
        private val approveButton: Button = itemView.findViewById(R.id.approveButton)
        private val rejectButton: Button = itemView.findViewById(R.id.rejectButton)

        fun bind(submission: Submission) {
            eventTitle.text = "Event: ${submission.eventTitle}"
            summary.text = submission.summary

            // We'll placeholder the email for now and fetch it in the activity
            studentEmail.text = "Submitted by: Loading..."

            // Use Glide to load the image from the URL
            Glide.with(itemView.context)
                .load(submission.imageUrl)
                .into(imageView)

            approveButton.setOnClickListener { onApprove(submission) }
            rejectButton.setOnClickListener { onReject(submission) }
        }
    }
}