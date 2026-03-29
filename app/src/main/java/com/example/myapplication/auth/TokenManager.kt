package com.example.myapplication.auth

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun clearToken() {
        // Reset both token and setup flag on logout
        prefs.edit()
            .remove("jwt_token")
            .remove("setup_complete_v1")
            .apply()
    }

    fun isSetupComplete(): Boolean {
        return prefs.getBoolean("setup_complete_v1", false)
    }

    fun setSetupComplete() {
        prefs.edit().putBoolean("setup_complete_v1", true).apply()
    }
}
