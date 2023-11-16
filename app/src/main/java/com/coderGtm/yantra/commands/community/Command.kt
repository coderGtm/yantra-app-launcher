package com.coderGtm.yantra.commands.community

import com.coderGtm.yantra.DISCORD_COMMUNITY_URL
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "community",
        helpTitle = "community",
        description = "Opens the Discord server of Yantra Launcher. Here you can share and get Feedback, Suggestions, Insights, tips and CLI emotions from other users like you!"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'community' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        openURL(DISCORD_COMMUNITY_URL, terminal.activity)
        output("Opened Community Discord Server",terminal.theme.successTextColor)
    }
}