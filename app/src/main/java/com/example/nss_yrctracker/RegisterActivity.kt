package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // FIXED: Using the IDs we defined in activity_register.xml
        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        tvBackToLogin.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user?.uid
                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to "STUDENT",
                        "totalHours" to 0,
                        "eventsAttended" to 0
                    )

                    if (userId != null) {
                        db.collection("users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}