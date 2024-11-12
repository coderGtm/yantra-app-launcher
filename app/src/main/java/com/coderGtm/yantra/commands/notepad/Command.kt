package com.coderGtm.yantra.commands.notepad

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "notepad",
        helpTitle = "notepad [list / new <name> / read <name> / delete <name>]",
        description = "A simple in-app notepad to keep important notes. The 'notepad list' command lists all notes. To create a new note, use the 'notepad new <note_name>' command. To read an existing note, use the 'notepad read <note_name>' command. Use the 'notepad delete <note_name>' command to delete a note."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2 || args.size > 4) {
            output(terminal.activity.getString(R.string.invalid_parameter_see_help_to_see_usage_info, metadata.name), terminal.theme.errorTextColor)
            return
        }
        if (args.size == 2) {
            if (args[1].trim() == "list") {
                val notes = terminal.preferenceObject.getString("notepad_notes","")?.split(",") ?: listOf()
                if (notes.isEmpty()) {
                    output("No notes found...", terminal.theme.warningTextColor, Typeface.ITALIC)
                } else {
                    output("Notepad:", terminal.theme.resultTextColor, Typeface.BOLD)
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
                    output("A note by this name already exists! please use a different name.", terminal.theme.errorTextColor)
                    return
                }
                YantraLauncherDialog(terminal.activity).takeInput(
                    title = "New Note ($name)",
                    message = "Enter the note content below:",
                    cancellable = false,
                    positiveButton = "Save",
                    positiveAction = {
                        val note_text = it.trim()
                        terminal.preferenceObject.edit().putString("notepad_note_${name}", note_text).apply()
                        val newNotes = notes.toMutableList()
                        newNotes.add(name)
                        terminal.preferenceObject.edit().putString("notepad_notes", newNotes.joinToString(",")).apply()
                        output("Note '$name' saved to notepad.", terminal.theme.successTextColor)
                    }
                )
                return
            }
            if (action == "read") {
                if (notes.contains(name)) {
                    val note_text = terminal.preferenceObject.getString("notepad_note_${name}", "") ?: ""
                    output(note_text)
                } else {
                    output("No note fond by the name ${name}!", terminal.theme.errorTextColor)
                }
                return
            }
            if (action == "delete") {
                if (notes.contains(name)) {
                    terminal.preferenceObject.edit().remove("notepad_note_${name}").apply()
                    val newNotes = notes.toMutableList()
                    newNotes.remove(name)
                    terminal.preferenceObject.edit().putString("notepad_notes", newNotes.joinToString(",")).apply()
                    output("Deleted '$name' from notepad.", terminal.theme.successTextColor)
                } else {
                    output("No note fond by the name ${name}!", terminal.theme.errorTextColor)
                }
                return
            }
        }
    }
}