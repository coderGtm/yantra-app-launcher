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
        helpTitle = terminal.activity.getString(R.string.cmd_uninstall_title),
        description = terminal.activity.getString(R.string.cmd_uninstall_help)
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
            output(terminal.activity.getString(R.string.requested_to_uninstall, candidates[0].appName))
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:"+candidates[0].packageName)
            terminal.activity.startActivity(intent)
        }
        else if (candidates.size > 1) {
            val b1 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle(terminal.activity.getString(R.string.multiple_apps_found))
                .setMessage(terminal.activity.getString(R.string.multiple_apps_found_with_name_please_select_one, name))
                .setPositiveButton(terminal.activity.getString(R.string.ok)) { _, _ ->
                    val items = mutableListOf<String>()
                    for (app in candidates) {
                        items.add(app.packageName)
                    }
                    val b2 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                        .setTitle(terminal.activity.getString(R.string.select_package_name))
                        .setItems(items.toTypedArray()) { _, which ->
                            output(terminal.activity.getString(R.string.requested_to_uninstall, candidates[which].appName))
                            val intent = Intent(Intent.ACTION_DELETE)
                            intent.data = Uri.parse("package:"+candidates[which].packageName)
                            terminal.activity.startActivity(intent)
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