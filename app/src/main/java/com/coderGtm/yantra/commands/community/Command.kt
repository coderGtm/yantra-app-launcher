package com.coderGtm.yantra.commands.community

import com.coderGtm.yantra.DISCORD_COMMUNITY_URL
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "community",
        helpTitle = "community",
        description = terminal.activity.getString(R.string.cmd_community_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.community_no_args), terminal.theme.errorTextColor)
            return
        }
        openURL(DISCORD_COMMUNITY_URL, terminal.activity)
        output(terminal.activity.getString(R.string.opened_community_discord_server),terminal.theme.successTextColor)
    }
}