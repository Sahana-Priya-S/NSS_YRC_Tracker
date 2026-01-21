package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DigitalIDActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvHours: TextView
    private lateinit var tvEvents: TextView

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_id)

        // Initialize Views
        tvName = findViewById(R.id.tvStudentName)
        tvHours = findViewById(R.id.tvTotalHours)
        tvEvents = findViewById(R.id.tvEventsAttended)

        loadStudentData()
    }

    private fun loadStudentData() {
        if (userId == null) return

        db.collection("users").document(userId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                tvName.text = snapshot.getString("name") ?: "NSS Volunteer"
                // Unit logic removed
                tvHours.text = (snapshot.getLong("totalHours") ?: 0).toString()
                tvEvents.text = (snapshot.getLong("eventsAttended") ?: 0).toString()
            }
        }
    }
}