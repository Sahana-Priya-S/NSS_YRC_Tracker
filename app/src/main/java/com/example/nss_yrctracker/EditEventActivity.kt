package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore

class EditEventActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var eventId: String = ""
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0

    // UI Variables
    private lateinit var etTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnLocation: Button
    private lateinit var tvLocation: TextView
    private lateinit var btnSave: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        // ==============================================================================
        // THE FIX: INITIALIZE PLACES WITH YOUR KEY
        // ==============================================================================
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCx4oQApTqJRcdl3hYOrV3nYtYu3kj4-gQ")
        }

        eventId = intent.getStringExtra("EVENT_ID") ?: ""

        // Bind Views
        etTitle = findViewById(R.id.eventTitleEditText)
        etDate = findViewById(R.id.eventDateEditText)
        etDesc = findViewById(R.id.eventDescriptionEditText)
        btnLocation = findViewById(R.id.btnEditLocation)
        tvLocation = findViewById(R.id.tvEditLocation)
        btnSave = findViewById(R.id.saveChangesButton)
        btnStart = findViewById(R.id.startAttendanceButton)
        btnStop = findViewById(R.id.stopAttendanceButton)

        loadEventData()

        // 1. Location Picker Logic
        btnLocation.setOnClickListener {
            try {
                // Set the fields to specify which types of place data to return
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

                // Start the autocomplete intent
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this)
                startActivityForResult(intent, 105)
            } catch (e: Exception) {
                Toast.makeText(this, "Error starting location picker: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // 2. Save Logic
        btnSave.setOnClickListener {
            val updates = hashMapOf<String, Any>(
                "title" to etTitle.text.toString(),
                "date" to etDate.text.toString(),
                "description" to etDesc.text.toString(),
                "location" to tvLocation.text.toString(),
                "latitude" to selectedLat,
                "longitude" to selectedLng
            )
            db.collection("events").document(eventId).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                }
        }

        // 3. Start/Stop Attendance Logic
        btnStart.setOnClickListener { updateStatus(true) }
        btnStop.setOnClickListener { updateStatus(false) }
    }

    private fun loadEventData() {
        if (eventId.isEmpty()) return
        db.collection("events").document(eventId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val event = doc.toObject(Event::class.java)
                etTitle.setText(event?.title)
                etDate.setText(event?.date)
                etDesc.setText(event?.description)
                tvLocation.text = event?.location ?: "No location set"

                selectedLat = event?.latitude ?: 0.0
                selectedLng = event?.longitude ?: 0.0
            }
        }
    }

    private fun updateStatus(isActive: Boolean) {
        db.collection("events").document(eventId).update("attendanceActive", isActive)
            .addOnSuccessListener {
                val msg = if (isActive) "Attendance Started" else "Attendance Stopped"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    // Handle the result from the Location Picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 105) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    tvLocation.text = place.name ?: place.address
                    selectedLat = place.latLng?.latitude ?: 0.0
                    selectedLng = place.latLng?.longitude ?: 0.0
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_LONG).show()
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation
                }
            }
        }
    }
}