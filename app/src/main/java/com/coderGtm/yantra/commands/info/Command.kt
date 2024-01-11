package com.coderGtm.yantra.commands.info

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.provider.Settings
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "info",
        helpTitle = "infof [appName]",
        description = "Opens app settings page for specified app. Example: 'info Big Battery Display' or 'i Farty Orbit'"
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify an app name.", terminal.theme.errorTextColor)
            return
        }
        val name = command.removePrefix(args[0]).trim().lowercase()

        output("Locating '$name'...", terminal.theme.resultTextColor, Typeface.ITALIC)

        val candidates = mutableListOf<AppBlock>()
        for (app in terminal.appList) {
            if (app.appName.lowercase() == name) {
                output("+ Found ${app.packageName}")
                candidates.add(app)
            }
        }
        if (candidates.size == 1) {
            output("Opening settings for ${candidates[0].appName} (${candidates[0].packageName})", terminal.theme.successTextColor)
            launchAppInfo(this@Command, candidates[0])
        }
        else if (candidates.size > 1) {
            output("Multiple entries found for'$name'. Opening selection dialog.", terminal.theme.warningTextColor)
            val b1 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle("Multiple apps found")
                .setMessage("Multiple apps found with name '$name'. Please select one.")
                .setPositiveButton("OK") { _, _ ->
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
                        .setTitle("Select Package Name")
                        .setItems(items.toTypedArray()) { _, which ->
                            output("Opening settings for ${candidates[which].appName} (${candidates[which].packageName})", terminal.theme.successTextColor)
                            launchAppInfo(this@Command, candidates[which])
                        }
                    terminal.activity.runOnUiThread { b2.show() }
                }
            terminal.activity.runOnUiThread { b1.show() }
        }
        else {
            output("'$name' app not found. Try using 'list apps' to get list of all app names.", terminal.theme.warningTextColor)
        }
    }
}