package com.example.nss_yrctracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log // Import Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private val TAG = "MainActivity" // Tag for logging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d(TAG, "onCreate started")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("NSS_YRC_Tracker_Prefs", Context.MODE_PRIVATE)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // **FIX:** Call loadCredentials correctly
        loadCredentials(emailEditText, passwordEditText)

        // SIGN UP LOGIC
        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting signup for: $email")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Signup successful in Authentication.")
                        val uid = auth.currentUser?.uid
                        if (uid == null) {
                            Log.e(TAG, "Error: UID is null after successful signup.")
                            Toast.makeText(this, "Signup failed: Could not get user ID.", Toast.LENGTH_LONG).show()
                            return@addOnCompleteListener
                        }

                        Log.d(TAG, "User UID: $uid")
                        val userMap = hashMapOf("uid" to uid, "email" to email, "role" to "Student")

                        Log.d(TAG, "Attempting to save user data to Firestore...")
                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                Log.d(TAG, "User data successfully saved to Firestore.")
                                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                                // **FIX:** Call redirectByRole correctly
                                redirectByRole("Student")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Firestore save failed during signup: ${e.message}", e)
                                Toast.makeText(this, "Signup OK, but failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                                // **FIX:** Call redirectByRole correctly
                                redirectByRole("Student")
                            }
                    } else {
                        Log.w(TAG, "Signup failed in Authentication: ${task.exception?.message}")
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // LOGIN LOGIC
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting login for: $email")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Login successful in Authentication.")
                        proceedToRoleCheck(email, password) // Check role before asking to save
                    } else {
                        Log.w(TAG, "Login failed in Authentication: ${task.exception?.message}")
                        // **FIX:** Use Toast.makeText correctly
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
        Log.d(TAG, "onCreate finished")
    }

    // --- MAKE SURE ALL THESE FUNCTIONS ARE PRESENT ---

    private fun proceedToRoleCheck(email: String, pass: String) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            Log.d(TAG, "Fetching user role for UID: $uid")
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val role = doc.getString("role") ?: "Student"
                        Log.d(TAG, "User role found: $role.")
                        // **FIX:** Call showSaveCredentialsDialog correctly
                        showSaveCredentialsDialog(email, pass, role)
                    } else {
                        Log.w(TAG, "User document missing in Firestore for UID: $uid. Defaulting to Student role.")
                        Toast.makeText(this, "Login successful, but user data missing. Please contact admin.", Toast.LENGTH_LONG).show()
                        // **FIX:** Call showSaveCredentialsDialog correctly
                        showSaveCredentialsDialog(email, pass, "Student") // Assume student if doc missing
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error reading user role from Firestore: ${e.message}", e)
                    Toast.makeText(this, "Error reading user role: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Log.e(TAG, "Login succeeded but UID is null immediately after.")
            Toast.makeText(this, "Login error: User ID not found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSaveCredentialsDialog(email: String, pass: String, role: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Credentials")
        builder.setMessage("Would you like to save your login information for next time?")

        builder.setPositiveButton("Save") { _, _ ->
            saveCredentials(email, pass)
            Log.d(TAG, "Credentials saved. Redirecting to role: $role")
            redirectByRole(role)
        }
        builder.setNegativeButton("No, Thanks") { _, _ ->
            clearCredentials()
            Log.d(TAG, "Credentials not saved. Redirecting to role: $role")
            redirectByRole(role)
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun saveCredentials(email: String, pass: String) {
        sharedPreferences.edit().apply {
            putString("EMAIL", email)
            putString("PASSWORD", pass)
            apply()
        }
        Log.d(TAG, "Credentials saved.")
    }

    private fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove("EMAIL")
            remove("PASSWORD")
            apply()
        }
        Log.d(TAG, "Saved credentials cleared.")
    }

    private fun loadCredentials(emailEt: EditText, passEt: EditText) {
        emailEt.setText(sharedPreferences.getString("EMAIL", ""))
        passEt.setText(sharedPreferences.getString("PASSWORD", ""))
        if (sharedPreferences.contains("EMAIL")) {
            Log.d(TAG, "Loaded saved credentials.")
        }
    }

    private fun redirectByRole(role: String) {
        val intent = when (role) {
            "Admin" -> Intent(this, AdminHomeActivity::class.java)
            else -> Intent(this, StudentHomeActivity::class.java)
        }
        try {
            Log.d(TAG, "Starting activity for role: $role")
            startActivity(intent)
            finish() // Close login activity
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start activity for role $role: ${e.message}", e)
            Toast.makeText(this, "Error starting dashboard. Please try again.", Toast.LENGTH_LONG).show()
        }
    }
}