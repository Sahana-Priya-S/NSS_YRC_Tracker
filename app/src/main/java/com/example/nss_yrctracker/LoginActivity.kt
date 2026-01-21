package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // FIXED: Using IDs from activity_login.xml
        val etEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)

        // Inside LoginActivity.kt
        tvGoToSignUp.setOnClickListener {
            // Ensure the file is named RegisterActivity.kt or SignUpActivity.kt
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid
                    if (userId != null) {
                        checkUserRole(userId)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkUserRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: "STUDENT"
                    if (role == "ADMIN") {
                        startActivity(Intent(this, AdminHomeActivity::class.java))
                    } else {
                        startActivity(Intent(this, StudentHomeActivity::class.java))
                    }
                    finish()
                }
            }
    }
}