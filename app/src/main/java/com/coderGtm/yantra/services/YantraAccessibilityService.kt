package com.coderGtm.yantra.services

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class YantraAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if ((accessibilityEvent.source?.className == "android.widget.Button") and (accessibilityEvent.source?.text?.toString()?.uppercase() == "LOCKSCREEN")) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            }
            catch (e: Exception) {
                //Toast.makeText(this, "Failed to Lock Screen. Please report it to the developer via the Feedback command", Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onInterrupt() {}


}