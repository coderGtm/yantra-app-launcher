package com.coderGtm.yantra.commands.unalias

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.commands.alias.updateAliasList
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "unalias",
        helpTitle = "unalias",
        description = "Used to un-alias (remove) an alias.\nUsage: 'unalias alias_name'\nExample: 'unalias h'\nUse 'unalias -1' to remove all aliases"
    )

    override fun execute(command: String) {
        if (command.trim() == "unalias") {
            output("Invalid command. Use 'unalias alias_name' to remove alias", terminal.theme.errorTextColor)
            return
        }
        val cmdArray = command.trim().split(" ")
        if (cmdArray.size == 1) {
            output("Invalid command. Use 'unalias alias_name' to remove alias", terminal.theme.errorTextColor)
            return
        }
        if (cmdArray.size >= 2) {
            if (cmdArray[1].trim() == "-1") {
                // clear aliasList
                terminal.aliasList.clear()
                updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                output("Alias list cleared", terminal.theme.successTextColor)
                return
            }
            val aliasName = cmdArray[1].trim()
            for (i in terminal.aliasList.indices) {
                if (terminal.aliasList[i].key == aliasName) {
                    terminal.aliasList.removeAt(i)
                    updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                    output("Alias '$aliasName' removed.", terminal.theme.successTextColor)
                    return
                }
            }
            output("No alias found for '$aliasName'", terminal.theme.errorTextColor)
            return
        }
        output("Invalid command. See 'help' to get usage info.", terminal.theme.errorTextColor)
    }
}