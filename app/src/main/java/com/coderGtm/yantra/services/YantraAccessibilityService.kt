package com.coderGtm.yantra.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent

@SuppressLint("AccessibilityPolicy")
class YantraAccessibilityService : AccessibilityService() {

    companion object {
        private const val ACTION_REQUEST_LOCK_SCREEN = "com.coderGtm.yantra.action.REQUEST_LOCK_SCREEN"

        fun requestLockScreen(context: Context) {
            context.sendBroadcast(
                Intent(ACTION_REQUEST_LOCK_SCREEN).setPackage(context.packageName),
            )
        }
    }

    private val lockScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_REQUEST_LOCK_SCREEN) {
                performLockScreenAction()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter(ACTION_REQUEST_LOCK_SCREEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(lockScreenReceiver, filter, RECEIVER_NOT_EXPORTED)
        }
        else {
            registerReceiver(lockScreenReceiver, filter)
        }
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        // Ignore. Don't read any accessibility event.
    }

    private fun performLockScreenAction() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
            catch (_: Exception) {
                // Silently fail.
            }
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(lockScreenReceiver)
        }
        catch (_: Exception) {
        }
        super.onDestroy()
    }

    override fun onInterrupt() {}


}