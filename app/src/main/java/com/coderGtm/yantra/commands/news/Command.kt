package com.coderGtm.yantra.commands.news

import android.content.ActivityNotFoundException
import android.graphics.Typeface
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "news",
        helpTitle = "news",
        description = "Opens the news website configured via settings. Defaults to Google News"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'news' command does not take any parameters", terminal.theme.errorTextColor)
            return
        }
        output("Opening News...", terminal.theme.resultTextColor, Typeface.ITALIC)
        try {
            openURL(terminal.preferenceObject.getString("newsWebsite","https://news.google.com/")!!, terminal.activity)
        } catch (e: ActivityNotFoundException) {
            output("Could not open the news website. Please check the URL in settings.", terminal.theme.errorTextColor)
        }
    }
}