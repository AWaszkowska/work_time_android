package com.example.shift

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private var prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "user_session"
        const val KEY_USERNAME = "username"
    }

    fun createLoginSession(username: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    fun getUserDetails(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun logoutUser() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.contains(KEY_USERNAME)
    }
}
