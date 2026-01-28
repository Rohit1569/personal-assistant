package com.example.myapplication.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CabBookingManager @Inject constructor(private val context: Context) {

    fun bookCab(provider: String, destination: String): Result<Unit> {
        return try {
            val intent = if (provider.uppercase() == "OLA") {
                getOlaIntent(destination)
            } else {
                getUberIntent(destination)
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CabBooking", "Failed to launch $provider: ${e.message}")
            Result.failure(e)
        }
    }

    private fun getUberIntent(destination: String): Intent {
        // Uber Deep Link Protocol - using formatted_address for better recognition
        val uri = Uri.parse("uber://?action=setPickup&pickup=my_location&dropoff[formatted_address]=${Uri.encode(destination)}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.ubercab")
        }
    }

    private fun getOlaIntent(destination: String): Intent {
        // Ola Deep Link Protocol
        val uri = Uri.parse("ola://app/booking?dropoff_name=${Uri.encode(destination)}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.olacabs.customer")
        }
    }
}
