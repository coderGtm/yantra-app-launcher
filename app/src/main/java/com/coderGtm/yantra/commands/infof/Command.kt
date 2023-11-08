package com.coderGtm.yantra.commands.infof

import android.content.Intent
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
        helpTitle = "infof [approx app name]",
        description = "Opens app settings by matching given app name string using fuzzy search algorithm (Levenshtein distance). Example: 'openf tube' may open system settings for YouTube."
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        val name = command.removePrefix(args[0]).trim().lowercase()
        val candidates = mutableListOf<AppBlock>()
        //wait till appList has been initialized
        for (app in terminal.appList) {
            if (app.appName.lowercase() == name) {
                candidates.add(app)
            }
        }
        if (candidates.size == 1) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:"+candidates[0].packageName)
            terminal.activity.startActivity(intent)
            output("Opened settings for ${candidates[0].appName}", terminal.theme.successTextColor)
        }
        else if (candidates.size > 1) {
            val b1 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle("Multiple apps found")
                .setMessage("Multiple apps found with name '$name'. Please select one.")
                .setPositiveButton("OK") { _, _ ->
                    val items = mutableListOf<String>()
                    for (app in candidates) {
                        items.add(app.packageName)
                    }
                    val b2 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                        .setTitle("Select Package Name")
                        .setItems(items.toTypedArray()) { _, which ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:"+candidates[which].packageName)
                            terminal.activity.startActivity(intent)
                            output("Opened settings for ${candidates[which].appName}", terminal.theme.successTextColor)
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