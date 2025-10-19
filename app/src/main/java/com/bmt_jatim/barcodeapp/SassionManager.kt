package com.bmt_jatim.barcodeapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)

    fun saveLogin(username: String) {
        prefs.edit().putString("USERNAME", username).apply()
        prefs.edit().putBoolean("IS_LOGGED_IN", true).apply()
    }

    fun getUsername(): String? = prefs.getString("USERNAME", null)

    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
