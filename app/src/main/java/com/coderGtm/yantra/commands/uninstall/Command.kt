package com.coderGtm.yantra.commands.uninstall

import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "uninstall",
        helpTitle = "uninstall [app name]",
        description = "Uninstalls the specified app. Example: 'u Instagram'"
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
            output("Requested to uninstall '${candidates[0].appName}'")
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:"+candidates[0].packageName)
            terminal.activity.startActivity(intent)
            terminal.uninstallCmdActive = true
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
                            output("Requested to uninstall '${candidates[which].appName}'")
                            val intent = Intent(Intent.ACTION_DELETE)
                            intent.data = Uri.parse("package:"+candidates[which].packageName)
                            terminal.activity.startActivity(intent)
                            terminal.uninstallCmdActive = true
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