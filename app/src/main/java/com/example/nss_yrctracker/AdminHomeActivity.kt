package com.example.nss_yrctracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminHomeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        // Logout button
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Future: Add navigation for add/manage events & approvals
        val addEventButton = findViewById<Button>(R.id.addEventButton)
        val manageEventsButton = findViewById<Button>(R.id.manageEventsButton)
        val approvalsButton = findViewById<Button>(R.id.approvalsButton)

        val events = snapshot.toObjects(Event::class.java)
        eventAdapter.setEvents(events)

        addEventButton.setOnClickListener {
            // TODO: Open event creation screen
        }

        manageEventsButton.setOnClickListener {
            // TODO: Open manage events screen
        }

        approvalsButton.setOnClickListener {
            // TODO: Open approvals screen
        }
    }
}
