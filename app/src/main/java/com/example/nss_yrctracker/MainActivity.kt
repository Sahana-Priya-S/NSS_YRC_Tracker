package com.example.nss_yrctracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("NSS_YRC_Tracker_Prefs", Context.MODE_PRIVATE)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Load saved credentials if they exist
        loadCredentials(emailEditText, passwordEditText)

        // SIGN UP LOGIC
        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val userMap = hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "role" to "Student"
                        )
                        db.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                                // Redirect by role after successful signup
                                redirectByRole("Student")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
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

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Show a dialog to ask the user to save credentials
                        showSaveCredentialsDialog(email, password)
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun showSaveCredentialsDialog(email: String, pass: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Credentials")
        builder.setMessage("Would you like to save your login information for next time?")

        builder.setPositiveButton("Save") { _, _ ->
            saveCredentials(email, pass)
            proceedToNextScreen() // Continue after saving
        }

        builder.setNegativeButton("No, Thanks") { _, _ ->
            clearCredentials() // Ensure old credentials are cleared if user says no
            proceedToNextScreen() // Continue without saving
        }

        builder.setCancelable(false) // Prevent dialog from being dismissed by tapping outside
        builder.show()
    }

    private fun proceedToNextScreen() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role") ?: "Student"
                    redirectByRole(role)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error reading user role: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun saveCredentials(email: String, pass: String) {
        sharedPreferences.edit().apply {
            putString("EMAIL", email)
            putString("PASSWORD", pass)
            apply()
        }
    }

    private fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove("EMAIL")
            remove("PASSWORD")
            apply()
        }
    }

    private fun loadCredentials(emailEt: EditText, passEt: EditText) {
        // Automatically fill the fields if credentials were saved previously
        emailEt.setText(sharedPreferences.getString("EMAIL", ""))
        passEt.setText(sharedPreferences.getString("PASSWORD", ""))
    }

    private fun redirectByRole(role: String) {
        when (role) {
            "Admin" -> startActivity(Intent(this, AdminHomeActivity::class.java))
            else -> startActivity(Intent(this, StudentHomeActivity::class.java))
        }
        finish()
    }
}