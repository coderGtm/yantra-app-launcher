package com.coderGtm.yantra.commands.list

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.contactsManager

fun listApps(command: Command) {
    command.output(command.terminal.activity.getString(R.string.found_apps, command.terminal.appList.size))
    command.output("-------------------------")
    for (app in command.terminal.appList) {
        command.output("""- ${app.appName}""")
    }
}

fun listContacts(command: Command) {
    if (ContextCompat.checkSelfPermission(command.terminal.activity.baseContext,
            Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        command.output(command.terminal.activity.getString(R.string.feature_permission_missing, command.terminal.activity.getString(R.string.contacts)), command.terminal.theme.warningTextColor)
        ActivityCompat.requestPermissions(command.terminal.activity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            PermissionRequestCodes.CONTACTS.code)
    }
    else {
        Thread {
            val contacts = contactsManager(command.terminal)
            val len = contacts.count()
            for (item in contacts) {
                val name = item.name
                val number = item.number
                command.output(name)
                command.output(number, command.terminal.theme.commandColor)
                command.output("-------------")
            }
            command.output("-------------",command.terminal.theme.commandColor)
            command.output(command.terminal.activity.getString(R.string.found_contacts, len),command.terminal.theme.commandColor)
        }.start()
    }
}

fun listThemes(command: Command) {
    command.output(command.terminal.activity.getString(R.string.available_themes))
    command.output("-1: Custom")
    for ((i,theme) in Themes.entries.withIndex()) {
        command.output("$i: $theme")
    }
}