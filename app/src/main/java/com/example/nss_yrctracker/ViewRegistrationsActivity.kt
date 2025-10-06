package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath

class ViewRegistrationsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_registrations)

        val eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Unknown Event"
        findViewById<TextView>(R.id.eventNameTitle).text = "Registrations for $eventTitle"

        val recyclerView = findViewById<RecyclerView>(R.id.registeredStudentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadRegisteredStudentEmails(eventTitle, recyclerView)
    }

    private fun loadRegisteredStudentEmails(eventTitle: String, recyclerView: RecyclerView) {
        firestore.collection("registrations").whereEqualTo("eventTitle", eventTitle)
            .get()
            .addOnSuccessListener { registrationSnapshot ->
                val userIds = registrationSnapshot.documents.mapNotNull { it.getString("userId") }

                if (userIds.isNotEmpty()) {
                    // **FIX IS HERE**: Use FieldPath.documentId() to search by document ID
                    firestore.collection("users").whereIn(FieldPath.documentId(), userIds).get()
                        .addOnSuccessListener { userSnapshot ->
                            val emails = userSnapshot.documents.mapNotNull { it.getString("email") }
                            val adapter = RegisteredStudentAdapter(emails)
                            recyclerView.adapter = adapter
                        }
                } else {
                    val adapter = RegisteredStudentAdapter(listOf("No students registered yet."))
                    recyclerView.adapter = adapter
                }
            }
    }
}