package com.hermes.launcher.launcher

import android.content.Intent
import android.content.pm.PackageManager
import com.hermes.launcher.model.LaunchableApp

class InstalledAppsRepository(
    private val packageManager: PackageManager,
) {
    fun launchableApps(): List<LaunchableApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolved
            .map { info ->
                LaunchableApp(
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    label = info.loadLabel(packageManager).toString(),
                    icon = info.loadIcon(packageManager),
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}
