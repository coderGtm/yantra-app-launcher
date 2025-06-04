package com.coderGtm.yantra.terminal

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.coderGtm.yantra.R
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.commands.todo.getToDo
import com.coderGtm.yantra.Croissant
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.loadPrimarySuggestionsOrder
import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.Suggestion
import com.coderGtm.yantra.models.Theme
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.setSystemWallpaper
import java.util.Locale
import java.util.regex.Pattern

fun showSuggestions(
    rawInput: String,
    getPrimarySuggestions: Boolean,
    getSecondarySuggestions: Boolean,
    terminal: Terminal
) {
    Thread {
        terminal.activity.runOnUiThread {
            terminal.binding.suggestionsTab.removeAllViews()
        }
        val input = rawInput.trim()
        val suggestions = ArrayList<String>()
        val args = input.split(" ")
        var overrideLastWord = false
        var isPrimary = true
        var executeOnTapViable = true

        if ((args.isEmpty() || (args.size == 1 && terminal.binding.cmdInput.text.toString().lastOrNull() != ' ')) && getPrimarySuggestions) {
            overrideLastWord = true
            val regex = Regex(Pattern.quote(args[0]), RegexOption.IGNORE_CASE)
            val allPrimarySuggestions: MutableList<Suggestion> = terminal.primarySuggestions
            for (ps in allPrimarySuggestions) {
                if (!ps.isHidden && regex.containsMatchIn(ps.text) && !suggestions.contains(ps.text)) {
                    suggestions.add(ps.text)
                }
            }
        }
        else if ((args.size > 1 || (args.size == 1 && terminal.binding.cmdInput.text.toString().lastOrNull() == ' ')) && getSecondarySuggestions) {
            // check for alias
            val effectivePrimaryCmd: String
            val isAliasCmd = terminal.aliasList.any { it.key == args[0] }
            effectivePrimaryCmd = if (isAliasCmd) {
                terminal.aliasList.first { it.key == args[0] }.value
            } else {
                args[0].lowercase()
            }
            if (!terminal.commands.containsKey(effectivePrimaryCmd)) {
                return@Thread
            }
            val reg = input.removePrefix(args[0]).trim()
            if (effectivePrimaryCmd == "launch") {
                if (!terminal.appListFetched) {
                    return@Thread
                }
                val candidates = terminal.appList.map { it.appName }.toMutableList()
                candidates.add(0, "-s")
                candidates.add(0, "-p")
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true

                    val regex = Regex(Pattern.quote(reg), RegexOption.IGNORE_CASE)
                    for (app in candidates) {
                        if (regex.containsMatchIn(app) && !suggestions.contains(app)) {
                            if (app.substring(0, reg.length).lowercase() == reg && reg.isNotEmpty()){
                                suggestions.add(0, app)
                                continue
                            }
                            suggestions.add(app)
                        }
                    }
                }
                else {
                    for (app in candidates) {
                        if (!suggestions.contains(app)) {
                            suggestions.add(app)
                        }
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "open") {
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true
                    val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (file in getFiles(terminal)) {
                        if (regex.containsMatchIn(file) && !suggestions.contains(file)) {
                            if (file.substring(0, reg.length).lowercase() == reg && reg.isNotEmpty()){
                                suggestions.add(0, file)
                                continue
                            }
                            suggestions.add(file)
                        }
                    }
                }
                else {
                    for (file in getFiles(terminal)) {
                        if (!suggestions.contains(file)) {
                            suggestions.add(file)
                        }
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "cd") {
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true
                    val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (file in getFolders(terminal)) {
                        if (regex.containsMatchIn(file) && !suggestions.contains(file)) {
                            if (file.substring(0, reg.length).lowercase() == reg && reg.isNotEmpty()){
                                suggestions.add(0, file)
                                continue
                            }
                            suggestions.add(file)
                        }
                    }
                }
                else {
                    for (file in getFolders(terminal)) {
                        if (!suggestions.contains(file)) {
                            suggestions.add(file)
                        }
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "sfx") {
                if (args.size > 1) {
                    overrideLastWord = true
                    val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (file in terminal.activity.filesDir.listFiles()) {
                        if ((file.name.endsWith(".mp3") || file.name.endsWith(".wav") || file.name.endsWith(".ogg")) && file.isFile) {
                            if (regex.containsMatchIn(file.name) && !suggestions.contains(file.name)) {
                                suggestions.add(file.name.removeSuffix(".mp3").removeSuffix(".wav").removeSuffix(".ogg"))
                            }
                        }
                    }
                }
                else {
                    for (file in terminal.activity.filesDir.listFiles()) {
                        if ((file.name.endsWith(".mp3") || file.name.endsWith(".wav") || file.name.endsWith(".ogg")) && file.isFile) {
                            suggestions.add(file.name.removeSuffix(".mp3").removeSuffix(".wav").removeSuffix(".ogg"))
                        }
                    }
                }
            }
            else if (effectivePrimaryCmd == "ls") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val lsArgs = listOf("-a")
                for (arg in lsArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "uninstall") {
                if (!terminal.appListFetched) {
                    return@Thread
                }
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true
                    val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (app in terminal.appList) {
                        if (regex.containsMatchIn(app.appName) && !suggestions.contains(app.appName)) {
                            if (app.appName.substring(0, reg.length).lowercase() == reg && reg.isNotEmpty()){
                                suggestions.add(0, app.appName)
                                continue
                            }
                            suggestions.add(app.appName)
                        }
                    }
                }
                else {
                    for (app in terminal.appList) {
                        if (!suggestions.contains(app.appName)) {
                            suggestions.add(app.appName)
                        }
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "info") {
                if (!terminal.appListFetched) {
                    return@Thread
                }
                val candidates = terminal.appList.map { it.appName }.toMutableList()
                candidates.add(0, "-p")
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true
                    val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (app in candidates) {
                        if (regex.containsMatchIn(app) && !suggestions.contains(app)) {
                            if (app.substring(0, reg.length).lowercase() == reg && reg.isNotEmpty()){
                                suggestions.add(0, app)
                                continue
                            }
                            suggestions.add(app)
                        }
                    }
                }
                else {
                    for (app in candidates) {
                        if (!suggestions.contains(app)) {
                            suggestions.add(app)
                        }
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "launchf") {
                if (!terminal.appListFetched) {
                    return@Thread
                }
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true
                    val name = input.removePrefix(args[0]).trim().lowercase()
                    val candidates = mutableListOf<Double>()
                    for (app in terminal.appList) {
                        val score = findSimilarity(app.appName.lowercase(), name)
                        candidates.add(score)
                        //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
                    }
                    val maxIndex = candidates.indexOf(candidates.max())
                    val appBlock = terminal.appList[maxIndex]
                    suggestions.add(appBlock.appName)
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "screentime") {
                if (!terminal.appListFetched) {
                    return@Thread
                }
                val screentimeArgs = listOf("-all") + terminal.appList.map { it.appName }
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true
                    val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (arg in screentimeArgs) {
                        if (regex.containsMatchIn(arg) && !suggestions.contains(arg)) {
                            suggestions.add(arg)
                        }
                    }
                }
                else {
                    for (arg in screentimeArgs) {
                        if (!suggestions.contains(arg)) {
                            suggestions.add(arg)
                        }
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "call") {
                if (!terminal.contactsFetched) {
                    terminal.activity.runOnUiThread {
                        terminal.binding.suggestionsTab.removeAllViews()
                    }
                    val tv = TextView(terminal.activity)
                    tv.text = terminal.activity.getString(R.string.contacts_not_fetched_yet)
                    tv.setTextColor(terminal.theme.suggestionTextColor)
                    //italics
                    tv.setTypeface(terminal.typeface, Typeface.BOLD_ITALIC)
                    terminal.activity.runOnUiThread {
                        terminal.binding.suggestionsTab.addView(tv)
                    }
                    return@Thread
                }
                else if (args.size>1) {
                    //search using regex
                    overrideLastWord = true
                    val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (contact in terminal.contactNames) {
                        if (regex.containsMatchIn(contact)) {
                            suggestions.add(contact)
                        }
                    }
                }
                else {
                    for (contact in terminal.contactNames) {
                        if (!suggestions.contains(contact)) {
                            suggestions.add(contact)
                        }
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "list") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val listArgs = listOf("apps","shortcuts","themes","contacts")
                for (arg in listArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "notepad") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val notepadArgs = listOf("list","read","new","edit","delete")
                for (arg in notepadArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "community") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val communityArgs = listOf("discord","reddit")
                for (arg in communityArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "search") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val listArgs = listOf("-e=google","-e=duckduckgo","-e=brave","-e=bing","-e=yahoo","-e=ecosia","-e=startpage","-e=qwant","-e=you","-e=playstore","-e=maps", "-e=youtube","-u=")
                for (arg in listArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
                executeOnTapViable = false
            }
            else if (effectivePrimaryCmd == "battery") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val batteryArgs = listOf("-bar")
                for (arg in batteryArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "dict") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val dictArgs = listOf("-urban")
                for (arg in dictArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
                executeOnTapViable = false
            }
            else if (effectivePrimaryCmd == "flash" || effectivePrimaryCmd == "bluetooth") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val listArgs = listOf("1","0","on","off")
                for (arg in listArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "todo") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val listArgs = mutableListOf("-p","-1")
                val todoSize = getToDo(terminal.preferenceObject).size
                for (i in 0 until todoSize) {
                    listArgs.add(i.toString())
                }
                for (arg in listArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
                executeOnTapViable = false
            }
            else if (effectivePrimaryCmd == "help") {
                if (args.size>1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                terminal.commands.keys.filterTo(suggestions) { regex.containsMatchIn(it) }
                isPrimary = false

            }
            else if (effectivePrimaryCmd == "alias") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                if (regex.containsMatchIn("-1")) {
                    suggestions.add("-1")
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "unalias") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val unaliasArgs = terminal.aliasList.toMutableList()
                unaliasArgs.add(0, Alias("-1",""))
                unaliasArgs
                    .filter { regex.containsMatchIn(it.key) }
                    .mapTo(suggestions) { it.key }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "theme") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val themeArgs = mutableListOf("Custom")
                Themes.entries.forEach { themeArgs.add(it.name) }
                themeArgs.filterTo(suggestions) { regex.containsMatchIn(it) }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "sysinfo") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val sysInfoArgs= listOf("-os", "-host", "-kernel", "-uptime", "-apps", "-terminal", "-font", "-resolution", "-theme", "-cpu", "-memory", "-art")
                for (arg in sysInfoArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "bg") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val bgArgs= listOf("-1", "random")
                for (arg in bgArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
                executeOnTapViable = false
            }
            else if (effectivePrimaryCmd == "echo") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val echoArgs= listOf("-e", "-s", "-w")
                for (arg in echoArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
                executeOnTapViable = false
            }
            else if (effectivePrimaryCmd == "time") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val timeArgs= listOf("utc")
                for (arg in timeArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
                executeOnTapViable = false
            }
            else if (effectivePrimaryCmd == "backup") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val backupArgs = listOf("-i")
                for (arg in backupArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "music") {
                if (args.size > 1) {
                    overrideLastWord = true
                }
                val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                val musicArgs = listOf("play", "pause", "prev", "next")
                for (arg in musicArgs) {
                    if (regex.containsMatchIn(arg)) {
                        suggestions.add(arg)
                    }
                }
                isPrimary = false
            }
            else if (effectivePrimaryCmd == "run") {
                try {
                    val runArgs = getScripts(terminal.preferenceObject).toMutableList()
                    runArgs.add(0, "-lua")
                    runArgs.add(0, "-clean")
                    if (args.size>1) {
                        overrideLastWord = true
                        val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                        for (ra in runArgs) {
                            if (regex.containsMatchIn(ra)) {
                                suggestions.add(ra)
                            }
                        }
                    }
                    else {
                        for (ra in runArgs) {
                            if (!suggestions.contains(ra)) {
                                suggestions.add(ra)
                            }
                        }
                    }
                    isPrimary = false
                }
                catch (e: java.lang.Exception) {
                    return@Thread
                }
            }
        }
        suggestions.forEach { sug ->
            if ((isPrimary && (input.trim() == sug.trim())) || (!isPrimary && (input.removePrefix(args[0]).trim() == sug.trim()))) {
                return@forEach
            }
            val suggestion = TextView(terminal.activity)
            suggestion.text = sug.uppercase(Locale.getDefault())
            suggestion.setTextColor(terminal.theme.suggestionTextColor)
            suggestion.setTypeface(terminal.typeface, Typeface.BOLD)
            suggestion.setBackgroundColor(Color.TRANSPARENT)
            suggestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5F)
            suggestion.setPadding(40, 30, 40, 30)


            suggestion.setOnClickListener {
                val newCmd = if (overrideLastWord) {
                    input.substring(0, input.length-args[args.size-1].length) + sug + " "
                }
                else {
                    "$input $sug "
                }
                val actOnSuggestionTap = terminal.preferenceObject.getBoolean("actOnSuggestionTap", false)
                if (!isPrimary && actOnSuggestionTap && executeOnTapViable) {
                    terminal.handleCommand(newCmd)
                    terminal.binding.cmdInput.setText("")
                }
                else {
                    terminal.binding.cmdInput.setText(newCmd)
                    terminal.binding.cmdInput.setSelection(terminal.binding.cmdInput.text!!.length)
                    requestCmdInputFocusAndShowKeyboard(terminal.activity, terminal.binding)
                    terminal.binding.suggestionsTab.removeView(it)
                }

            }
            if (isPrimary) {
                suggestion.setOnLongClickListener {
                    try {
                        val commandClass = terminal.commands[sug]
                        if (commandClass != null) {
                            val cmdMetadata = commandClass.getDeclaredConstructor(Terminal::class.java).newInstance(terminal).metadata
                            YantraLauncherDialog(terminal.activity).showInfo(
                                title = cmdMetadata.helpTitle,
                                message = cmdMetadata.description,
                                positiveButton = terminal.activity.getString(R.string.ok),
                            )
                        }
                    }
                    catch (e: Exception) {}
                    true
                }
            }
            terminal.activity.runOnUiThread {
                terminal.binding.suggestionsTab.addView(suggestion)
            }
        }
        if (suggestions.size == 1 && !isPrimary && terminal.preferenceObject.getBoolean("actOnLastSecondarySuggestion", false)) {
            // auto execute if only one suggestion
            val effectivePrimaryCmd: String
            val isAliasCmd = terminal.aliasList.any { it.key == args[0] }
            effectivePrimaryCmd = if (isAliasCmd) {
                terminal.aliasList.first { it.key == args[0] }.value
            } else {
                args[0].lowercase()
            }
            // dont auto execute for some commands
            if (effectivePrimaryCmd == "call" || effectivePrimaryCmd == "time" || effectivePrimaryCmd == "bg" || effectivePrimaryCmd == "notepad" || effectivePrimaryCmd == "todo" || effectivePrimaryCmd == "run") {
                return@Thread
            }
            // dont auto execute if only flag suggestion
            if (suggestions[0].startsWith("-")) {
                return@Thread
            }
            // dont auto execute if no input after primary command
            if (args.size == 1) {
                return@Thread
            }

            terminal.activity.runOnUiThread {
                terminal.output(terminal.activity.getString(R.string.auto_executing_suggestion), terminal.theme.successTextColor, Typeface.ITALIC)

                val sug = suggestions.first()
                val newCmd = if (overrideLastWord) {
                    input.substring(0, input.length-args[args.size-1].length) + sug + " "
                }
                else {
                    "$input $sug "
                }
                terminal.handleCommand(newCmd)
                terminal.binding.suggestionsTab.removeAllViews()
                suggestions.clear()

                // clear input now and after 100ms delay
                Thread {
                    Thread.sleep(500)
                    terminal.activity.runOnUiThread {
                        terminal.binding.cmdInput.setText("")
                    }
                }.start()
                terminal.binding.cmdInput.setText("")
            }
        }
    }.start()
}

fun getFolders(terminal: Terminal): List<String> {
    val files = Croissant().getListOfObjects(terminal, terminal.workingDir)

    val fullList = mutableListOf<String>()

    for (file in files) {
        if (file.isDirectory && !file.isHidden) {
            fullList.add(file.name)
        }
    }

    fullList.sort()
    return fullList
}

fun getFiles(terminal: Terminal): List<String> {
    val files = Croissant().getListOfObjects(terminal, terminal.workingDir)

    val fullList = mutableListOf<String>()

    for (file in files) {
        if (!file.isDirectory) {
            fullList.add(file.name)
        }
    }

    fullList.sort()
    return fullList
}

fun setWallpaperIfNeeded(preferenceObject: SharedPreferences, applicationContext: Context, curTheme: Theme, ) {
    if (preferenceObject.getBoolean("defaultWallpaper",true)) {
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val colorDrawable = ColorDrawable(curTheme.bgColor)
        setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(applicationContext.resources.displayMetrics.widthPixels, applicationContext.resources.displayMetrics.heightPixels))
    }
}

fun reorderPrimarySuggestions(preferenceObject: SharedPreferences, suggestionList: List<Suggestion>): MutableList<Suggestion> {
    val savedOrder = loadPrimarySuggestionsOrder(preferenceObject)
    val reorderedSuggestions = mutableListOf<Suggestion>()
    if (savedOrder != null) {
        for (sug in savedOrder) {
            suggestionList.firstOrNull { it.text == sug.text }?.let {
                reorderedSuggestions.add(sug)
            }
        }
        // Add any remaining commands that weren't reordered by the user
        suggestionList.forEach { (command) ->
            if (!reorderedSuggestions.any { it.text == command }) {
                suggestionList.firstOrNull { it.text == command }?.let {
                    reorderedSuggestions.add(it)
                }
            }
        }
        return reorderedSuggestions
    }

    return suggestionList.toMutableList() // No reorder exists, return as-is
}

fun getPrimarySuggestionsList(commands:  Map<String, Class<out BaseCommand>>, aliasList: MutableList<Alias>): MutableList<Suggestion> {
    val all: MutableList<Suggestion> = mutableListOf()
    commands.forEach {
        all.add(Suggestion(it.key, 0, false, false))
    }
    aliasList.forEach {
        all.add(Suggestion(it.key, 0, false, false))
    }

    return all
}


fun getAvailableCommands(activity: Activity): Map<String,  Class<out BaseCommand>> {
    if (isPro(activity)) {
        return mapOf(
            "launch" to com.coderGtm.yantra.commands.launch.Command::class.java,
            "help" to com.coderGtm.yantra.commands.help.Command::class.java,
            "community" to com.coderGtm.yantra.commands.community.Command::class.java,
            "theme" to com.coderGtm.yantra.commands.theme.Command::class.java,
            "call" to com.coderGtm.yantra.commands.call.Command::class.java,
            "bluetooth" to com.coderGtm.yantra.commands.bluetooth.Command::class.java,
            "flash" to com.coderGtm.yantra.commands.flash.Command::class.java,
            "internet" to com.coderGtm.yantra.commands.internet.Command::class.java,
            "ai" to com.coderGtm.yantra.commands.ai.Command::class.java,
            "todo" to com.coderGtm.yantra.commands.todo.Command::class.java,
            "notepad" to com.coderGtm.yantra.commands.notepad.Command::class.java,
            "alias" to com.coderGtm.yantra.commands.alias.Command::class.java,
            "weather" to com.coderGtm.yantra.commands.weather.Command::class.java,
            "username" to com.coderGtm.yantra.commands.username.Command::class.java,
            "pwd" to com.coderGtm.yantra.commands.pwd.Command::class.java,
            "cd" to com.coderGtm.yantra.commands.cd.Command::class.java,
            "ls" to com.coderGtm.yantra.commands.ls.Command::class.java,
            "open" to com.coderGtm.yantra.commands.open.Command::class.java,
            "search" to com.coderGtm.yantra.commands.search.Command::class.java,
            "web" to com.coderGtm.yantra.commands.web.Command::class.java,
            "gupt" to com.coderGtm.yantra.commands.gupt.Command::class.java,
            "tts" to com.coderGtm.yantra.commands.tts.Command::class.java,
            "sfx" to com.coderGtm.yantra.commands.sfx.Command::class.java,
            "news" to com.coderGtm.yantra.commands.news.Command::class.java,
            "bored" to com.coderGtm.yantra.commands.bored.Command::class.java,
            "time" to com.coderGtm.yantra.commands.time.Command::class.java,
            "alarm" to com.coderGtm.yantra.commands.alarm.Command::class.java,
            "timer" to com.coderGtm.yantra.commands.timer.Command::class.java,
            "settings" to com.coderGtm.yantra.commands.settings.Command::class.java,
            "sysinfo" to com.coderGtm.yantra.commands.sysinfo.Command::class.java,
            "setclr" to com.coderGtm.yantra.commands.setclr.Command::class.java,
            "screentime" to com.coderGtm.yantra.commands.screentime.Command::class.java,
            "scripts" to com.coderGtm.yantra.commands.scripts.Command::class.java,
            "quote" to com.coderGtm.yantra.commands.quote.Command::class.java,
            "bg" to com.coderGtm.yantra.commands.bg.Command::class.java,
            "text" to com.coderGtm.yantra.commands.text.Command::class.java,
            "translate" to com.coderGtm.yantra.commands.translate.Command::class.java,
            "music" to com.coderGtm.yantra.commands.music.Command::class.java,
            "echo" to com.coderGtm.yantra.commands.echo.Command::class.java,
            "speedtest" to com.coderGtm.yantra.commands.speedtest.Command::class.java,
            "notify" to com.coderGtm.yantra.commands.notify.Command::class.java,
            "calc" to com.coderGtm.yantra.commands.calc.Command::class.java,
            "email" to com.coderGtm.yantra.commands.email.Command::class.java,
            "sleep" to com.coderGtm.yantra.commands.sleep.Command::class.java,
            "vibe" to com.coderGtm.yantra.commands.vibe.Command::class.java,
            "init" to com.coderGtm.yantra.commands.init.Command::class.java,
            "launchf" to com.coderGtm.yantra.commands.launchf.Command::class.java,
            "info" to com.coderGtm.yantra.commands.info.Command::class.java,
            "infof" to com.coderGtm.yantra.commands.infof.Command::class.java,
            "uninstall" to com.coderGtm.yantra.commands.uninstall.Command::class.java,
            "list" to com.coderGtm.yantra.commands.list.Command::class.java,
            "unalias" to com.coderGtm.yantra.commands.unalias.Command::class.java,
            "termux" to com.coderGtm.yantra.commands.termux.Command::class.java,
            "run" to com.coderGtm.yantra.commands.run.Command::class.java,
            "backup" to com.coderGtm.yantra.commands.backup.Command::class.java,
            "dict" to com.coderGtm.yantra.commands.dict.Command::class.java,
            "history" to com.coderGtm.yantra.commands.history.Command::class.java,
            "battery" to com.coderGtm.yantra.commands.battery.Command::class.java,
            "lock" to com.coderGtm.yantra.commands.lock.Command::class.java,
            "clear" to com.coderGtm.yantra.commands.clear.Command::class.java,
            "reset" to com.coderGtm.yantra.commands.reset.Command::class.java,
            "cmdrequest" to com.coderGtm.yantra.commands.cmdrequest.Command::class.java,
            "feedback" to com.coderGtm.yantra.commands.feedback.Command::class.java,
            "support" to com.coderGtm.yantra.commands.support.Command::class.java,
            "exit" to com.coderGtm.yantra.commands.exit.Command::class.java,
        )
    }
    else {
        return mapOf(
            "launch" to com.coderGtm.yantra.commands.launch.Command::class.java,
            "help" to com.coderGtm.yantra.commands.help.Command::class.java,
            "community" to com.coderGtm.yantra.commands.community.Command::class.java,
            "call" to com.coderGtm.yantra.commands.call.Command::class.java,
            "bluetooth" to com.coderGtm.yantra.commands.bluetooth.Command::class.java,
            "flash" to com.coderGtm.yantra.commands.flash.Command::class.java,
            "alias" to com.coderGtm.yantra.commands.alias.Command::class.java,
            "weather" to com.coderGtm.yantra.commands.weather.Command::class.java,
            "search" to com.coderGtm.yantra.commands.search.Command::class.java,
            "username" to com.coderGtm.yantra.commands.username.Command::class.java,
            "time" to com.coderGtm.yantra.commands.time.Command::class.java,
            "settings" to com.coderGtm.yantra.commands.settings.Command::class.java,
            "sysinfo" to com.coderGtm.yantra.commands.sysinfo.Command::class.java,
            "pro" to com.coderGtm.yantra.commands.pro.Command::class.java,
            "quote" to com.coderGtm.yantra.commands.quote.Command::class.java,
            "text" to com.coderGtm.yantra.commands.text.Command::class.java,
            "tts" to com.coderGtm.yantra.commands.tts.Command::class.java,
            "info" to com.coderGtm.yantra.commands.info.Command::class.java,
            "uninstall" to com.coderGtm.yantra.commands.uninstall.Command::class.java,
            "list" to com.coderGtm.yantra.commands.list.Command::class.java,
            "music" to com.coderGtm.yantra.commands.music.Command::class.java,
            "backup" to com.coderGtm.yantra.commands.backup.Command::class.java,
            "unalias" to com.coderGtm.yantra.commands.unalias.Command::class.java,
            "history" to com.coderGtm.yantra.commands.history.Command::class.java,
            "lock" to com.coderGtm.yantra.commands.lock.Command::class.java,
            "clear" to com.coderGtm.yantra.commands.clear.Command::class.java,
            "reset" to com.coderGtm.yantra.commands.reset.Command::class.java,
            "cmdrequest" to com.coderGtm.yantra.commands.cmdrequest.Command::class.java,
            "feedback" to com.coderGtm.yantra.commands.feedback.Command::class.java,
            "support" to com.coderGtm.yantra.commands.support.Command::class.java,
            "exit" to com.coderGtm.yantra.commands.exit.Command::class.java,
        )
    }
}