package com.example.nss_yrctracker

import android.app.DatePickerDialog
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
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity() {

    // UI Components
    private lateinit var etTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etDesc: EditText
    private lateinit var etAllottedHours: EditText

    private lateinit var btnLocation: Button
    private lateinit var tvLocationStatus: TextView

    private lateinit var btnStart: Button
    private lateinit var btnEnd: Button
    private lateinit var tvTimeStatus: TextView
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()

    // Data Variables
    private var selectedDateStr: String = ""
    private var startTimeStr: String = ""
    private var endTimeStr: String = ""
    private var selectedLocationName: String = ""
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0
    private var finalTimestamp: Long = 0

    // Google Places Launcher
    private val startAutocomplete = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                selectedLocationName = place.name ?: "Unknown"
                tvLocationStatus.text = "📍 $selectedLocationName"

                if (place.latLng != null) {
                    selectedLat = place.latLng!!.latitude
                    selectedLng = place.latLng!!.longitude
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)

        // Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        // Bind Views
        etTitle = findViewById(R.id.etEventTitle)
        etDate = findViewById(R.id.etEventDate)
        etDesc = findViewById(R.id.etEventDescription)
        etAllottedHours = findViewById(R.id.etAllottedHours)

        btnLocation = findViewById(R.id.btnSelectLocation)
        tvLocationStatus = findViewById(R.id.tvLocationStatus)

        btnStart = findViewById(R.id.btnStartTime)
        btnEnd = findViewById(R.id.btnEndTime)
        tvTimeStatus = findViewById(R.id.tvTimeStatus)

        btnSave = findViewById(R.id.btnSaveEvent)

        // Listeners
        etDate.isFocusable = false
        etDate.setOnClickListener { pickDate() }

        btnLocation.setOnClickListener {
            val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
            startAutocomplete.launch(intent)
        }

        btnStart.setOnClickListener { pickTime(isStart = true) }
        btnEnd.setOnClickListener { pickTime(isStart = false) }

        btnSave.setOnClickListener { saveEvent() }
    }

    private fun pickDate() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            selectedDateStr = "$day/${month + 1}/$year"
            etDate.setText(selectedDateStr)
            c.set(year, month, day)
            finalTimestamp = c.timeInMillis
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime(isStart: Boolean) {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            if (isStart) {
                startTimeStr = timeStr
                btnStart.text = "Start: $timeStr"
            } else {
                endTimeStr = timeStr
                btnEnd.text = "End: $timeStr"
            }
            tvTimeStatus.text = "Window: $startTimeStr - $endTimeStr"
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    private fun saveEvent() {
        val title = etTitle.text.toString().trim()
        val desc = etDesc.text.toString().trim()
        val hoursString = etAllottedHours.text.toString().trim()
        val allottedHours = hoursString.toIntOrNull() ?: 0

        // Validation
        if (title.isEmpty() || selectedDateStr.isEmpty() || selectedLocationName.isEmpty() || allottedHours <= 0) {
            Toast.makeText(this, "Please fill Title, Date, Location, and Allotted Hours", Toast.LENGTH_SHORT).show()
            return
        }

        val eventMap = hashMapOf(
            "title" to title,
            "description" to desc,
            "date" to selectedDateStr,
            "startTime" to startTimeStr,
            "endTime" to endTimeStr,
            "location" to selectedLocationName,
            "latitude" to selectedLat,
            "longitude" to selectedLng,
            "timestamp" to finalTimestamp,
            "allottedHours" to allottedHours,
            "status" to "STOPPED",
            "isArchived" to false
        )

        db.collection("events").add(eventMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Event Created with $allottedHours Credit Hours!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}