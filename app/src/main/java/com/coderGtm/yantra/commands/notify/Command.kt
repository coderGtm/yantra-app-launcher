package com.coderGtm.yantra.commands.notify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.USER_NOTIFICATION_ID
import com.coderGtm.yantra.UserNotificationChannelConfig
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "notify",
        helpTitle = terminal.activity.getString(R.string.cmd_notify_title),
        description = terminal.activity.getString(R.string.cmd_notify_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.notify_specify_msg), terminal.theme.errorTextColor)
            return
        }
        val message = command.removePrefix(args[0]).trim()
        createNotificationChannel(terminal.activity)
        val builder = NotificationCompat.Builder(terminal.activity.baseContext, UserNotificationChannelConfig.ID.value)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Yantra Launcher")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        if (ActivityCompat.checkSelfPermission(terminal.activity.baseContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            output(terminal.activity.getString(R.string.feature_permission_missing, terminal.activity.getString(R.string.notifications)), terminal.theme.warningTextColor)
            ActivityCompat.requestPermissions(terminal.activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PermissionRequestCodes.NOTIFICATIONS.code)
            return
        }
        NotificationManagerCompat.from(terminal.activity.baseContext).notify(USER_NOTIFICATION_ID, builder.build())
        output(terminal.activity.getString(R.string.notification_fired),terminal.theme.successTextColor)
    }
}