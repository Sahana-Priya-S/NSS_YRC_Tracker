package com.example.nss_yrctracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class StudentHomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentEventAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var eventsList = mutableListOf<Event>()
    private var targetEvent: Event? = null

    // Permission Launcher for GPS
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            targetEvent?.let { verifyLocationAndMark(it) }
        } else {
            Toast.makeText(context, "Location required to verify attendance", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_home, container, false)

        // 1. Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerStudentEvents)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 2. Setup Adapter
        adapter = StudentEventAdapter(
            events = eventsList,
            onMarkClick = { event ->
                targetEvent = event
                checkPermissionAndVerify()
            },
            onCardClick = { event ->
                val intent = Intent(activity, EventDetailsActivity::class.java)
                intent.putExtra("EXTRA_TITLE", event.title)
                intent.putExtra("EXTRA_DATE", event.date)
                intent.putExtra("EXTRA_STATUS", event.status)
                intent.putExtra("EXTRA_DESC", event.description)
                intent.putExtra("EXTRA_LOC", event.location)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        // 3. Setup Submit Proof Button
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitProof)
        btnSubmit.setOnClickListener {
            val intent = Intent(activity, SubmitProofActivity::class.java)
            startActivity(intent)
        }

        // 4. Setup Logout Button
        val btnLogout = view.findViewById<ImageButton>(R.id.logoutButton)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // --- NEW CODE ADDED HERE ---
        // 5. Setup ID Card Button
        val btnViewID = view.findViewById<CardView>(R.id.cardViewID)
        btnViewID.setOnClickListener {
            val intent = Intent(activity, DigitalIDActivity::class.java)
            startActivity(intent)
        }
        // ---------------------------

        // 6. Setup View History Button
        val btnHistory = view.findViewById<TextView>(R.id.btnViewHistory)
        btnHistory.setOnClickListener {
            startActivity(Intent(activity, MySubmissionsActivity::class.java))
        }

        // 7. Load Data
        loadEventsSafely()

        return view
    }

    private fun loadEventsSafely() {
        db.collection("events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("StudentHome", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    eventsList.clear()
                    for (doc in snapshots) {
                        try {
                            val title = doc.getString("title") ?: "No Title"
                            val date = doc.getString("date") ?: ""
                            val status = doc.getString("status") ?: "STOPPED"
                            val desc = doc.getString("description") ?: "No description available."
                            val loc = doc.getString("location") ?: "TBD"
                            val lat = doc.getDouble("latitude") ?: 0.0
                            val lng = doc.getDouble("longitude") ?: 0.0

                            val event = Event(
                                id = doc.id,
                                title = title,
                                date = date,
                                description = desc,
                                location = loc,
                                status = status,
                                latitude = lat,
                                longitude = lng
                            )
                            eventsList.add(event)
                        } catch (err: Exception) {
                            Log.e("StudentHome", "Error converting event", err)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun checkPermissionAndVerify() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            targetEvent?.let { verifyLocationAndMark(it) }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun verifyLocationAndMark(event: Event) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        Toast.makeText(context, "Verifying location...", Toast.LENGTH_SHORT).show()

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val results = FloatArray(1)
                    Location.distanceBetween(location.latitude, location.longitude, event.latitude, event.longitude, results)
                    val distance = results[0]

                    if (distance <= 100) {
                        markAttendanceInDb(event)
                    } else {
                        Toast.makeText(context, "You are ${distance.toInt()}m away. Go to the venue!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Open Google Maps to refresh GPS.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun markAttendanceInDb(event: Event) {
        if (userId == null) return

        // Fetch the student's name first so we can save it with the attendance
        db.collection("users").document(userId).get().addOnSuccessListener { userDoc ->
            val studentName = userDoc.getString("name") ?: "NSS Volunteer"

            val data = hashMapOf(
                "eventId" to event.id,
                "studentId" to userId,
                "studentName" to studentName, // ADD THIS LINE
                "status" to "PRESENT",
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("attendance").add(data).addOnSuccessListener {
                Toast.makeText(context, "✅ Attendance Marked!", Toast.LENGTH_LONG).show()
            }
        }
    }
}