package com.coderGtm.yantra.commands.unalias

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.commands.alias.updateAliasList
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "unalias",
        helpTitle = "unalias",
        description = terminal.activity.getString(R.string.cmd_unalias_help)
    )

    override fun execute(command: String) {
        if (command.trim() == "unalias") {
            output(terminal.activity.getString(R.string.unalias_give_something), terminal.theme.errorTextColor)
            return
        }
        val cmdArray = command.trim().split(" ")
        if (cmdArray.size == 1) {
            output(terminal.activity.getString(R.string.unalias_give_something), terminal.theme.errorTextColor)
            return
        }
        if (cmdArray.size >= 2) {
            if (cmdArray[1].trim() == "-1") {
                // clear aliasList
                terminal.aliasList.clear()
                updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                output(terminal.activity.getString(R.string.alias_list_cleared), terminal.theme.successTextColor)
                return
            }
            val aliasName = cmdArray[1].trim()
            for (i in terminal.aliasList.indices) {
                if (terminal.aliasList[i].key == aliasName) {
                    terminal.aliasList.removeAt(i)
                    updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                    output(terminal.activity.getString(R.string.alias_removed, aliasName), terminal.theme.successTextColor)
                    return
                }
            }
            output(terminal.activity.getString(R.string.no_alias_found_for, aliasName), terminal.theme.errorTextColor)
            return
        }
        output(terminal.activity.getString(R.string.unalias_see_help), terminal.theme.errorTextColor)
    }
}