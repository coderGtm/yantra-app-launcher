package com.coderGtm.yantra.commands.news

import android.content.ActivityNotFoundException
import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "news",
        helpTitle = "news",
        description = terminal.activity.getString(R.string.cmd_news_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmd_takes_no_params, metadata.name), terminal.theme.errorTextColor)
            return
        }
        output(terminal.activity.getString(R.string.opening_news), terminal.theme.resultTextColor, Typeface.ITALIC)
        try {
            openURL(terminal.preferenceObject.getString("newsWebsite","https://news.google.com/")!!, terminal.activity)
        } catch (e: ActivityNotFoundException) {
            output(terminal.activity.getString(R.string.news_invalid_url), terminal.theme.errorTextColor)
        }
    }
}