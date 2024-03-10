package com.coderGtm.yantra.commands.list

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "list",
        helpTitle = terminal.activity.getString(R.string.cmd_list_title),
        description = terminal.activity.getString(R.string.cmd_list_help)
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 2) {
            output(terminal.activity.getString(R.string.invalid_list_cmd), terminal.theme.errorTextColor)
            return
        }
        if (args.size > 1) {
            if (args[1].lowercase() == "apps") {
                output(terminal.activity.getString(R.string.fetching_apps))
                listApps(this)
            }
            else if (args[1].lowercase() == "contacts") {
                output(terminal.activity.getString(R.string.fetching_contacts))
                listContacts(this)
            }
            else if (args[1].lowercase() == "themes") {
                listThemes(this)
            }
            else {
                output(terminal.activity.getString(R.string.list_unknow_param, args[1]), terminal.theme.resultTextColor)
            }
        }
        else {
            output(terminal.activity.getString(R.string.list_no_param), terminal.theme.errorTextColor)
        }
    }
}