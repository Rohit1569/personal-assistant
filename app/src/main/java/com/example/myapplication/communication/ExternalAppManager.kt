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
                "ZOMATO" -> getZomatoIntent(query)
                "YOUTUBE" -> getYoutubeIntent(query)
                "SWIGGY" -> getSwiggyIntent(query)
                "RAPIDO" -> getRapidoIntent()
                "MAPS" -> getMapsIntent(query)
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
        // Search for specific place on Google Maps
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        return mapIntent
    }

    private fun getBrowserIntent(query: String): Intent {
        // Universal Search on Google/Chrome
        val url = "https://www.google.com/search?q=${Uri.encode(query)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (isAppInstalled("com.android.chrome")) {
            intent.setPackage("com.android.chrome")
        }
        return intent
    }

    private fun getAmazonIntent(query: String): Intent {
        val uri = Uri.parse("amazon://gp/aw/s/ref=mw_dp_a_s?k=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (isAppInstalled("com.amazon.mShop.android.shopping")) {
            intent.setPackage("com.amazon.mShop.android.shopping")
        } else {
            intent.data = Uri.parse("https://www.amazon.in/s?k=${Uri.encode(query)}")
        }
        return intent
    }

    private fun getZomatoIntent(query: String): Intent {
        val uri = Uri.parse("zomato://search?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (isAppInstalled("com.application.zomato")) {
            intent.setPackage("com.application.zomato")
        } else {
            intent.data = Uri.parse("https://www.zomato.com/search?q=${Uri.encode(query)}")
        }
        return intent
    }

    private fun getYoutubeIntent(query: String): Intent {
        val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.youtube")
        return intent
    }

    private fun getSwiggyIntent(query: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("swiggy://search?query=${Uri.encode(query)}"))
        intent.setPackage("in.swiggy.android")
        return intent
    }

    private fun getRapidoIntent(): Intent {
        val intent = context.packageManager.getLaunchIntentForPackage("com.rapido.passenger")
            ?: Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.rapido.passenger"))
        return intent
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
