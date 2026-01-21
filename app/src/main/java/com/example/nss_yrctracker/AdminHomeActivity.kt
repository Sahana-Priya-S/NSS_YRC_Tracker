package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminHomeActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView

    // Firebase Instances
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        // 1. Initialize Header Views
        tvName = findViewById(R.id.tvAdminName)
        tvEmail = findViewById(R.id.tvAdminEmail)

        // 2. Load Admin Profile Data
        loadAdminProfile()

        // 3. Setup Logout Button Logic (FIXED)
        // We use 'logoutButton' because the ImageButton sits on top and catches the click
        val btnLogout = findViewById<View>(R.id.logoutButton)

        btnLogout.setOnClickListener {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut()

            // Navigate back to Login Screen & Clear Back Stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show()
        }

        // 4. Setup Bottom Navigation (FIXED)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            // Handle Navigation Item Clicks
            when (item.itemId) {
                R.id.nav_admin_events -> selectedFragment = AdminEventsFragment()
                R.id.nav_admin_manage -> selectedFragment = ManageFragment()
                R.id.nav_admin_approvals -> selectedFragment = ApprovalsFragment()
                R.id.nav_admin_archives -> selectedFragment = ArchivesFragment() // LOAD THE FRAGMENT HERE
            }

            // Switch the Fragment if one was selected
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit()
                return@setOnItemSelectedListener true
            }
            false
        }

        // 5. Load Default Fragment (Events) on Startup
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminEventsFragment())
                .commit()
        }
    }

    // --- HELPER FUNCTION: Load Admin Details ---
    private fun loadAdminProfile() {
        if (userId == null) return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Admin"
                    val email = document.getString("email") ?: "No Email"
                    val role = document.getString("role") ?: "Admin"

                    tvName.text = "Hello, $name"
                    tvEmail.text = "$email ($role)"
                } else {
                    tvName.text = "Hello, Admin"
                    tvEmail.text = "Profile not found"
                }
            }
            .addOnFailureListener { e ->
                tvName.text = "Welcome"
                tvEmail.text = "Error loading profile"
                Log.e("AdminHome", "Error loading profile", e)
            }
    }
}