package com.coderGtm.yantra.commands.community

import com.coderGtm.yantra.DISCORD_COMMUNITY_URL
import com.coderGtm.yantra.R
import com.coderGtm.yantra.REDDIT_COMMUNITY_URL
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "community",
        helpTitle = "community [discord|reddit]",
        description = terminal.activity.getString(R.string.cmd_community_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size == 1) {
            openURL(DISCORD_COMMUNITY_URL, terminal.activity)
            output(terminal.activity.getString(R.string.opened_community_discord_server),terminal.theme.successTextColor)
            return
        }
        else if (args.size == 2) {
            if (args[1] == "discord") {
                openURL(DISCORD_COMMUNITY_URL, terminal.activity)
                output(terminal.activity.getString(R.string.opened_community_discord_server),terminal.theme.successTextColor)
                return
            }
            else if (args[1] == "reddit") {
                openURL(REDDIT_COMMUNITY_URL, terminal.activity)
                output("Navigating to the Reddit Community!",terminal.theme.successTextColor)
                return
            }
            else {
                output("Invalid community parameter! Yantra Launcher probably does not have a community here.",terminal.theme.errorTextColor)
                return
            }
        }
        else {
            output(terminal.activity.getString(R.string.command_takes_one_param, "community"),terminal.theme.errorTextColor)
            return
        }
    }
}