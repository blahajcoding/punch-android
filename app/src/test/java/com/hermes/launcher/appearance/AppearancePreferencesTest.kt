package com.hermes.launcher.appearance

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppearancePreferencesTest {
    @Test
    fun systemWallpaperDefaultsOnAndPersists() {
        val prefs = AppearancePreferences(ApplicationProvider.getApplicationContext())
        assertTrue(prefs.useSystemWallpaper())
        prefs.setUseSystemWallpaper(false)
        assertFalse(prefs.useSystemWallpaper())
        prefs.setUseSystemWallpaper(true)
        assertTrue(prefs.useSystemWallpaper())
    }
}
