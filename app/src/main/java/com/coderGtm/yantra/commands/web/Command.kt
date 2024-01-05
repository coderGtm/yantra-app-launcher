package com.coderGtm.yantra.commands.web

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.graphics.Typeface
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "web",
        helpTitle = "web <url>",
        description = "Opens the specified URL in your browser, if present, ofc!"
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size <= 1) {
            output("Please specify the URL to open.", terminal.theme.errorTextColor)
            return
        }
        var url = command.removePrefix(args[0]).trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        output("Opening '$url' in your web browser...", terminal.theme.resultTextColor, Typeface.ITALIC)
        try {
            openURL(url, terminal.activity)
        } catch (e: ActivityNotFoundException) {
            output("You would need a web browser to open this URL, which I couldn't find on your device :|", terminal.theme.errorTextColor)
            output("TIP: If you don't want to install a full-fledged web browser, you can use the 'gupt' command to open the URL in a private browsing session, built into Yantra Launcher.", terminal.theme.warningTextColor)
        } catch (e: Exception) {
            output("Something went wrong while opening the URL :(", terminal.theme.errorTextColor)
        }
    }
}