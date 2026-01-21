package com.example.nss_yrctracker

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide // Ensure you have the Glide dependency
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class StudentSubmissionActivity : AppCompatActivity() {

    // UI Components
    private lateinit var imgPreview: ImageView
    private lateinit var btnPick: Button
    private lateinit var etReport: EditText
    private lateinit var btnSubmit: Button

    // Logic Variables
    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Event Data
    private var eventId: String = ""
    private var eventTitle: String = ""

    // Resubmission State
    private var existingSubmissionId: String? = null
    private var oldProofUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_submission)

        // 1. Initialize Views
        imgPreview = findViewById(R.id.imgPreview)
        btnPick = findViewById(R.id.btnPickImage)
        etReport = findViewById(R.id.etStudentReport)
        btnSubmit = findViewById(R.id.btnFinalSubmit)

        // 2. Retrieve Event Details
        eventId = intent.getStringExtra("EVENT_ID") ?: ""
        eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Unknown Event"

        if (eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 3. Check Database for Existing Work
        checkExistingSubmission()

        // 4. Setup Click Listeners
        btnPick.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        btnSubmit.setOnClickListener { validateAndUpload() }
    }

    // --- CHECK SUBMISSION STATUS ---
    private fun checkExistingSubmission() {
        if (userId == null) return

        db.collection("submissions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val submission = documents.documents[0]
                    existingSubmissionId = submission.id

                    val status = submission.getString("status") ?: "Submitted"
                    val report = submission.getString("studentReport")
                    oldProofUrl = submission.getString("proofUrl")
                    val adminComment = submission.getString("adminComment") ?: ""

                    // Load existing image using Glide (Fast & Efficient)
                    if (!oldProofUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(oldProofUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(imgPreview)
                    }

                    if (status == "Rejected") {
                        // --- UNLOCK UI FOR EDITING ---
                        etReport.setText(report)
                        etReport.isEnabled = true
                        btnPick.visibility = View.VISIBLE

                        btnSubmit.text = "RESUBMIT (Previously Rejected)"
                        btnSubmit.isEnabled = true
                        btnSubmit.setBackgroundColor(Color.RED)

                        Toast.makeText(this, "REJECTED: $adminComment", Toast.LENGTH_LONG).show()
                        etReport.error = "Admin Reason: $adminComment"

                    } else {
                        // --- LOCK UI (Pending/Approved) ---
                        etReport.setText(report)
                        etReport.isEnabled = false
                        btnPick.visibility = View.GONE

                        btnSubmit.text = "Status: $status"
                        btnSubmit.isEnabled = false
                        btnSubmit.setBackgroundColor(Color.GRAY)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check status", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imgPreview.setImageURI(imageUri) // Show local preview immediately
        }
    }

    private fun validateAndUpload() {
        val reportText = etReport.text.toString().trim()

        if (reportText.isEmpty()) {
            Toast.makeText(this, "Please write a report.", Toast.LENGTH_SHORT).show()
            return
        }

        // LOGIC: Use New Image OR Old Image
        if (imageUri != null) {
            // Case 1: User selected a NEW image -> Upload it
            uploadImage(reportText)
        } else if (oldProofUrl != null) {
            // Case 2: User kept the OLD image -> Just update text
            saveToFirestore(oldProofUrl!!, reportText)
        } else {
            // Case 3: No image at all
            Toast.makeText(this, "Please select an image proof.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage(reportText: String) {
        btnSubmit.text = "Uploading..."
        btnSubmit.isEnabled = false

        val fileName = "${System.currentTimeMillis()}_proof.jpg"
        val ref = FirebaseStorage.getInstance().reference.child("proofs/$userId/$fileName")

        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveToFirestore(uri.toString(), reportText)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                resetButton()
            }
    }

    private fun saveToFirestore(imageUrl: String, reportText: String) {
        // Prepare the data
        val submissionData = hashMapOf(
            "userId" to userId,
            "eventId" to eventId,
            "eventTitle" to eventTitle,
            "proofUrl" to imageUrl,
            "studentReport" to reportText,
            "status" to "Pending",  // Always reset status to Pending on edit
            "adminComment" to "",    // Clear old rejection comments
            "timestamp" to System.currentTimeMillis()
        )

        // Decide: Update or Create?
        if (existingSubmissionId != null) {
            // UPDATE EXISTING
            db.collection("submissions").document(existingSubmissionId!!)
                .set(submissionData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Resubmitted Successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                    resetButton()
                }
        } else {
            // CREATE NEW
            db.collection("submissions").add(submissionData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Submitted Successfully!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Submission Failed", Toast.LENGTH_SHORT).show()
                    resetButton()
                }
        }
    }

    // --- HELPER TO RESET BUTTON ON ERROR ---
    private fun resetButton() {
        btnSubmit.isEnabled = true
        btnSubmit.text = if (existingSubmissionId != null) "RESUBMIT" else "SUBMIT EVERYTHING"
        if (existingSubmissionId != null) btnSubmit.setBackgroundColor(Color.RED)
    }
}