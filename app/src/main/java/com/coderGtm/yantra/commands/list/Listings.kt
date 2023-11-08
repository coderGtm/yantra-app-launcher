package com.coderGtm.yantra.commands.list

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.contactsManager
import com.coderGtm.yantra.terminal.Terminal

fun listApps(command: Command) {
    command.output("Found ${command.terminal.appList.size} apps")
    command.output("-------------------------")
    for (app in command.terminal.appList) {
        command.output("""- ${app.appName}""")
    }
}

fun listContacts(command: Command) {
    if (ContextCompat.checkSelfPermission(command.terminal.activity.baseContext,
            Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        command.output("Contacts permission missing!",command.terminal.theme.warningTextColor)
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
            command.output("Found $len Contacts",command.terminal.theme.commandColor)
        }.start()
    }
}

fun listThemes(command: Command) {
    command.output("Available themes:")
    command.output("-1: Custom")
    for ((i,theme) in Themes.entries.withIndex()) {
        command.output("$i: $theme")
    }
}