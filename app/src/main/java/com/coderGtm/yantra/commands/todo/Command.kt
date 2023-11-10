package com.coderGtm.yantra.commands.todo

import android.graphics.Typeface
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "todo",
        helpTitle = "todo",
        description = "A simple TODO utility. Use 'todo' to get list of tasks with their indexes and progress. Add a task like 'todo Go for a brisk walk'. Mark tasks as done using their index returned from 'todo' command, like 'todo 2' marks the 3rd task as done. Use 'todo -1' to clear list.\n\nUse the -p argument to mark optional progress for any task, like 'todo -p 1 30' marks the 2nd task as 30% done (Syntax: todo -p taskIndex progress). Use 'todo -p -1' to reset progress for all tasks."
    )

    override fun execute(command: String) {
        val args = command.split("\\s+".toRegex())
        val todolist = getToDo(terminal.preferenceObject)
        val todoProgressList = getToDoProgressList(todolist.size, terminal.preferenceObject)

        if (args.size == 1) {
            if (todolist.size == 0) {
                output("Enjoy! Nothing to do. ¯\\_(ツ)_/¯",terminal.theme.warningTextColor)
                output("Try adding an item like: todo Finish Game server config")
            }
            else {
                output("-------------------------")
                output("TODO List:", terminal.theme.warningTextColor, Typeface.BOLD)
                output("-------------------------")
                for ((i, item) in todolist.withIndex()) {
                    val progress = todoProgressList[i]
                    var todoString = "$i:   $item"
                    if (progress > 0) {
                        todoString += " [${todoProgressList[i]}%]"
                    }
                    output(todoString)
                }
            }
        }
        else if (args[1].trim() == "-1") {
            setToDo(mutableSetOf(), terminal.preferenceObject.edit())
            setToDoProgress(arrayListOf(), terminal.preferenceObject.edit())
            output("Todo list cleared", terminal.theme.successTextColor)
        }
        else if (args[1].trim().toIntOrNull() !== null && args.size == 2) {
            val index = args[1].trim().toInt()
            if (index >= todolist.size) {
                output("TODO List index out of range. Try a number less than ${todolist.size}.",terminal.theme.errorTextColor)
                return
            }
            else if (index < -1) {
                output("Invalid index provided. Use index of -1 to clear the list.",terminal.theme.errorTextColor)
                return
            }
            val toRemove = todolist.elementAt(index)
            todolist.remove(toRemove)
            todoProgressList.removeAt(index)
            setToDo(todolist, terminal.preferenceObject.edit())
            setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
            output("Marked '$toRemove' as completed!",terminal.theme.successTextColor)
        }
        else if (args.size == 3 && args[1].trim() == "-p" && args[2].trim() == "-1") {
            // set all % to 0
            for (i in 0 until todolist.size) {
                todoProgressList[i] = 0
            }
            setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
            output("Progress reset for all tasks!")
            return
        }
        else if (args.size == 4 && args[1].trim() == "-p" && args[2].trim().toIntOrNull() != null && args[3].trim().toIntOrNull() != null) {
            val index = args[2].trim().toInt()
            val percent = args[3].trim().toInt()
            if (index >= todolist.size) {
                output("TODO List index out of range. Try a number less than ${todolist.size}.",terminal.theme.errorTextColor)
                return
            }
            if (index < 0) {
                output("Invalid command usage. Use 'todo -p -1' to reset progress for all tasks.", terminal.theme.errorTextColor)
                return
            }
            if (percent < 0 || percent > 100) {
                output("Invalid progress value. Use a value from 0 to 100.", terminal.theme.errorTextColor)
                return
            }
            if (percent == 100) {
                val toRemove = todolist.elementAt(index)
                todolist.remove(toRemove)
                todoProgressList.removeAt(index)
                setToDo(todolist, terminal.preferenceObject.edit())
                setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
                output("Marked '$toRemove' as completed!",terminal.theme.successTextColor)
                return
            }
            val toUpdate = todolist.elementAt(index)
            todoProgressList[index] = percent
            setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
            output("Marked '$toUpdate' as $percent% done!")
            return
        }
        else {
            val toAdd = command.removePrefix(args[0]).trim()
            if (todolist.add(toAdd)) {
                todoProgressList.add(0)
                setToDo(todolist, terminal.preferenceObject.edit())
                setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
                output("Added '$toAdd' to TODO List")
            }
            else {
                output("Item already present in todo list", terminal.theme.warningTextColor)
            }
        }
    }
}