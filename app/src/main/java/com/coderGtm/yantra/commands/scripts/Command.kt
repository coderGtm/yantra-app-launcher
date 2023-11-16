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
        description = "Opens dialog for creating, modifying and deleting custom scripts for Yantra Launcher, to execute multiple commands at once. Also see the 'run' command."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'scripts' command does not take any parameters", terminal.theme.errorTextColor)
            return
        }
        // for user-defined scripts
        output("Opening Yantra Scripts...")
        val scripts = getScripts(terminal.preferenceObject)
        val scriptsMainDialog = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
            .setTitle("Yantra Scripts")
        if (scripts.isNotEmpty()) {
            scriptsMainDialog.setItems(scripts.toTypedArray()) { _, which ->
                val scriptName = scripts.elementAt(which)
                val scriptEditor = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                    .setTitle(scriptName)
                    .setMessage("View, Edit or Delete this script. Note: Enter 1 command per line, just as you normally enter in the Yantra terminal.")
                    .setView(R.layout.dialog_multiline_input)
                    .setCancelable(false)
                    .setPositiveButton("Save") { dialog, _ ->
                        val scriptBody = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
                        terminal.preferenceObject.edit().putString("script_$scriptName", scriptBody).apply()
                        output("Script $scriptName saved successfully!",terminal.theme.successTextColor)
                    }
                    .setNegativeButton("Delete") { _, _ ->
                        terminal.preferenceObject.edit().remove("script_$scriptName").apply()
                        scripts.remove(scriptName)
                        terminal.preferenceObject.edit().putString("scripts",scripts.joinToString(";")).apply()
                        output("Script '$scriptName' deleted.",terminal.theme.successTextColor)
                    }
                    .setNeutralButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                scriptEditor.findViewById<EditText>(R.id.bodyText)?.setText(terminal.preferenceObject.getString("script_$scriptName",""))
            }
        }
        else {
            scriptsMainDialog.setMessage("No Scripts found! Try creating one.")
        }
        scriptsMainDialog.setPositiveButton("Add") { _, _ ->
            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle("New Script")
                .setMessage("Enter Script name")
                .setView(R.layout.dialog_singleline_input)
                .setPositiveButton("Create") { dialog, _ ->
                    val name = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString().trim()
                    if (name.contains(";") || name == "") {
                        output("Script name cannot contain ';' or be empty.", terminal.theme.errorTextColor)
                    }
                    else if (!name[0].isLetter() || name.contains(' ')) {
                        output("Script name must start with a letter and cannot contain any spaces",terminal.theme.errorTextColor)
                    }
                    else if (scripts.contains(name)) {
                        output("This Name is already taken",terminal.theme.warningTextColor)
                    }
                    else {
                        scripts.add(name)
                        terminal.preferenceObject.edit().putString("scripts",scripts.joinToString(";")).apply()
                        output("Script '$name' created successfully! You can now edit it.",terminal.theme.successTextColor)
                        dialog.dismiss()
                    }
                }
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}