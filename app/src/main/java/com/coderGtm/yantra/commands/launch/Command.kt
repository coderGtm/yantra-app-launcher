package com.coderGtm.yantra.commands.launch

import android.graphics.Typeface
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.models.ShortcutBlock
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "launch",
        helpTitle = terminal.activity.getString(R.string.cmd_launch_title),
        description = terminal.activity.getString(R.string.cmd_launch_help)
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_an_app_to_launch), terminal.theme.errorTextColor)
            return
        }

        if (args[1].trim() == "-p") {
            val packageName = command.trim().removePrefix(args[0]).trim().removePrefix(args[1]).trim()
            if (packageName.isEmpty()) {
                output(terminal.activity.getString(R.string.specify_a_package_name), terminal.theme.errorTextColor)
                return
            }
            val app = terminal.appList.find {
                it.packageName == packageName
            }
            if (app != null) {
                output(terminal.activity.getString(R.string.launching_app, app.appName, app.packageName), terminal.theme.successTextColor)
                launchApp(this@Command, app)
                if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
                    terminal.appList.remove(app)
                    terminal.appList.add(0, app)
                }
            }
            else {
                output(terminal.activity.getString(R.string.app_not_found, packageName), terminal.theme.warningTextColor)
            }
            return
        }

        else if (args[1].trim() == "-s") {
            val shortcutLabel = command.trim().removePrefix(args[0]).trim().removePrefix(args[1]).trim().lowercase()
            if (shortcutLabel.isEmpty()) {
                output(terminal.activity.getString(R.string.specify_a_shortcut_label), terminal.theme.errorTextColor)
                return
            }
            val candidates = mutableListOf<ShortcutBlock>()
            terminal.shortcutList.forEach { 
                if (it.label.trim().lowercase() == shortcutLabel) {
                    candidates.add(it)
                }
            }
            var shortcut: ShortcutBlock?
            if (candidates.size == 1) {
                shortcut = candidates[0]
                output(terminal.activity.getString(R.string.launching_app, shortcut.label, shortcut.packageName), terminal.theme.successTextColor)
                launchShortcut(this@Command, shortcut)
                return
            }
            else if (candidates.size > 1) {
                output(terminal.activity.getString(R.string.multiple_entries_found_for_opening_selection_dialog, shortcutLabel), terminal.theme.warningTextColor)
                YantraLauncherDialog(terminal.activity).showInfo(
                    title = terminal.activity.getString(R.string.multiple_shortcuts_found),
                    message = terminal.activity.getString(R.string.multiple_shortcuts_found_with_label_please_select_one, shortcutLabel),
                    positiveButton = terminal.activity.getString(R.string.ok),
                    positiveAction = {
                        val items = mutableListOf<String>()
                        for (candidate in candidates) {
                            items.add(candidate.packageName)
                        }
                        YantraLauncherDialog(terminal.activity).selectItem(
                            title = terminal.activity.getString(R.string.select_package_name),
                            items = items.toTypedArray(),
                            clickAction = { which ->
                                shortcut = candidates[which]
                                output(terminal.activity.getString(R.string.launching_app, shortcut!!.label, shortcut!!.packageName), terminal.theme.successTextColor)
                                launchShortcut(this@Command, shortcut!!)
                            }
                        )
                    }
                )
                return
            }
            output(terminal.activity.getString(R.string.shortcut_not_found, shortcutLabel), terminal.theme.warningTextColor)
            return
        }

        val name = command.removePrefix(args[0]).trim().lowercase()

        val launchFirstMatch = terminal.preferenceObject.getBoolean("launchFirstMatch", false)
        if (launchFirstMatch) {
            val firstMatch = findFirstMatchingApp(name, terminal)
            if (firstMatch != null) {
                output(terminal.activity.getString(R.string.launching_app, firstMatch.appName, firstMatch.packageName), terminal.theme.successTextColor)
                launchApp(this@Command, firstMatch)
                if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
                    terminal.appList.remove(firstMatch)
                    terminal.appList.add(0, firstMatch)
                }
                return
            }
        }

        output(terminal.activity.getString(R.string.locating_app, name), terminal.theme.resultTextColor, Typeface.ITALIC)

        val candidates = mutableListOf<AppBlock>()
        for (app in terminal.appList) {
            if (app.appName.lowercase() == name) {
                output(terminal.activity.getString(R.string.found_app, app.packageName))
                candidates.add(app)
            }
        }
        if (candidates.removeAll {
                it.packageName == terminal.activity.packageName
            }) {
            output(terminal.activity.getString(R.string.excluding_app, terminal.activity.packageName), terminal.theme.warningTextColor)
        }

        if (candidates.size == 1) {
            output(terminal.activity.getString(R.string.launching_app, candidates[0].appName, candidates[0].packageName), terminal.theme.successTextColor)
            launchApp(this@Command, candidates[0])
            if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
                terminal.appList.remove(candidates[0])
                terminal.appList.add(0, candidates[0])
            }
        }
        else if (candidates.size > 1) {
            output(terminal.activity.getString(R.string.multiple_entries_found_for_opening_selection_dialog, name), terminal.theme.warningTextColor)
            YantraLauncherDialog(terminal.activity).showInfo(
                title = terminal.activity.getString(R.string.multiple_apps_found),
                message = terminal.activity.getString(R.string.multiple_apps_found_with_name_please_select_one, name),
                positiveButton = terminal.activity.getString(R.string.ok),
                positiveAction = {
                    val items = mutableListOf<String>()
                    for (app in candidates) {
                        items.add(app.packageName)
                    }
                    for (i in 0 until items.size) {
                        for (j in i + 1 until items.size) {
                            if (candidates[i].user != candidates[j].user) {
                                if (!isDefaultUser(candidates[i].user, this@Command)) {
                                    if (!items[i].endsWith(" (work)")) {
                                        items[i] = "${items[i]} (work)"
                                    }
                                }
                                else {
                                    if (!items[j].endsWith(" (work)")) {
                                        items[j] = "${items[j]} (work)"
                                    }
                                }
                            }
                        }
                    }
                    YantraLauncherDialog(terminal.activity).selectItem(
                        title = terminal.activity.getString(R.string.select_package_name),
                        items = items.toTypedArray(),
                        clickAction = { which ->
                            output(terminal.activity.getString(R.string.launching_app, candidates[which].appName, candidates[which].packageName), terminal.theme.successTextColor)
                            launchApp(this@Command, candidates[which])
                            if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
                                terminal.appList.remove(candidates[which])
                                terminal.appList.add(0, candidates[which])
                            }
                        }
                    )
                }
            )
        }
        else {
            output(terminal.activity.getString(R.string.app_not_found, name), terminal.theme.warningTextColor)
        }
    }
}