package com.coderGtm.yantra.commands.lock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.listeners.AdminReceiver
import com.coderGtm.yantra.services.YantraAccessibilityService

// function to check if Accessibility service is enabled (source: https://stackoverflow.com/a/56970606)
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val prefString = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return prefString != null && prefString.contains(context.packageName.toString() + "/" + YantraAccessibilityService::class.java.name)
}

fun lockDeviceByAccessibilityService(activity: Activity, binding: ActivityMainBinding) {
    if (isAccessibilityServiceEnabled(activity)) {
        binding.lockView.performClick()
    }
    else {
        YantraLauncherDialog(activity).showInfo(
            title = activity.getString(R.string.enable_locking_device),
            message = activity.getString(R.string.lock_by_accessibility_explainer),
            positiveButton = activity.getString(R.string.launch_settings),
            negativeButton = activity.getString(R.string.cancel),
            positiveAction = {
                activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )
    }
}

fun lockDeviceByAdmin(activity: Activity) {
    val pm = activity.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
    if (pm.isScreenOn) {
        val policy = activity.getSystemService(AppCompatActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        try {
            policy.lockNow()
        } catch (ex: SecurityException) {
            YantraLauncherDialog(activity).showInfo(
                title = activity.getString(R.string.enable_locking_device),
                message = activity.getString(R.string.lock_by_device_admin_explainer),
                positiveButton = activity.getString(R.string.launch_settings),
                negativeButton = activity.getString(R.string.cancel),
                positiveAction = {
                    val admin = ComponentName(activity.baseContext, AdminReceiver::class.java)
                    val intent: Intent = Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                    ).putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin
                    )
                    activity.startActivity(intent)
                }
            )
        }
    }
}