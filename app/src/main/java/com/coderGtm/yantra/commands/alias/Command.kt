package com.coderGtm.yantra.commands.alias

import android.graphics.Typeface
import com.coderGtm.yantra.DEFAULT_ALIAS_LIST
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "alias",
        helpTitle = "alias",
        description = terminal.activity.getString(R.string.cmd_alias_help)
    )

    override fun execute(command: String) {
        if (command.trim() == "alias") {
            output("Aliases:", terminal.theme.warningTextColor, Typeface.BOLD_ITALIC)
            output("-------------------------", terminal.theme.warningTextColor)
            for (i in terminal.aliasList.indices) {
                output(terminal.aliasList[i].key + " = " + terminal.aliasList[i].value)
            }
            if (terminal.aliasList.size == 0) {
                output(terminal.activity.getString(R.string.no_alias_found))
            }
            output("-------------------------", terminal.theme.warningTextColor)
            return
        }
        val cmdArray = command.trim().split(" ")
        if (cmdArray.size == 1) {
            output(terminal.activity.getString(R.string.alias_invalid_cmd), terminal.theme.errorTextColor)
            return
        }
        if (cmdArray.size >= 2) {
            if (cmdArray[1].trim() == "-1") {
                if (cmdArray.size > 2) {
                    output(terminal.activity.getString(R.string.alias_invalid_cmd_see_help), terminal.theme.errorTextColor)
                    return
                }
                // set aliasList to default
                terminal.aliasList = DEFAULT_ALIAS_LIST
                updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                output(terminal.activity.getString(R.string.alias_list_set_to_default), terminal.theme.successTextColor)
                return
            }
            val aliasComponents = cmdArray.drop(1).joinToString(" ").split("=")
            if (aliasComponents.size == 1) {
                if (aliasComponents[0].split(" ").size > 1) {
                    output(terminal.activity.getString(R.string.alias_invalid_cmd_see_help), terminal.theme.errorTextColor)
                    return
                }
                val aliasName = aliasComponents[0].split(" ")[0].trim()
                for (i in terminal.aliasList.indices) {
                    if (terminal.aliasList[i].key == aliasName) {
                        output("alias " + aliasName + " = " + terminal.aliasList[i].value)
                        return
                    }
                }
                output(terminal.activity.getString(R.string.no_alias_found_for, aliasName), terminal.theme.errorTextColor)
                return
            }
            if (aliasComponents.size >= 2) {
                val aliasName = aliasComponents[0].trim()
                if (aliasName.trim() in terminal.commands.keys) {
                    output(terminal.activity.getString(R.string.alias_name_cmd_name), terminal.theme.errorTextColor)
                    return
                }
                // check if aliasName contains only alphanumeric characters and starts with a letter
                else if (!aliasName.matches(Regex("^[a-zA-Z][a-zA-Z0-9]*\$")) || aliasName.contains(" ")) {
                    output(terminal.activity.getString(R.string.alias_name_invalid), terminal.theme.errorTextColor)
                    return
                }
                val aliasCmd = aliasComponents.drop(1).joinToString("=").trim()
                for (i in terminal.aliasList.indices) {
                    if (terminal.aliasList[i].key == aliasName) {
                        terminal.aliasList[i] = Alias(aliasName, aliasCmd)
                        updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                        output(terminal.activity.getString(R.string.alias_updated, aliasName), terminal.theme.successTextColor)
                        return
                    }
                }
                terminal.aliasList.add(Alias(aliasName, aliasCmd))
                updateAliasList(terminal.aliasList, terminal.preferenceObject.edit())
                output(terminal.activity.getString(R.string.alias_added, aliasName), terminal.theme.successTextColor)
                return
            }
        }
        output(terminal.activity.getString(R.string.alias_invalid_cmd_see_help), terminal.theme.errorTextColor)
    }
}