package com.example.nss_yrctracker

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var locationTextView: TextView
    private lateinit var timeWindowTextView: TextView
    private var eventLatitude: Double = 0.0
    private var eventLongitude: Double = 0.0
    private var startTime: Long = 0
    private var endTime: Long = 0

    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    eventLatitude = place.latLng?.latitude ?: 0.0
                    eventLongitude = place.latLng?.longitude ?: 0.0
                    locationTextView.text = getString(R.string.location_selected, place.name)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        val titleEditText = findViewById<EditText>(R.id.eventTitleEditText)
        val dateEditText = findViewById<EditText>(R.id.eventDateEditText)
        val descriptionEditText = findViewById<EditText>(R.id.eventDescriptionEditText)
        val selectLocationButton = findViewById<Button>(R.id.selectLocationButton)
        locationTextView = findViewById(R.id.locationTextView)
        val startTimeButton = findViewById<Button>(R.id.startTimeButton)
        val endTimeButton = findViewById<Button>(R.id.endTimeButton)
        timeWindowTextView = findViewById(R.id.timeWindowTextView)
        val saveButton = findViewById<Button>(R.id.saveEventButton)

        selectLocationButton.setOnClickListener { launchPlacePicker() }
        startTimeButton.setOnClickListener { showTimePicker(true) }
        endTimeButton.setOnClickListener { showTimePicker(false) }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val date = dateEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (title.isNotEmpty() && date.isNotEmpty() && description.isNotEmpty() && eventLatitude != 0.0 && startTime > 0 && endTime > 0) {
                if (endTime <= startTime) {
                    Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                saveEvent(title, date, description, eventLatitude, eventLongitude, startTime, endTime)
            } else {
                Toast.makeText(this, "Please fill all fields, select a location, and set a time window.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            if (isStartTime) {
                startTime = selectedTime
            } else {
                endTime = selectedTime
            }
            updateTimeWindowText()
        }, hour, minute, false).show()
    }

    private fun updateTimeWindowText() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startText = if (startTime > 0) timeFormat.format(Date(startTime)) else "..."
        val endText = if (endTime > 0) timeFormat.format(Date(endTime)) else "..."
        timeWindowTextView.text = "Window: $startText - $endText"
    }

    private fun launchPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
        startAutocomplete.launch(intent)
    }

    private fun saveEvent(title: String, date: String, description: String, lat: Double, lon: Double, start: Long, end: Long) {
        val eventsCollection = firestore.collection("events")
        val eventDoc = eventsCollection.document()

        val newEvent = Event(
            id = eventDoc.id,
            title = title,
            date = date,
            description = description,
            latitude = lat,
            longitude = lon,
            status = "active",
            attendanceStartTime = start,
            attendanceEndTime = end
        )

        eventDoc.set(newEvent)
            .addOnSuccessListener {
                Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add event.", Toast.LENGTH_SHORT).show()
            }
    }
}