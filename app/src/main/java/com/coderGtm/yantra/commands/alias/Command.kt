package com.coderGtm.yantra.commands.alias

import android.graphics.Typeface
import com.coderGtm.yantra.DEFAULT_ALIAS_LIST
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "alias",
        helpTitle = "alias",
        description = "Unix-like aliasing system to set short-hand commands for available commands. Note that Yantra Launcher saves all aliases in memory and is retained even after restarting the Launcher session. Pre-defined commands can not be aliased.\nUsage (to set or update an alias): 'alias alias_name = alias_cmd'\nExample: 'alias h = help'\nUse 'alias alias_name' to get value of the alias. Use 'alias' to get list of all current aliases. Use 'alias -1' to reset to default aliases."
    )

    override fun execute(command: String) {
        if (command.trim() == "alias") {
            output("Aliases:", terminal.theme.warningTextColor, Typeface.BOLD_ITALIC)
            output("-------------------------", terminal.theme.warningTextColor)
            for (i in terminal.aliasList.indices) {
                output(terminal.aliasList[i].key + " = " + terminal.aliasList[i].value)
            }
            if (terminal.aliasList.size == 0) {
                output("No alias found.")
            }
            output("-------------------------", terminal.theme.warningTextColor)
            return
        }
        val cmdArray = command.trim().split(" ")
        if (cmdArray.size == 1) {
            output("Invalid command. Use 'alias' to get list of aliases.", terminal.theme.errorTextColor)
            return
        }
        if (cmdArray.size >= 2) {
            if (cmdArray[1].trim() == "-1") {
                if (cmdArray.size > 2) {
                    output("Invalid command. See 'help alias' for usage info", terminal.theme.errorTextColor)
                    return
                }
                // set aliasList to default
                terminal.aliasList = DEFAULT_ALIAS_LIST
                updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                output("Alias list set to default", terminal.theme.successTextColor)
                return
            }
            val aliasComponents = cmdArray.drop(1).joinToString(" ").split("=")
            if (aliasComponents.size == 1) {
                if (aliasComponents[0].split(" ").size > 1) {
                    output("Invalid command. See 'help alias' for usage info", terminal.theme.errorTextColor)
                    return
                }
                val aliasName = aliasComponents[0].split(" ")[0].trim()
                for (i in terminal.aliasList.indices) {
                    if (terminal.aliasList[i].key == aliasName) {
                        output("alias " + aliasName + " = " + terminal.aliasList[i].value)
                        return
                    }
                }
                output("No alias found for '$aliasName'", terminal.theme.errorTextColor)
                return
            }
            if (aliasComponents.size >= 2) {
                val aliasName = aliasComponents[0].trim()
                if (aliasName.trim() in terminal.commands.keys) {
                    output("Alias name cannot be an existing command name.", terminal.theme.errorTextColor)
                    return
                }
                // check if aliasName contains only alphanumeric characters and starts with a letter
                else if (!aliasName.matches(Regex("^[a-zA-Z][a-zA-Z0-9]*\$")) || aliasName.contains(" ")) {
                    output("Alias name must contain only alphanumeric characters and must start with a letter", terminal.theme.errorTextColor)
                    return
                }
                val aliasCmd = aliasComponents.drop(1).joinToString("=").trim()
                for (i in terminal.aliasList.indices) {
                    if (terminal.aliasList[i].key == aliasName) {
                        terminal.aliasList[i] = Alias(aliasName, aliasCmd)
                        updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                        output("Alias '$aliasName' updated.", terminal.theme.successTextColor)
                        return
                    }
                }
                terminal.aliasList.add(Alias(aliasName, aliasCmd))
                updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                output("Alias '$aliasName' added.", terminal.theme.successTextColor)
                return
            }
        }
        output("Invalid command. See 'help alias' to get usage info.", terminal.theme.errorTextColor)
    }
}