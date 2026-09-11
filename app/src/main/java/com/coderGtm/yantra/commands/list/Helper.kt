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

fun resolveAppCategory(packageName: String, command: Command): String {
    return try {
        val pm = command.terminal.activity.packageManager
        val info = pm.getApplicationInfo(packageName, 0)
        AppCategories.fromApplicationInfoCategory(info.category)
    } catch (_: Exception) {
        AppCategories.OTHER
    }
}

fun listApps(command: Command, filter: String? = null, showPackages: Boolean = false) {
    val normalizedFilter = filter?.let { normalizeCategoryFilter(it) }
    if (filter != null && normalizedFilter == null) {
        command.output(
            command.terminal.activity.getString(R.string.list_unknow_param, filter),
            command.terminal.theme.errorTextColor
        )
        command.output(
            "Available categories: ${AppCategories.ALL.joinToString(", ")}",
            command.terminal.theme.resultTextColor
        )
        return
    }

    val categorized = command.terminal.appList.distinct().map { app ->
        CategorizedApp(app.appName, app.packageName, resolveAppCategory(app.packageName, command))
    }
    var grouped = groupAppsByCategory(categorized)
    if (normalizedFilter != null) {
        grouped = grouped.filter { it.category == normalizedFilter }
    }

    val total = grouped.sumOf { it.apps.size }
    if (normalizedFilter != null) {
        command.output(
            command.terminal.activity.getString(R.string.found_apps, total) + " in $normalizedFilter"
        )
    } else {
        command.output(
            command.terminal.activity.getString(R.string.found_apps, total) + " in ${grouped.size} categories"
        )
    }

    for (group in grouped) {
        command.output(
            formatCategoryTitle(group),
            command.terminal.theme.warningTextColor,
            android.graphics.Typeface.BOLD
        )
        for (app in group.apps) {
            command.output(formatAppLine(app, showPackages))
        }
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