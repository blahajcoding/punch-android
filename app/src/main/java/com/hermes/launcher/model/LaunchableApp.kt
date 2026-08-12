package com.hermes.launcher.model

import android.graphics.drawable.Drawable

data class LaunchableApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable?,
)
