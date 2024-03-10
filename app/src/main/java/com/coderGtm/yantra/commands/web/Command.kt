package com.coderGtm.yantra.commands.web

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "web",
        helpTitle = terminal.activity.getString(R.string.cmd_web_title),
        description = terminal.activity.getString(R.string.cmd_web_help)
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size <= 1) {
            output(terminal.activity.getString(R.string.please_specify_the_url_to_open), terminal.theme.errorTextColor)
            return
        }
        var url = command.removePrefix(args[0]).trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        output(terminal.activity.getString(R.string.opening_in_your_web_browser, url), terminal.theme.resultTextColor, Typeface.ITALIC)
        try {
            openURL(url, terminal.activity)
        } catch (e: ActivityNotFoundException) {
            output(terminal.activity.getString(R.string.you_would_need_a_web_browser_to_open_this_url), terminal.theme.errorTextColor)
            output(terminal.activity.getString(R.string.web_tip), terminal.theme.warningTextColor)
        } catch (e: Exception) {
            output(terminal.activity.getString(R.string.something_went_wrong_while_opening_the_url), terminal.theme.errorTextColor)
        }
    }
}