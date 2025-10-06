package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

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
                        // Get the UID right after user creation
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                        // **FIX IS HERE**: Create the map with the 'uid' field
                        val userMap = hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "role" to "Student"
                        )

                        // Save the user data to Firestore
                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
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
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        val role = doc.getString("role") ?: "Student"
                                        redirectByRole(role)
                                    } else {
                                        redirectByRole("Student")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error reading role: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun redirectByRole(role: String) {
        when (role) {
            "Admin" -> startActivity(Intent(this, AdminHomeActivity::class.java))
            else -> startActivity(Intent(this, StudentHomeActivity::class.java))
        }
        finish()
    }
}