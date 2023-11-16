package com.coderGtm.yantra.commands.username

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "username",
        helpTitle = "username [new_username]",
        description = "Used to change username. Example: username johnDoe"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify the new username.", terminal.theme.errorTextColor)
            return
        }
        val newUsername = command.trim().removePrefix(args[0]).trim()
        terminal.preferenceObject.edit().putString("username",newUsername).apply()
        terminal.binding.username.text = getUserNamePrefix(terminal.preferenceObject)+newUsername+">"
        output("Username set to $newUsername", terminal.theme.successTextColor)
    }
}