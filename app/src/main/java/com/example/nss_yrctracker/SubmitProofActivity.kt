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
    private var eventsList = mutableListOf<Event>()
    private var isSubmitting = false // **FIX #1: Flag to prevent double submission**

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

        loadEventsIntoSpinner()
    }

    private fun loadEventsIntoSpinner() {
        firestore.collection("events").get().addOnSuccessListener { snapshot ->
            eventsList = snapshot.toObjects(Event::class.java)
            val eventTitles = eventsList.map { it.title }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventTitles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            eventsSpinner.adapter = adapter
        }
    }

    private fun checkAndUploadProof() {
        // **FIX #2: Prevent submission if one is already in progress**
        if (isSubmitting) {
            Toast.makeText(this, "Submission in progress...", Toast.LENGTH_SHORT).show()
            return
        }

        if (eventsList.isEmpty() || eventsSpinner.selectedItemPosition < 0) {
            Toast.makeText(this, "Please select an event.", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedEvent = eventsList[eventsSpinner.selectedItemPosition]
        val userId = auth.currentUser?.uid

        if (selectedEvent.id.isEmpty()) {
            Toast.makeText(this, "This event cannot be submitted for (missing ID).", Toast.LENGTH_LONG).show()
            return
        }

        if (userId == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        setSubmitting(true) // Disable button and set flag

        firestore.collection("submissions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", selectedEvent.id)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    uploadProof()
                } else {
                    Toast.makeText(this, "You have already submitted proof for this event.", Toast.LENGTH_LONG).show()
                    setSubmitting(false) // Re-enable button
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking for existing submissions.", Toast.LENGTH_SHORT).show()
                setSubmitting(false) // Re-enable button
            }
    }

    private fun uploadProof() {
        val selectedEvent = eventsList[eventsSpinner.selectedItemPosition]
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

    // **FIX #3: Helper function to manage the UI state**
    private fun setSubmitting(submitting: Boolean) {
        isSubmitting = submitting
        submitProofButton.isEnabled = !submitting
        submitProofButton.text = if (submitting) "Submitting..." else "Submit"
    }
}