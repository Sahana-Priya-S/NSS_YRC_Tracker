package com.example.nss_yrctracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // This is a "safety" check. If the intent is null, we stop.
        if (intent == null || context == null) return

        // Check if this event has an error
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null && geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error code: ${geofencingEvent.errorCode}")
            return
        }

        // If we ever decide to use this feature later,
        // the code to handle "Entering" or "Exiting" a location goes here.
        // For now, we just log it so the app doesn't crash.
        Log.d("GeofenceReceiver", "Geofence event received (Placeholder)")
    }
}