package com.coderGtm.yantra.commands.open

import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "open",
        helpTitle = "open [app name]",
        description = "Opens specified app. Example: 'open Chrome'"
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
        candidates.removeAll {
            it.packageName == terminal.activity.packageName
        }

        if (candidates.size == 1) {
            terminal.activity.applicationContext.startActivity(terminal.activity.applicationContext.packageManager.getLaunchIntentForPackage(candidates[0].packageName))
            output("Opened ${candidates[0].appName}", terminal.theme.successTextColor)
            if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
                terminal.appList.remove(candidates[0])
                terminal.appList.add(0, candidates[0])
            }
        }
        else if (candidates.size > 1) {
            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle("Multiple apps found")
                .setMessage("Multiple apps found with name '$name'. Please select one.")
                .setPositiveButton("OK") { _, _ ->
                    val items = mutableListOf<String>()
                    for (app in candidates) {
                        items.add(app.packageName)
                    }
                    MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                        .setTitle("Select Package Name")
                        .setItems(items.toTypedArray()) { _, which ->
                            terminal.activity.applicationContext.startActivity(terminal.activity.applicationContext.packageManager.getLaunchIntentForPackage(candidates[which].packageName))
                            output("Opened ${candidates[which].appName}", terminal.theme.successTextColor)
                            if (terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.RECENT.value) {
                                terminal.appList.remove(candidates[which])
                                terminal.appList.add(0, candidates[which])
                            }
                        }
                        .show()
                }
                .show()
        }
        else {
            output("'$name' app not found. Try using 'list apps' to get list of all app names.", terminal.theme.warningTextColor)
        }
    }
}