package com.hermes.launcher.gateway

import android.content.Context
import android.util.Log

/**
 * Stores Pi gateway origin + Basic auth. Never log the password.
 */
class GatewayCredentialsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getOrigin(): String = prefs.getString(KEY_ORIGIN, "").orEmpty()

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "").orEmpty()

    fun getPassword(): String = prefs.getString(KEY_PASSWORD, "").orEmpty()

    fun getSessionId(): String = prefs.getString(KEY_SESSION, "").orEmpty()

    fun isConfigured(): Boolean = getOrigin().isNotBlank()

    fun save(origin: String, username: String, password: String) {
        prefs.edit()
            .putString(KEY_ORIGIN, origin)
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
        Log.d(TAG, "credentials saved originConfigured=true userConfigured=${username.isNotBlank()}")
    }

    fun saveSessionId(sessionId: String) {
        prefs.edit().putString(KEY_SESSION, sessionId).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
        Log.d(TAG, "credentials cleared")
    }

    companion object {
        private const val PREFS = "pincher_gateway"
        private const val KEY_ORIGIN = "origin"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SESSION = "session_id"
        private const val TAG = "PincherGatewayStore"
    }
}
