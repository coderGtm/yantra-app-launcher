package com.coderGtm.yantra.commands.wiki

import android.content.ActivityNotFoundException
import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

const val WIKI_URL = "https://codergtm.github.io/yantra-app-launcher/docs/"

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "wiki",
        helpTitle = terminal.activity.getString(R.string.cmd_wiki_title),
        description = terminal.activity.getString(R.string.cmd_wiki_help),
    )

    override fun execute(command: String) {
        output(
            terminal.activity.getString(R.string.opening_in_your_web_browser, WIKI_URL),
            terminal.theme.resultTextColor,
            Typeface.ITALIC,
        )
        try {
            openURL(WIKI_URL, terminal.activity)
        } catch (e: ActivityNotFoundException) {
            output(
                terminal.activity.getString(R.string.you_would_need_a_web_browser_to_open_this_url),
                terminal.theme.errorTextColor,
            )
            output(terminal.activity.getString(R.string.web_tip), terminal.theme.warningTextColor)
        } catch (e: Exception) {
            output(
                terminal.activity.getString(R.string.something_went_wrong_while_opening_the_url),
                terminal.theme.errorTextColor,
            )
        }
    }
}
