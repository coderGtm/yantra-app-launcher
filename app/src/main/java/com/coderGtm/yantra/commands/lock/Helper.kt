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
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.receivers.AdminReceiver
import com.coderGtm.yantra.services.YantraAccessibilityService
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle("Enable Locking Device")
            .setMessage("Please turn on Accessibility Service for Yantra Launcher for 'lock' command to work.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

fun lockDeviceByAdmin(activity: Activity) {
    val pm = activity.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
    if (pm.isScreenOn) {
        val policy = activity.getSystemService(AppCompatActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        try {
            policy.lockNow()
        } catch (ex: SecurityException) {
            MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle("Enable Locking Device")
                .setMessage("Please enable device administrator for Yantra Launcher for Lock command to work.\n\nNote that for Android 8 and below, using lock from Yantra Launcher may prevent Biometric Authentication and use only PIN or Password for the next time you unlock your Device, due to Android API limitations.")
                .setPositiveButton("Open Settings") { dialog, _ ->
                    val admin = ComponentName(activity.baseContext, AdminReceiver::class.java)
                    val intent: Intent = Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                    ).putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin
                    )
                    activity.startActivity(intent)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}