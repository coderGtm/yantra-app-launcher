package com.coderGtm.yantra.commands.list

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.contactsManager

fun listApps(command: Command) {
    command.output("Found ${command.terminal.appList.size} apps")
    command.output("-------------------------")
    val accessibilityApps = command.terminal.appList.filter { it.category == 8 }
    val audioApps = command.terminal.appList.filter { it.category == 1 }
    val gameApps = command.terminal.appList.filter { it.category == 0 }
    val imageApps = command.terminal.appList.filter { it.category == 3 }
    val mapApps = command.terminal.appList.filter { it.category == 6 }
    val newsApps = command.terminal.appList.filter { it.category == 5 }
    val productivityApps = command.terminal.appList.filter { it.category == 7 }
    val socialApps = command.terminal.appList.filter { it.category == 4 }
    val videoApps = command.terminal.appList.filter { it.category == 2 }
    val otherApps = command.terminal.appList.filter { it.category == -1 }

    if (otherApps.size == command.terminal.appList.size) {
        for (app in command.terminal.appList) {
            command.output("""- ${app.appName}""")
        }
        return
    }

    if (accessibilityApps.isNotEmpty()) {
        command.output("--> Accessibility", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in accessibilityApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (audioApps.isNotEmpty()) {
        command.output("--> Audio", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in audioApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (gameApps.isNotEmpty()) {
        command.output("--> Games", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in gameApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (imageApps.isNotEmpty()) {
        command.output("--> Image", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in imageApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (mapApps.isNotEmpty()) {
        command.output("--> Maps", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in mapApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (newsApps.isNotEmpty()) {
        command.output("--> News", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in newsApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (productivityApps.isNotEmpty()) {
        command.output("--> Productivity", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in productivityApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (socialApps.isNotEmpty()) {
        command.output("--> Social", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in socialApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (videoApps.isNotEmpty()) {
        command.output("--> Video", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in videoApps) {
            command.output("""- ${app.appName}""")
        }
    }
    if (otherApps.isNotEmpty()) {
        command.output("--> Undefined", command.terminal.theme.warningTextColor, Typeface.BOLD)
        for (app in otherApps) {
            command.output("""- ${app.appName}""")
        }
    }
    command.output("-------------------------")
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