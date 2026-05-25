package com.coderGtm.yantra.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent

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

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter(ACTION_REQUEST_LOCK_SCREEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(lockScreenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
        else {
            registerReceiver(lockScreenReceiver, filter)
        }
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        if (
            accessibilityEvent.source?.className == "android.widget.Button" &&
            accessibilityEvent.source?.text?.toString()?.uppercase() == "LOCKSCREEN"
        ) {
            performLockScreenAction()
        }
    }

    private fun performLockScreenAction() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
            catch (_: Exception) {
                // Intentionally ignored to preserve existing silent-failure behavior.
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