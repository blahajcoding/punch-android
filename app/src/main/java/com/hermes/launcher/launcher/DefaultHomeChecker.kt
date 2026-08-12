package com.hermes.launcher.launcher

import android.content.Intent
import android.content.pm.PackageManager

class DefaultHomeChecker(
    private val packageManager: PackageManager,
    private val packageName: String,
) {
    fun isDefaultHome(): Boolean {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = packageManager.resolveActivity(
            homeIntent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return resolved?.activityInfo?.packageName == packageName
    }
}
