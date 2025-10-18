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

class EventDetailActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentEvent: Event? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var checkInButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val eventId = intent.getStringExtra("EVENT_ID")
        if (eventId == null) {
            finish()
            return
        }

        val titleTv = findViewById<TextView>(R.id.eventTitleTextView)
        val dateTv = findViewById<TextView>(R.id.eventDateTextView)
        val descriptionTv = findViewById<TextView>(R.id.eventDescriptionTextView)
        checkInButton = findViewById(R.id.checkInButton)

        firestore.collection("events").document(eventId).get()
            .addOnSuccessListener { document ->
                currentEvent = document.toObject(Event::class.java)
                currentEvent?.let {
                    titleTv.text = it.title
                    dateTv.text = it.date
                    descriptionTv.text = it.description
                }
            }

        checkInButton.setOnClickListener {
            checkPermissionsAndMarkAttendance()
        }
    }

    private fun checkPermissionsAndMarkAttendance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        } else {
            markAttendance()
        }
    }

    private fun markAttendance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(this, "Could not get your location. Please ensure GPS is enabled.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                currentEvent?.let { event ->
                    // **THIS IS THE NEW LOGIC**: Check the time window and attendance status
                    val currentTime = System.currentTimeMillis()
                    if (!event.attendanceActive) {
                        Toast.makeText(this, "Attendance has not been started by the admin yet.", Toast.LENGTH_LONG).show()
                        return@let
                    }
                    if (currentTime < event.attendanceStartTime || currentTime > event.attendanceEndTime) {
                        Toast.makeText(this, "The attendance window is currently closed.", Toast.LENGTH_LONG).show()
                        return@let
                    }

                    val eventLocation = Location("EventLocation").apply {
                        latitude = event.latitude
                        longitude = event.longitude
                    }

                    val distance = location.distanceTo(eventLocation)

                    if (distance <= 150) { // 150-meter radius
                        saveAttendanceToFirestore(event)
                    } else {
                        Toast.makeText(this, "You are too far from the event location to mark attendance.", Toast.LENGTH_LONG).show()
                    }
                }
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
            .document("${userId}_${event.id}")
            .set(attendanceRecord)
            .addOnSuccessListener {
                Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show()
                checkInButton.isEnabled = false
                checkInButton.text = "Attendance Marked"
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            markAttendance()
        } else {
            Toast.makeText(this, "Location permission is required to mark attendance.", Toast.LENGTH_LONG).show()
        }
    }
}