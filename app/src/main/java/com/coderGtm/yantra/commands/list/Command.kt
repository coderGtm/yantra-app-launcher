package com.coderGtm.yantra.commands.list

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "list",
        helpTitle = "list [component]",
        description = "Lists specified component [apps/themes/contacts]."
    )
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 2) {
            output("Invalid command. See 'help list' for usage info", terminal.theme.errorTextColor)
            return
        }
        if (args.size > 1) {
            if (args[1].lowercase() == "apps") {
                output("Fetching apps...")
                listApps(this)
            }
            else if (args[1].lowercase() == "contacts") {
                output("Fetching Contacts...")
                listContacts(this)
            }
            else if (args[1].lowercase() == "themes") {
                listThemes(this)
            }
            else {
                output("${args[1]} is not a recognized parameter. Try using 'apps'.", terminal.theme.resultTextColor)
            }
        }
        else {
            output("Please specify a list parameter", terminal.theme.errorTextColor)
        }
    }
}