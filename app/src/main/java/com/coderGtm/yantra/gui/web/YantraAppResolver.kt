package com.coderGtm.yantra.gui.web

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.coderGtm.yantra.gui.drawer.repository.AppRepository
import com.coderGtm.yantra.gui.desktop.model.AppInfo

/** Android-side resolver for app/package operations exposed to imported Yantra GUIs. */
class YantraAppResolver(
    private val context: Context
) {

    fun list(): List<AppInfo> =
        AppRepository(context).getInstalledApps()

    fun exists(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        return runCatching {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        }.getOrDefault(false)
    }

    fun launch(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return false

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    fun info(packageName: String): Boolean {
        if (!exists(packageName)) return false

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }

    fun uninstall(packageName: String): Boolean {
        if (!exists(packageName)) return false

        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }
}
