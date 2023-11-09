package com.coderGtm.yantra.commands.notify

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.coderGtm.yantra.UserNotificationChannelConfig

fun createNotificationChannel(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = UserNotificationChannelConfig.NAME.value
        val descriptionText = UserNotificationChannelConfig.DESCRIPTION.value
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(UserNotificationChannelConfig.ID.value, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}