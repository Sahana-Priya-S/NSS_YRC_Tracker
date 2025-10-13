package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MySubmissionsAdapter(private var submissions: List<Submission>) :
    RecyclerView.Adapter<MySubmissionsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_submission, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(submissions[position])
    }

    override fun getItemCount(): Int = submissions.size

    fun updateSubmissions(newSubmissions: List<Submission>) {
        this.submissions = newSubmissions
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitle: TextView = itemView.findViewById(R.id.eventTitleTextView)
        private val status: TextView = itemView.findViewById(R.id.statusTextView)
        private val comments: TextView = itemView.findViewById(R.id.commentsTextView)

        fun bind(submission: Submission) {
            eventTitle.text = submission.eventTitle
            status.text = "Status: ${submission.status}"

            if (submission.status == "Rejected" && submission.comments.isNotEmpty()) {
                comments.visibility = View.VISIBLE
                comments.text = "Admin Comments: ${submission.comments}"
            } else {
                comments.visibility = View.GONE
            }
        }
    }
}