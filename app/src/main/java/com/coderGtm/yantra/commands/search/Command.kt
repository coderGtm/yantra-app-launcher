package com.coderGtm.yantra.commands.search

import android.app.SearchManager
import android.content.Intent
import android.graphics.Typeface
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "search",
        helpTitle = "search",
        description = "Searches the internet for the provided query. Search engine can be specified with the -e flag (-e=google|duckduckgo|bing|yahoo|ecosia|startpage|qwant|you). Default is google. You can use a custom search engine by specifying the url with the -u flag (-u=https://example.com/search?q=). The query is the only required argument that is provided at the end of the command. It is automatically URL encoded during execution of the command. Examples:\n'search Yantra Launcher'\n'search -e=duckduckgo Yantra Launcher'\n'search -u=https://example.com/search?q= Yantra Launcher'"
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)
        if (args.isEmpty()) {
            output("Please provide a query.", terminal.theme.errorTextColor)
            return
        }

        // check if the user provided a search engine with the -e flag
        if (args.first().trim().startsWith("-e")) {
            val engine = args.first().trim().split("=").last().trim()
            val query = args.drop(1).joinToString(" ").trim()
            if (query.isEmpty()) {
                output("Please provide a query.", terminal.theme.errorTextColor)
                return
            }
            val url = when (engine) {
                "google" -> "https://www.google.com/search?q="
                "duckduckgo" -> "https://duckduckgo.com/?q="
                "bing" -> "https://www.bing.com/search?q="
                "yahoo" -> "https://search.yahoo.com/search?p="
                "ecosia" -> "https://www.ecosia.org/search?q="
                "startpage" -> "https://www.startpage.com/do/search?q="
                "qwant" -> "https://www.qwant.com/?q="
                "you" -> "https://you.com/search?q="
                else -> {
                    output("Invalid search engine. See 'help search' to get info about search engine keywords.", terminal.theme.errorTextColor)
                    return
                }
            }
            output("Searching for '$query' with $engine...", style = Typeface.ITALIC)
            // check if the device has the search engine app installed.
            // if it does, open the search engine app with the query
            // if it doesn't, open the search engine website with the query
            val urlEncodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            if (isSearchEngineAppInstalled(engine, terminal.appList)) {
                openUrlInApp("$url$urlEncodedQuery", getPackageName(engine), engine, terminal)
            } else {
                openURL("$url$urlEncodedQuery", terminal.activity)
            }
            return
        }
        else if (args.first().trim().startsWith("-u")) {
            val url = args.first().trim().split("=").last().trim()
            val query = args.drop(1).joinToString(" ").trim()
            if (query.isEmpty()) {
                output("Please provide a query.", terminal.theme.errorTextColor)
                return
            }
            output("Searching for '$query' with custom search engine...", style = Typeface.ITALIC)
            val urlEncodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            openURL("$url$urlEncodedQuery", terminal.activity)
            return
        }
        else {
            val query = command.trim().removePrefix("search").trim()
            if (query.isEmpty()) {
                output("Please provide a query.", terminal.theme.errorTextColor)
                return
            }
            output("Searching for '$query' with google...", style = Typeface.ITALIC)
            val urlEncodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            //search in google app if installed
            val intent = Intent(Intent.ACTION_WEB_SEARCH)
            intent.putExtra(SearchManager.QUERY, query.trim())
            if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                terminal.activity.startActivity(intent)
            }
            //else search in browser
            else {
                val url = "https://www.google.com/search?q=$urlEncodedQuery"
                openURL(url, terminal.activity)
            }
        }
    }
}