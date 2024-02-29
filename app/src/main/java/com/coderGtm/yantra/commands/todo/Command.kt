package com.coderGtm.yantra.commands.todo

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "todo",
        helpTitle = "todo",
        description = terminal.activity.getString(R.string.cmd_todo_help)
    )

    override fun execute(command: String) {
        val args = command.split("\\s+".toRegex())
        val todolist = getToDo(terminal.preferenceObject)
        val todoProgressList = getToDoProgressList(todolist.size, terminal.preferenceObject)

        if (args.size == 1) {
            if (todolist.size == 0) {
                output(terminal.activity.getString(R.string.enjoy_nothing_to_do),terminal.theme.warningTextColor)
                output(terminal.activity.getString(R.string.try_adding_an_item))
            }
            else {
                output("-------------------------")
                output(terminal.activity.getString(R.string.todo_list), terminal.theme.warningTextColor, Typeface.BOLD)
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
            output(terminal.activity.getString(R.string.todo_list_cleared), terminal.theme.successTextColor)
        }
        else if (args[1].trim().toIntOrNull() !== null && args.size == 2) {
            val index = args[1].trim().toInt()
            if (index >= todolist.size) {
                output(terminal.activity.getString(R.string.todo_list_index_out_of_range, todolist.size),terminal.theme.errorTextColor)
                return
            }
            else if (index < -1) {
                output(terminal.activity.getString(R.string.todo_invalid_index_provided),terminal.theme.errorTextColor)
                return
            }
            val toRemove = todolist.elementAt(index)
            todolist.remove(toRemove)
            todoProgressList.removeAt(index)
            setToDo(todolist, terminal.preferenceObject.edit())
            setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
            output(terminal.activity.getString(R.string.marked_as_completed, toRemove),terminal.theme.successTextColor)
        }
        else if (args.size == 3 && args[1].trim() == "-p" && args[2].trim() == "-1") {
            // set all % to 0
            for (i in 0 until todolist.size) {
                todoProgressList[i] = 0
            }
            setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
            output(terminal.activity.getString(R.string.progress_reset_for_all_tasks))
            return
        }
        else if (args.size == 4 && args[1].trim() == "-p" && args[2].trim().toIntOrNull() != null && args[3].trim().toIntOrNull() != null) {
            val index = args[2].trim().toInt()
            val percent = args[3].trim().toInt()
            if (index >= todolist.size) {
                output(terminal.activity.getString(R.string.todo_list_index_out_of_range, todolist.size),terminal.theme.errorTextColor)
                return
            }
            if (index < 0) {
                output(terminal.activity.getString(R.string.todo_p_invalid), terminal.theme.errorTextColor)
                return
            }
            if (percent < 0 || percent > 100) {
                output(terminal.activity.getString(R.string.invalid_progress_value), terminal.theme.errorTextColor)
                return
            }
            if (percent == 100) {
                val toRemove = todolist.elementAt(index)
                todolist.remove(toRemove)
                todoProgressList.removeAt(index)
                setToDo(todolist, terminal.preferenceObject.edit())
                setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
                output(terminal.activity.getString(R.string.marked_as_completed, toRemove),terminal.theme.successTextColor)
                return
            }
            val toUpdate = todolist.elementAt(index)
            todoProgressList[index] = percent
            setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
            output(terminal.activity.getString(R.string.marked_as_pct_done, toUpdate, percent))
            return
        }
        else {
            val toAdd = command.removePrefix(args[0]).trim()
            if (todolist.add(toAdd)) {
                todoProgressList.add(0)
                setToDo(todolist, terminal.preferenceObject.edit())
                setToDoProgress(todoProgressList, terminal.preferenceObject.edit())
                output(terminal.activity.getString(R.string.added_to_todo_list, toAdd))
            }
            else {
                output(terminal.activity.getString(R.string.item_already_present_in_todo), terminal.theme.warningTextColor)
            }
        }
    }
}