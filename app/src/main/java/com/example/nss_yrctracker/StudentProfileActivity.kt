package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class StudentProfileActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var emailTextView: TextView
    private lateinit var eventsAttendedTextView: TextView
    private lateinit var approvedSubmissionsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        emailTextView = findViewById(R.id.profileEmailTextView)
        eventsAttendedTextView = findViewById(R.id.eventsAttendedTextView)
        approvedSubmissionsTextView = findViewById(R.id.approvedSubmissionsTextView)

        loadProfileData()
    }

    private fun loadProfileData() {
        val user = auth.currentUser
        val userId = user?.uid

        if (user == null || userId == null) {
            Toast.makeText(this, "Could not load profile. User not logged in.", Toast.LENGTH_SHORT).show()
            finish() // Close the profile screen if user is not logged in
            return
        }

        // Display Email
        emailTextView.text = user.email ?: "No email available"

        // Fetch Attendance Count
        firestore.collection("attendance")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { attendanceSnapshot ->
                eventsAttendedTextView.text = "Events Attended: ${attendanceSnapshot.size()}"
            }
            .addOnFailureListener {
                eventsAttendedTextView.text = "Events Attended: Error"
            }

        // Fetch Approved Submissions Count
        firestore.collection("submissions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "Approved")
            .get()
            .addOnSuccessListener { submissionsSnapshot ->
                approvedSubmissionsTextView.text = "Approved Submissions: ${submissionsSnapshot.size()}"
            }
            .addOnFailureListener {
                approvedSubmissionsTextView.text = "Approved Submissions: Error"
            }
    }
}