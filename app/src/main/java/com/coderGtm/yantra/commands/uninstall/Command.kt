package com.coderGtm.yantra.commands.uninstall

import android.content.Intent
import android.net.Uri
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

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
            terminal.activity.runOnUiThread {
                YantraLauncherDialog(terminal.activity).showInfo(
                    title = terminal.activity.getString(R.string.multiple_apps_found),
                    message = terminal.activity.getString(R.string.multiple_apps_found_with_name_please_select_one, name),
                    positiveButton = terminal.activity.getString(R.string.ok),
                    positiveAction = {
                        val items = mutableListOf<String>()
                        for (app in candidates) {
                            items.add(app.packageName)
                        }
                        terminal.activity.runOnUiThread {
                            YantraLauncherDialog(terminal.activity).selectItem(
                                title = terminal.activity.getString(R.string.select_package_name),
                                items = items.toTypedArray(),
                                clickAction = { which ->
                                    output(terminal.activity.getString(R.string.requested_to_uninstall, candidates[which].appName))
                                    val intent = Intent(Intent.ACTION_DELETE)
                                    intent.data = Uri.parse("package:"+candidates[which].packageName)
                                    terminal.activity.startActivity(intent)
                                }
                            )
                        }
                    }
                )
            }
        }
        else {
            output(terminal.activity.getString(R.string.app_not_found, name), terminal.theme.warningTextColor)
        }
    }
}