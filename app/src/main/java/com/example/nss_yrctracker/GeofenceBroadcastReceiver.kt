package com.example.nss_yrctracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // **FIX IS HERE**: First, check if the event is null.
        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event is null, ignoring.")
            return
        }

        // Now that we know it's not null, we can safely check for errors.
        if (geofencingEvent.hasError()) {
            val errorMessage = "Geofence Error code: ${geofencingEvent.errorCode}"
            Log.e(TAG, errorMessage)
            return
        }

        // Get the transition type.
        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.i(TAG, "Geofence Entered")
                Toast.makeText(context, "Welcome! Attendance marked.", Toast.LENGTH_SHORT).show()

                // TODO: Add logic to save attendance to Firestore
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.i(TAG, "Geofence Exited")
                Toast.makeText(context, "Goodbye!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}