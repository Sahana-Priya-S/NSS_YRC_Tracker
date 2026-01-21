package com.example.nss_yrctracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // Re-using login layout

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inside MainActivity.kt onCreate
        val emailEditText = findViewById<EditText>(R.id.etLoginEmail)
        val passwordEditText = findViewById<EditText>(R.id.etLoginPassword)
        val signupButton = findViewById<TextView>(R.id.tvGoToSignUp)
        val loginButton = findViewById<Button>(R.id.btnLogin)

        // Add logic for these buttons as needed or just use LoginActivity as your main screen
    }
}