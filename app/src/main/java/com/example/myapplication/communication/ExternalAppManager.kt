package com.example.myapplication.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalAppManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun launchAppWithSearch(app: String, query: String): Result<Unit> {
        return try {
            val intent = when (app.uppercase()) {
                "AMAZON" -> getAmazonIntent(query)
                "UBER" -> getUberIntent(query)
                "MAPS" -> getMapsIntent(query)
                "YOUTUBE" -> getYoutubeIntent(query)
                "BROWSER" -> getBrowserIntent(query)
                else -> null
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Result.success(Unit)
            } else {
                Result.failure(Exception("App protocol not defined for $app"))
            }
        } catch (e: Exception) {
            Log.e("ExternalApp", "Error launching $app: ${e.message}")
            Result.failure(e)
        }
    }

    private fun getMapsIntent(query: String): Intent {
        val uri = Uri.parse("google.navigation:q=${Uri.encode(query)}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
    }

    private fun getUberIntent(destination: String): Intent {
        val uri = Uri.parse("uber://?action=setPickup&pickup=my_location&dropoff[formatted_address]=${Uri.encode(destination)}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.ubercab")
        }
    }

    private fun getAmazonIntent(query: String): Intent {
        val uri = Uri.parse("amazon://gp/aw/s/ref=mw_dp_a_s?k=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (isAppInstalled("com.amazon.mShop.android.shopping")) {
            intent.setPackage("com.amazon.mShop.android.shopping")
        } else {
            intent.data = Uri.parse("https://www.amazon.com/s?k=${Uri.encode(query)}")
        }
        return intent
    }

    private fun getYoutubeIntent(query: String): Intent {
        val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.youtube")
        }
    }

    private fun getBrowserIntent(query: String): Intent {
        val url = "https://www.google.com/search?q=${Uri.encode(query)}"
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
