package com.example.nss_yrctracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source // Import Source

class EventDetailActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var eventId: String? = null // Store eventId
    private var currentEvent: Event? = null // Keep cached event for display

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var checkInButton: Button
    private lateinit var titleTv: TextView
    private lateinit var dateTv: TextView
    private lateinit var descriptionTv: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        eventId = intent.getStringExtra("EVENT_ID")
        if (eventId == null) {
            finish()
            return
        }

        titleTv = findViewById(R.id.eventTitleTextView)
        dateTv = findViewById(R.id.eventDateTextView)
        descriptionTv = findViewById(R.id.eventDescriptionTextView)
        checkInButton = findViewById(R.id.checkInButton)

        // Load initial event details for display
        loadEventDetails()

        checkInButton.setOnClickListener {
            // Check permissions first, then fetch fresh data and mark attendance
            checkPermissionsThenFetchAndMark()
        }
    }

    private fun loadEventDetails() {
        eventId?.let { id ->
            firestore.collection("events").document(id).get()
                .addOnSuccessListener { document ->
                    currentEvent = document.toObject(Event::class.java)
                    displayEventDetails()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load event details.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun displayEventDetails() {
        currentEvent?.let {
            titleTv.text = it.title
            dateTv.text = it.date
            descriptionTv.text = it.description
        }
    }


    private fun checkPermissionsThenFetchAndMark() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        } else {
            // **NEW LOGIC**: Fetch fresh data before marking attendance
            fetchLatestEventDataAndMarkAttendance()
        }
    }

    private fun fetchLatestEventDataAndMarkAttendance() {
        val currentEventId = eventId ?: return
        checkInButton.isEnabled = false // Disable button during check

        // **FETCH FRESH DATA FROM SERVER**
        firestore.collection("events").document(currentEventId).get(Source.SERVER)
            .addOnSuccessListener { document ->
                val freshEvent = document.toObject(Event::class.java)
                if (freshEvent != null) {
                    // Now perform checks with the fresh data
                    checkTimeAndLocation(freshEvent)
                } else {
                    Toast.makeText(this, "Event data not found.", Toast.LENGTH_SHORT).show()
                    checkInButton.isEnabled = true // Re-enable button
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get latest event status.", Toast.LENGTH_SHORT).show()
                checkInButton.isEnabled = true // Re-enable button
            }
    }


    private fun checkTimeAndLocation(event: Event) {
        // Check 1: Is attendance active?
        if (!event.attendanceActive) {
            Toast.makeText(this, "Attendance has not been started by the admin yet.", Toast.LENGTH_LONG).show()
            checkInButton.isEnabled = true // Re-enable button
            return
        }

        // Check 2: Is it within the time window?
        val currentTime = System.currentTimeMillis()
        if (currentTime < event.attendanceStartTime || currentTime > event.attendanceEndTime) {
            Toast.makeText(this, "The attendance window is currently closed.", Toast.LENGTH_LONG).show()
            checkInButton.isEnabled = true // Re-enable button
            return
        }

        // If time/status is okay, proceed to location check
        getLocationAndCheckDistance(event)
    }


    private fun getLocationAndCheckDistance(event: Event) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkInButton.isEnabled = true // Re-enable button if permission somehow lost
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(this, "Could not get your location. Ensure GPS is enabled.", Toast.LENGTH_LONG).show()
                    checkInButton.isEnabled = true // Re-enable button
                    return@addOnSuccessListener
                }

                val eventLocation = Location("EventLocation").apply {
                    latitude = event.latitude
                    longitude = event.longitude
                }
                val distance = location.distanceTo(eventLocation)

                if (distance <= 150) { // 150-meter radius
                    saveAttendanceToFirestore(event)
                } else {
                    Toast.makeText(this, "You are too far from the event location ($distance meters).", Toast.LENGTH_LONG).show()
                    checkInButton.isEnabled = true // Re-enable button
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location. Please try again.", Toast.LENGTH_SHORT).show()
                checkInButton.isEnabled = true // Re-enable button
            }
    }


    private fun saveAttendanceToFirestore(event: Event) {
        val userId = auth.currentUser?.uid ?: return

        val attendanceRecord = mapOf(
            "userId" to userId,
            "eventId" to event.id,
            "eventTitle" to event.title,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("attendance")
            .document("${userId}_${event.id}") // Unique ID per user per event
            .set(attendanceRecord)
            .addOnSuccessListener {
                Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show()
                checkInButton.text = "Attendance Marked"
                // Button remains disabled
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save attendance. Please try again.", Toast.LENGTH_SHORT).show()
                checkInButton.isEnabled = true // Re-enable button on failure
            }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now fetch data and check attendance
                fetchLatestEventDataAndMarkAttendance()
            } else {
                Toast.makeText(this, "Location permission is required to mark attendance.", Toast.LENGTH_LONG).show()
            }
        }
    }
}