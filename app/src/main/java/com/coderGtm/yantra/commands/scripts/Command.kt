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

    private fun openEditorDialog(scriptName: String, scripts: ArrayList<String>) {
        YantraLauncherDialog(terminal.activity).selectItem(
            title = terminal.activity.getString(R.string.select_option_for_editing, scriptName),
            items = arrayOf(
                terminal.activity.getString(R.string.launcher_s_editor),
                terminal.activity.getString(R.string.external_editor)
            ),
            clickAction = { option ->
                if (option == 0) {
                    YantraLauncherDialog(terminal.activity).takeInput(
                        title = scriptName,
                        message = terminal.activity.getString(R.string.scripts_disclaimer),
                        initialInput = terminal.preferenceObject.getString("script_$scriptName", "") ?: "",
                        cancellable = false,
                        inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                        positiveButton = terminal.activity.getString(R.string.save),
                        negativeButton = terminal.activity.getString(R.string.cancel),
                        positiveAction = { input ->
                            val scriptBody = input.trim()
                            terminal.preferenceObject.edit {
                                putString("script_$scriptName", scriptBody)
                            }
                            output(terminal.activity.getString(R.string.script_saved_successfully, scriptName), terminal.theme.successTextColor)
                        },
                        negativeAction = {}
                    )
                } else {
                    val textToEdit = terminal.preferenceObject.getString("script_$scriptName", "") ?: ""
                    val tempFile = File(terminal.activity.filesDir, "script.lua")
                    tempFile.writeText(textToEdit)

                    val uri = FileProvider.getUriForFile(
                        terminal.activity,
                        "${terminal.activity.packageName}.provider",
                        tempFile
                    )

                    val intent = Intent(Intent.ACTION_EDIT)
                    intent.setDataAndType(uri, "text/plain")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                        val mainAct = terminal.activity as MainActivity
                        mainAct.pendingScriptName = scriptName
                        mainAct.externalEditor.launch(intent)
                    } else {
                        toast(terminal.activity, terminal.activity.getString(R.string.no_external_editor))
                    }
                }
            }
        )
    }

    override fun execute(command: String) {
        val args = command.trim().split("\\s+".toRegex())
        val scripts = getScripts(terminal.preferenceObject)

        when {
            args.size == 1 -> {
                // List all scripts
                if (scripts.isEmpty()) {
                    output(terminal.activity.getString(R.string.no_scripts_found), terminal.theme.warningTextColor)
                } else {
                    output(terminal.activity.getString(R.string.yantra_scripts))
                    scripts.forEach { output("  • $it") }
                }
            }
            args.size == 2 && args[0] == "scripts" && args[1] != "-new" && args[1] != "-rm" -> {
                // Open editor for existing script: scripts script_name
                val scriptName = args[1]
                if (!scripts.contains(scriptName)) {
                    output(terminal.activity.getString(R.string.script_not_found, scriptName), terminal.theme.errorTextColor)
                    return
                }
                openEditorDialog(scriptName, scripts)
            }
            args.size == 3 && args[1] == "-new" -> {
                // Create new script: scripts -new script_name
                val name = args[2].trim()
                if (name.contains(";") || name.isEmpty()) {
                    output(terminal.activity.getString(R.string.script_name_validation), terminal.theme.errorTextColor)
                    return
                }
                if (!name[0].isLetter() || name.contains(' ')) {
                    output(terminal.activity.getString(R.string.script_name_another_validation), terminal.theme.errorTextColor)
                    return
                }
                if (scripts.contains(name)) {
                    output(terminal.activity.getString(R.string.name_already_taken), terminal.theme.warningTextColor)
                    return
                }
                scripts.add(name)
                terminal.preferenceObject.edit {
                    putString("scripts", scripts.joinToString(";"))
                }
                output(terminal.activity.getString(R.string.script_created, name), terminal.theme.successTextColor)
                openEditorDialog(name, scripts)
            }
            args.size == 3 && args[1] == "-rm" -> {
                // Delete script: scripts -rm script_name
                val scriptName = args[2]
                if (!scripts.contains(scriptName)) {
                    output(terminal.activity.getString(R.string.script_not_found, scriptName), terminal.theme.errorTextColor)
                    return
                }
                val content = terminal.preferenceObject.getString("script_$scriptName", "") ?: ""
                if (content.isNotEmpty()) {
                    output(content)
                }
                terminal.preferenceObject.edit { remove("script_$scriptName") }
                scripts.remove(scriptName)
                terminal.preferenceObject.edit {
                    putString("scripts", scripts.joinToString(";"))
                }
                output(terminal.activity.getString(R.string.script_deleted, scriptName), terminal.theme.successTextColor)
            }
            else -> {
                output(terminal.activity.getString(R.string.scripts_usage), terminal.theme.errorTextColor)
            }
        }
    }
}