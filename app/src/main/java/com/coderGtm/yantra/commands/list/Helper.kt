package com.coderGtm.yantra.commands.list

import android.Manifest
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.contactsManager
import com.coderGtm.yantra.isPro

fun listApps(command: Command) {
    command.output(command.terminal.activity.getString(R.string.found_apps, command.terminal.appList.size))
    command.output("-------------------------")
    for (app in command.terminal.appList) {
        command.output("""- ${app.appName} (${app.packageName})""")
    }
}

fun listShortcuts(command: Command) {
    command.output(command.terminal.activity.getString(R.string.found_shortcuts, command.terminal.shortcutList.size))
    command.output("-------------------------")
    for (shortcut in command.terminal.shortcutList) {
        command.output("""- ${shortcut.label} (${shortcut.packageName})""")
    }
    if (command.terminal.shortcutList.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        val launcherApps = command.terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        if (!launcherApps.hasShortcutHostPermission()) {
            command.terminal.output(command.terminal.activity.getString(R.string.not_shortcut_host, command.terminal.activity.applicationInfo.loadLabel(command.terminal.activity.packageManager)), command.terminal.theme.warningTextColor, null)
        }
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
    if (isPro(command.terminal.activity)) {
        command.output("-1: Custom")
        val allThemes = mutableListOf<String>()
        Themes.entries.forEach { allThemes.add(it.name) }
        command.terminal.preferenceObject.getString("savedThemeList", "")?.split(",")?.filter { it.isNotEmpty() }?.forEach { allThemes.add(it) }

        for ((i,theme) in allThemes.withIndex()) {
            command.output("$i: $theme")
        }
    }
    else {
        command.output("0: Default")
    }
}