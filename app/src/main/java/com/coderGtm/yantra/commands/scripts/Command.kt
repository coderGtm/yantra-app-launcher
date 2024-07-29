package com.coderGtm.yantra.commands.scripts

import android.text.InputType
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "scripts",
        helpTitle = "scripts",
        description = terminal.activity.getString(R.string.cmd_scripts_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.command_takes_one_param, metadata.name), terminal.theme.errorTextColor)
            return
        }
        // for user-defined scripts
        output(terminal.activity.getString(R.string.opening_yantra_scripts))
        val scripts = getScripts(terminal.preferenceObject)
        val scriptsMainDialog = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
            .setTitle(terminal.activity.getString(R.string.yantra_scripts))
        if (scripts.isNotEmpty()) {
            scriptsMainDialog.setItems(scripts.toTypedArray()) { _, which ->
                val scriptName = scripts.elementAt(which)
                YantraLauncherDialog(terminal.activity).takeInput(
                    title = scriptName,
                    message = terminal.activity.getString(R.string.scripts_disclaimer),
                    initialInput = terminal.preferenceObject.getString("script_$scriptName","") ?: "",
                    cancellable = false,
                    inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                    positiveButton = terminal.activity.getString(R.string.save),
                    negativeButton = terminal.activity.getString(R.string.delete),
                    positiveAction = {
                        val scriptBody = it.trim()
                        terminal.preferenceObject.edit().putString("script_$scriptName", scriptBody).apply()
                        output(terminal.activity.getString(R.string.script_saved_successfully, scriptName),terminal.theme.successTextColor)
                    },
                    negativeAction = {
                        terminal.preferenceObject.edit().remove("script_$scriptName").apply()
                        scripts.remove(scriptName)
                        terminal.preferenceObject.edit().putString("scripts",scripts.joinToString(";")).apply()
                        output(terminal.activity.getString(R.string.script_deleted, scriptName),terminal.theme.successTextColor)
                    }
                )
            }
        }
        else {
            scriptsMainDialog.setMessage(terminal.activity.getString(R.string.no_scripts_found))
        }
        scriptsMainDialog.setPositiveButton(terminal.activity.getString(R.string.add)) { _, _ ->
            YantraLauncherDialog(terminal.activity).takeInput(
                title = terminal.activity.getString(R.string.new_script),
                message = terminal.activity.getString(R.string.enter_script_name),
                cancellable = false,
                positiveButton = terminal.activity.getString(R.string.create),
                negativeButton = terminal.activity.getString(R.string.cancel),
                positiveAction = {
                    val name = it.trim()
                    if (name.contains(";") || name == "") {
                        output(terminal.activity.getString(R.string.script_name_validation), terminal.theme.errorTextColor)
                    }
                    else if (!name[0].isLetter() || name.contains(' ')) {
                        output(terminal.activity.getString(R.string.script_name_another_validation),terminal.theme.errorTextColor)
                    }
                    else if (scripts.contains(name)) {
                        output(terminal.activity.getString(R.string.name_already_taken),terminal.theme.warningTextColor)
                    }
                    else {
                        scripts.add(name)
                        terminal.preferenceObject.edit().putString("scripts",scripts.joinToString(";")).apply()
                        output(terminal.activity.getString(R.string.script_created, name),terminal.theme.successTextColor)
                    }
                }
            )
        }
            .setNeutralButton(terminal.activity.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}