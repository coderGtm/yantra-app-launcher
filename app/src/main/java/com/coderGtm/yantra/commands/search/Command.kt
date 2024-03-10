package com.coderGtm.yantra.commands.search

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "search",
        helpTitle = "search",
        description = terminal.activity.getString(R.string.cmd_search_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)
        if (args.isEmpty()) {
            output(terminal.activity.getString(R.string.missing_query), terminal.theme.errorTextColor)
            return
        }

        // check if the user provided a search engine with the -e flag
        if (args.first().trim().startsWith("-e")) {
            val engine = args.first().trim().split("=").last().trim()
            val query = args.drop(1).joinToString(" ").trim()
            if (query.isEmpty()) {
                output(terminal.activity.getString(R.string.missing_query), terminal.theme.errorTextColor)
                return
            }
            val url = when (engine) {
                "google" -> "https://www.google.com/search?q="
                "duckduckgo" -> "https://duckduckgo.com/?q="
                "brave" -> "https://search.brave.com/search?q="
                "bing" -> "https://www.bing.com/search?q="
                "yahoo" -> "https://search.yahoo.com/search?p="
                "ecosia" -> "https://www.ecosia.org/search?q="
                "startpage" -> "https://www.startpage.com/do/search?q="
                "qwant" -> "https://www.qwant.com/?q="
                "you" -> "https://you.com/search?q="
                "playstore" -> "https://play.google.com/store/search?q="
                else -> {
                    output(terminal.activity.getString(R.string.invalid_search_engine), terminal.theme.errorTextColor)
                    return
                }
            }
            output(terminal.activity.getString(R.string.searching_for_with, query, engine), style = Typeface.ITALIC)
            // check if the device has the search engine app installed.
            // if it does, open the search engine app with the query
            // if it doesn't, open the search engine website with the query
            val urlEncodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            if (isSearchEngineAppInstalled(engine, terminal.appList)) {
                openUrlInApp("$url$urlEncodedQuery", query, getPackageName(engine), terminal)
            } else {
                openURL("$url$urlEncodedQuery", terminal.activity)
            }
            return
        }
        else if (args.first().trim().startsWith("-u")) {
            var url = args.first().trim().split("=").drop(1).joinToString("=").trim()
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            val query = args.drop(1).joinToString(" ").trim()
            if (query.isEmpty()) {
                output(terminal.activity.getString(R.string.missing_query), terminal.theme.errorTextColor)
                return
            }
            output(terminal.activity.getString(R.string.searching_for_with_custom_search_engine, query), style = Typeface.ITALIC)
            val urlEncodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            try {
                openURL("$url$urlEncodedQuery", terminal.activity)
            } catch (e: Exception) {
                if (e is ActivityNotFoundException) {
                    output(terminal.activity.getString(R.string.no_app_found_to_open_the_url), terminal.theme.errorTextColor)
                }
                else {
                    output(terminal.activity.getString(R.string.error_opening_url), terminal.theme.errorTextColor)
                }
            }
            return
        }
        else {
            val query = command.trim().removePrefix("search").trim()
            if (query.isEmpty()) {
                output(terminal.activity.getString(R.string.missing_query), terminal.theme.errorTextColor)
                return
            }
            output(terminal.activity.getString(R.string.searching_for_with, query, "google"), style = Typeface.ITALIC)
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