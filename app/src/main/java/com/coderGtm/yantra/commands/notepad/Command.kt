package com.coderGtm.yantra.commands.notepad

import android.graphics.Typeface
import android.text.InputType
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "notepad",
        helpTitle = terminal.activity.getString(R.string.cmd_notepad_title),
        description = terminal.activity.getString(R.string.cmd_notepad_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2 || args.size > 4) {
            output(terminal.activity.getString(R.string.invalid_parameter_see_help_to_see_usage_info, metadata.name), terminal.theme.errorTextColor)
            return
        }
        if (args.size == 2) {
            if (args[1].trim() == "list") {
                var notes = terminal.preferenceObject.getString("notepad_notes","")?.split(",") ?: listOf()
                if (notes.isNotEmpty() && notes[0].isEmpty()) {
                    notes = notes.drop(1)
                }
                if (notes.isEmpty()) {
                    output(terminal.activity.getString(R.string.no_notes_found), terminal.theme.warningTextColor, Typeface.ITALIC)
                } else {
                    output(terminal.activity.getString(R.string.notepad), terminal.theme.resultTextColor, Typeface.BOLD)
                    notes.forEach {
                        output("--> $it")
                    }
                }
                return
            } else {
                output(terminal.activity.getString(R.string.invalid_parameter_see_help_to_see_usage_info, metadata.name), terminal.theme.errorTextColor)
                return
            }
        } else {
            val action = args[1].trim()
            val name = args[2].trim()
            val notes = terminal.preferenceObject.getString("notepad_notes","")?.split(",") ?: listOf()
            if (action == "new") {
                if (notes.contains(name)) {
                    output(terminal.activity.getString(R.string.note_exists), terminal.theme.errorTextColor)
                    return
                }
                YantraLauncherDialog(terminal.activity).takeInput(
                    title = terminal.activity.getString(R.string.new_note, name),
                    message = terminal.activity.getString(R.string.enter_note_content),
                    cancellable = false,
                    inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE,
                    positiveButton = terminal.activity.getString(R.string.save),
                    positiveAction = {
                        val note_text = it.trim()
                        terminal.preferenceObject.edit().putString("notepad_note_${name}", note_text).apply()
                        val newNotes = notes.toMutableList()
                        newNotes.add(name)
                        terminal.preferenceObject.edit().putString("notepad_notes", newNotes.joinToString(",")).apply()
                        output(terminal.activity.getString(R.string.note_saved_to_notepad, name), terminal.theme.successTextColor)
                    }
                )
                return
            }
            if (action == "read") {
                if (notes.contains(name)) {
                    val note_text = terminal.preferenceObject.getString("notepad_note_${name}", "") ?: ""
                    output(note_text)
                } else {
                    output(terminal.activity.getString(R.string.no_note_fond_by_the_name, name), terminal.theme.errorTextColor)
                }
                return
            }
            if (action == "delete") {
                if (notes.contains(name)) {
                    terminal.preferenceObject.edit().remove("notepad_note_${name}").apply()
                    val newNotes = notes.toMutableList()
                    newNotes.remove(name)
                    terminal.preferenceObject.edit().putString("notepad_notes", newNotes.joinToString(",")).apply()
                    output(terminal.activity.getString(R.string.deleted_from_notepad, name), terminal.theme.successTextColor)
                } else {
                    output(terminal.activity.getString(R.string.no_note_fond_by_the_name, name), terminal.theme.errorTextColor)
                }
                return
            }
        }
    }
}