package com.coderGtm.yantra.commands.scripts

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
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
                val scriptEditor = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                    .setTitle(scriptName)
                    .setMessage(terminal.activity.getString(R.string.scripts_disclaimer))
                    .setView(R.layout.dialog_multiline_input)
                    .setCancelable(false)
                    .setPositiveButton(terminal.activity.getString(R.string.save)) { dialog, _ ->
                        val scriptBody = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
                        terminal.preferenceObject.edit().putString("script_$scriptName", scriptBody).apply()
                        output(terminal.activity.getString(R.string.script_saved_successfully, scriptName),terminal.theme.successTextColor)
                    }
                    .setNegativeButton(terminal.activity.getString(R.string.delete)) { _, _ ->
                        terminal.preferenceObject.edit().remove("script_$scriptName").apply()
                        scripts.remove(scriptName)
                        terminal.preferenceObject.edit().putString("scripts",scripts.joinToString(";")).apply()
                        output(terminal.activity.getString(R.string.script_deleted, scriptName),terminal.theme.successTextColor)
                    }
                    .setNeutralButton(terminal.activity.getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                scriptEditor.findViewById<EditText>(R.id.bodyText)?.setText(terminal.preferenceObject.getString("script_$scriptName",""))
            }
        }
        else {
            scriptsMainDialog.setMessage(terminal.activity.getString(R.string.no_scripts_found))
        }
        scriptsMainDialog.setPositiveButton(terminal.activity.getString(R.string.add)) { _, _ ->
            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle(terminal.activity.getString(R.string.new_script))
                .setMessage(terminal.activity.getString(R.string.enter_script_name))
                .setView(R.layout.dialog_singleline_input)
                .setPositiveButton(terminal.activity.getString(R.string.create)) { dialog, _ ->
                    val name = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString().trim()
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
                        dialog.dismiss()
                    }
                }
                .setNeutralButton(terminal.activity.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
            .setNeutralButton(terminal.activity.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}