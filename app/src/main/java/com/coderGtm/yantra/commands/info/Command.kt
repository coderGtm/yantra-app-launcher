package com.coderGtm.yantra.commands.info

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "info",
        helpTitle = terminal.activity.getString(R.string.cmd_info_title),
        description = terminal.activity.getString(R.string.cmd_info_help)
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.specify_app_name), terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim().lowercase()

        output(terminal.activity.getString(R.string.locating_app, name), terminal.theme.resultTextColor, Typeface.ITALIC)

        val candidates = mutableListOf<AppBlock>()
        for (app in terminal.appList) {
            if (app.appName.lowercase() == name) {
                output(terminal.activity.getString(R.string.found_app, app.packageName))
                candidates.add(app)
            }
        }
        if (candidates.size == 1) {
            output(terminal.activity.getString(R.string.launching_settings_for, candidates[0].appName, candidates[0].packageName), terminal.theme.successTextColor)
            launchAppInfo(this@Command, candidates[0])
        }
        else if (candidates.size > 1) {
            output(terminal.activity.getString(R.string.multiple_entries_found_for_opening_selection_dialog, name), terminal.theme.warningTextColor)
            val b1 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle(terminal.activity.getString(R.string.multiple_apps_found))
                .setMessage(terminal.activity.getString(R.string.multiple_apps_found_with_name_please_select_one, name))
                .setPositiveButton(terminal.activity.getString(R.string.ok)) { _, _ ->
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
                    val b2 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                        .setTitle(terminal.activity.getString(R.string.select_package_name))
                        .setItems(items.toTypedArray()) { _, which ->
                            output(terminal.activity.getString(R.string.launching_settings_for, candidates[which].appName, candidates[which].packageName), terminal.theme.successTextColor)
                            launchAppInfo(this@Command, candidates[which])
                        }
                    terminal.activity.runOnUiThread { b2.show() }
                }
            terminal.activity.runOnUiThread { b1.show() }
        }
        else {
            output(terminal.activity.getString(R.string.app_not_found, name), terminal.theme.warningTextColor)
        }
    }
}