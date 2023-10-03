package com.coderGtm.yantra.receivers

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class AdminReceiver : DeviceAdminReceiver() {
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        LocalBroadcastManager.getInstance(context).sendBroadcast(
            Intent(ACTION_DISABLED)
        )
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        LocalBroadcastManager.getInstance(context).sendBroadcast(
            Intent(ACTION_ENABLED)
        )
    }

    companion object {
        const val ACTION_DISABLED = "device_admin_action_disabled"
        const val ACTION_ENABLED = "device_admin_action_enabled"
    }
}