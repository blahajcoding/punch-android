package com.hermes.launcher.appearance

import android.content.Context

class AppearancePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun useSystemWallpaper(): Boolean = prefs.getBoolean(KEY_SYSTEM_WALLPAPER, true)

    fun setUseSystemWallpaper(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SYSTEM_WALLPAPER, enabled).apply()
    }

    companion object {
        private const val PREFS = "hermes_appearance"
        private const val KEY_SYSTEM_WALLPAPER = "use_system_wallpaper"
    }
}
