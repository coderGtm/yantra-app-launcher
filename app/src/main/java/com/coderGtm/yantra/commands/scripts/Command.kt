package com.coderGtm.yantra.commands.scripts

import android.content.Intent
import android.text.InputType
import androidx.core.content.FileProvider
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import java.io.File
import androidx.core.content.edit

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
        YantraLauncherDialog(terminal.activity).selectItem(
            title = terminal.activity.getString(R.string.yantra_scripts),
            items = scripts.toTypedArray(),
            emptyMessage = terminal.activity.getString(R.string.no_scripts_found),
            clickAction = { which ->
                val scriptName = scripts.elementAt(which)
                YantraLauncherDialog(terminal.activity).selectItem(
                    title = "Select option for editing $scriptName",
                    items = arrayOf("Launcher's editor", "External editor"),
                    clickAction = { option ->
                        if (option == 0) {
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
                                    terminal.preferenceObject.edit {
                                        putString(
                                            "script_$scriptName",
                                            scriptBody
                                        )
                                    }
                                    output(terminal.activity.getString(R.string.script_saved_successfully, scriptName),terminal.theme.successTextColor)
                                },
                                negativeAction = {
                                    terminal.preferenceObject.edit { remove("script_$scriptName") }
                                    scripts.remove(scriptName)
                                    terminal.preferenceObject.edit {
                                        putString(
                                            "scripts",
                                            scripts.joinToString(";")
                                        )
                                    }
                                    output(terminal.activity.getString(R.string.script_deleted, scriptName),terminal.theme.successTextColor)
                                }
                            )
                        } else {
                            val textToEdit = terminal.preferenceObject.getString("script_$scriptName","") ?: ""
                            val tempFile = File(terminal.activity.filesDir, "script.lua")
                            tempFile.writeText(textToEdit)

                            val uri = FileProvider.getUriForFile(
                                terminal.activity,
                                "${terminal.activity.packageName}.provider",
                                tempFile
                            )

                            val intent = Intent(Intent.ACTION_EDIT)
                            intent.setDataAndType(uri, "text/plain")
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                            if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                                val mainAct = terminal.activity as MainActivity
                                mainAct.pendingScriptName = scriptName
                                mainAct.externalEditor.launch(intent)
                            } else {
                                toast(terminal.activity, "No available text editor")
                            }
                        }
                    },

                    negativeButton = terminal.activity.getString(R.string.delete),
                    negativeAction = {
                        terminal.preferenceObject.edit { remove("script_$scriptName") }
                        scripts.remove(scriptName)
                        terminal.preferenceObject.edit {
                            putString(
                                "scripts",
                                scripts.joinToString(";")
                            )
                        }
                        output(terminal.activity.getString(R.string.script_deleted, scriptName),terminal.theme.successTextColor)
                    }
                )
            },
            positiveButton = terminal.activity.getString(R.string.add),
            negativeButton = terminal.activity.getString(R.string.cancel),
            positiveAction = {
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
                            terminal.preferenceObject.edit {
                                putString(
                                    "scripts",
                                    scripts.joinToString(";")
                                )
                            }
                            output(terminal.activity.getString(R.string.script_created, name),terminal.theme.successTextColor)
                        }
                    }
                )
            }
        )
    }
}