package com.example.nss_yrctracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class SubmitProofActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var imgPreview: ImageView
    private lateinit var placeholder: LinearLayout
    private lateinit var etDesc: EditText
    private lateinit var btnSubmit: Button

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val eventNames = mutableListOf<String>()
    private val eventIds = mutableListOf<String>()
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            imgPreview.setImageURI(selectedImageUri)
            placeholder.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_proof)

        spinner = findViewById(R.id.spinnerEvents)
        imgPreview = findViewById(R.id.imgPreview)
        placeholder = findViewById(R.id.layoutUploadPlaceholder)
        etDesc = findViewById(R.id.etDescription)
        btnSubmit = findViewById(R.id.btnFinalSubmit)
        val cardImage = findViewById<CardView>(R.id.cardImagePicker)
        val btnBack = findViewById<View>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        cardImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        btnSubmit.setOnClickListener { startSubmissionProcess() }

        loadActiveEvents()
    }

    private fun loadActiveEvents() {
        db.collection("events")
            .whereEqualTo("status", "ACTIVE")
            .get()
            .addOnSuccessListener { documents ->
                eventNames.clear()
                eventIds.clear()
                if (documents.isEmpty) {
                    eventNames.add("No Active Events")
                } else {
                    for (doc in documents) {
                        eventNames.add(doc.getString("title") ?: "Unknown")
                        eventIds.add(doc.id)
                    }
                }
                spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, eventNames)
            }
    }

    private fun startSubmissionProcess() {
        if (userId == null) return
        if (eventIds.isEmpty() || eventNames[0].contains("No Active")) {
            Toast.makeText(this, "No active event selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. DISABLE BUTTON & SHOW LOADING
        btnSubmit.isEnabled = false
        btnSubmit.text = "Checking..."

        val selectedIndex = spinner.selectedItemPosition
        val eventId = eventIds[selectedIndex]
        val eventName = eventNames[selectedIndex]

        // 2. DUPLICATE CHECK
        db.collection("submissions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(this, "⚠️ You already submitted for this event!", Toast.LENGTH_LONG).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Proof"
                } else {
                    // NO DUPLICATE -> UPLOAD IMAGE
                    uploadImageToStorage(eventId, eventName)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Check failed: ${it.message}", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Proof"
            }
    }

    private fun uploadImageToStorage(eventId: String, eventName: String) {
        btnSubmit.text = "Uploading Image..."
        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child("proofs/$fileName")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    // IMAGE UPLOADED -> GET DOWNLOAD LINK
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveToFirestore(eventId, eventName, downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Proof"
                }
        }
    }

    private fun saveToFirestore(eventId: String, eventName: String, imageUrl: String) {
        btnSubmit.text = "Saving..."
        val desc = etDesc.text.toString().trim()

        val submission = hashMapOf(
            "userId" to userId,
            "eventId" to eventId,
            "eventTitle" to eventName,
            "summary" to desc,
            "status" to "PENDING",
            "timestamp" to System.currentTimeMillis(),
            "imageUrl" to imageUrl // NOW A REAL CLOUD LINK
        )

        db.collection("submissions").add(submission)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Submitted Successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Proof"
            }
    }
}