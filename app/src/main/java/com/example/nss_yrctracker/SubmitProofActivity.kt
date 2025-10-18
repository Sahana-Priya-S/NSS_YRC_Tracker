package com.example.nss_yrctracker

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SubmitProofActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var eventsSpinner: Spinner
    private lateinit var summaryEditText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var proofImageView: ImageView
    private lateinit var submitProofButton: Button

    private var selectedImageUri: Uri? = null
    // This will now hold only the events the student is eligible to submit for
    private var eligibleEventsList = mutableListOf<Event>()
    private var isSubmitting = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            proofImageView.setImageURI(it)
            proofImageView.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_proof)

        eventsSpinner = findViewById(R.id.eventsSpinner)
        summaryEditText = findViewById(R.id.summaryEditText)
        selectImageButton = findViewById(R.id.selectImageButton)
        proofImageView = findViewById(R.id.proofImageView)
        submitProofButton = findViewById(R.id.submitProofButton)

        selectImageButton.setOnClickListener { pickImage.launch("image/*") }
        submitProofButton.setOnClickListener { checkAndUploadProof() }

        // Load only the events the user can submit for
        loadEligibleEventsForSubmission()
    }

    private fun loadEligibleEventsForSubmission() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Get all events the user has attended
        firestore.collection("attendance")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { attendanceSnapshot ->
                if (attendanceSnapshot.isEmpty) {
                    Toast.makeText(this, "You have not attended any events yet.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val attendedEventIds = attendanceSnapshot.documents.mapNotNull { it.getString("eventId") }

                // 2. Get the details for those attended events
                firestore.collection("events")
                    .whereIn("id", attendedEventIds)
                    .get()
                    .addOnSuccessListener { eventsSnapshot ->
                        val today = Date()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                        // 3. Filter for events that are over
                        eligibleEventsList = eventsSnapshot.toObjects(Event::class.java).filter { event ->
                            try {
                                val eventDate = dateFormat.parse(event.date)
                                // The event is eligible if its date is before today
                                eventDate != null && eventDate.before(today)
                            } catch (e: Exception) {
                                false
                            }
                        }.toMutableList()

                        // 4. Populate the spinner with the filtered list
                        if (eligibleEventsList.isNotEmpty()) {
                            val eventTitles = eligibleEventsList.map { it.title }
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventTitles)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            eventsSpinner.adapter = adapter
                        } else {
                            Toast.makeText(this, "No past, attended events are available for submission.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
    }

    private fun checkAndUploadProof() {
        if (isSubmitting) {
            Toast.makeText(this, "Submission in progress...", Toast.LENGTH_SHORT).show()
            return
        }
        if (eligibleEventsList.isEmpty()) {
            Toast.makeText(this, "There are no eligible events to submit for.", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedEvent = eligibleEventsList[eventsSpinner.selectedItemPosition]
        val userId = auth.currentUser?.uid!!

        setSubmitting(true)

        // The logic to check for duplicate submissions is still valid and important
        firestore.collection("submissions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", selectedEvent.id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    uploadProof()
                } else {
                    Toast.makeText(this, "You have already submitted proof for this event.", Toast.LENGTH_LONG).show()
                    setSubmitting(false)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking for existing submissions.", Toast.LENGTH_SHORT).show()
                setSubmitting(false)
            }
    }

    // The rest of the functions (uploadProof, saveSubmissionToFirestore, setSubmitting) remain the same
    private fun uploadProof() {
        val selectedEvent = eligibleEventsList[eventsSpinner.selectedItemPosition]
        val summary = summaryEditText.text.toString().trim()
        val userId = auth.currentUser?.uid!!

        if (summary.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please write a summary and choose an image.", Toast.LENGTH_LONG).show()
            setSubmitting(false)
            return
        }

        val imageRef = storage.reference.child("proofs/${UUID.randomUUID()}")
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveSubmissionToFirestore(userId, selectedEvent, summary, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                setSubmitting(false)
            }
    }

    private fun saveSubmissionToFirestore(userId: String, event: Event, summary: String, imageUrl: String) {
        val submissionDoc = firestore.collection("submissions").document()
        val submission = Submission(
            id = submissionDoc.id,
            userId = userId,
            eventId = event.id,
            eventTitle = event.title,
            summary = summary,
            imageUrl = imageUrl,
            status = "Pending"
        )

        submissionDoc.set(submission)
            .addOnSuccessListener {
                Toast.makeText(this, "Proof submitted successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save submission.", Toast.LENGTH_SHORT).show()
                setSubmitting(false)
            }
    }

    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting
        submitProofButton.isEnabled = !submitting
        submitProofButton.text = if (submitting) "Submitting..." else "Submit"
    }
}