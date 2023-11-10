package com.coderGtm.yantra.commands.google

import android.app.SearchManager
import android.content.Intent
import android.graphics.Typeface
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "google",
        helpTitle = "google [search-query]",
        description = "Searches Google in app or browser for specified query."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Please specify a query to search", terminal.theme.errorTextColor)
        }
        val query = command.removePrefix(args[0])
        //search in google app if installed
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, query.trim())
        if (intent.resolveActivity(terminal.activity.packageManager) != null) {
            terminal.activity.startActivity(intent)
        }
        //else search in browser
        else {
            val url = "https://www.google.com/search?q=${query.trim()}"
            openURL(url, terminal.activity)
        }
        output("Searching '${query.trim()}' in Google...", terminal.theme.resultTextColor, Typeface.ITALIC)
    }
}