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

    // Activity result launcher for picking an image
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

        selectImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        submitProofButton.setOnClickListener {
            uploadProof()
        }

        loadEventsIntoSpinner()
    }

    private fun loadEventsIntoSpinner() {
        firestore.collection("events").get()
            .addOnSuccessListener { snapshot ->
                eventsList = snapshot.toObjects(Event::class.java)
                val eventTitles = eventsList.map { it.title }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventTitles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                eventsSpinner.adapter = adapter
            }
    }

    private fun uploadProof() {
        val selectedEvent = eventsList[eventsSpinner.selectedItemPosition]
        val summary = summaryEditText.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (summary.isEmpty() || selectedImageUri == null || userId == null) {
            Toast.makeText(this, "Please select an event, write a summary, and choose an image.", Toast.LENGTH_LONG).show()
            return
        }

        // 1. Upload the image to Firebase Storage
        val imageRef = storage.reference.child("proofs/${UUID.randomUUID()}")
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                // 2. Get the download URL of the uploaded image
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // 3. Save the submission details to Firestore
                    saveSubmissionToFirestore(userId, selectedEvent, summary, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Failed to submit proof.", Toast.LENGTH_SHORT).show()
            }
    }
}