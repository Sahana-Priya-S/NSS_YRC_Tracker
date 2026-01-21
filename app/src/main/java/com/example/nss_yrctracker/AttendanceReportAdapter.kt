package com.example.nss_yrctracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

// FIXED: Reference AttendanceReportAdapter.AttendanceViewHolder instead of AttendanceAdapter
class AttendanceReportAdapter(private val attendanceList: List<AttendanceRecord>) :
    RecyclerView.Adapter<AttendanceReportAdapter.AttendanceViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class AttendanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStudentName: TextView = view.findViewById(R.id.tvStudentName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_row, parent, false)
        return AttendanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val record = attendanceList[position]
        holder.tvStatus.text = record.status
        holder.tvStudentName.text = "Loading..."

        db.collection("users").document(record.studentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    holder.tvStudentName.text = document.getString("name") ?: "NSS Volunteer"
                } else {
                    holder.tvStudentName.text = "Unknown Student"
                }
            }
    }

    override fun getItemCount() = attendanceList.size
}