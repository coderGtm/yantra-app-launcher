package com.coderGtm.yantra.terminal

import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.coderGtm.yantra.R
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.commands.todo.getToDo
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.Theme
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.setSystemWallpaper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
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
            val allPrimarySuggestions: MutableSet<String> = terminal.commands.keys.toMutableSet()
            terminal.aliasList.forEach {
                allPrimarySuggestions.add(it.key)
            }
            for (ps in allPrimarySuggestions) {
                if (regex.containsMatchIn(ps)) {
                    suggestions.add(ps)
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
            val reg = input.removePrefix(args[0]).trim()
            if (effectivePrimaryCmd == "launch") {
                if (!terminal.appListFetched) {
                    return@Thread
                }
                if (args.size>1) {
                    //search using regex
                    overrideLastWord = true

                    val regex = Regex(Pattern.quote(reg), RegexOption.IGNORE_CASE)
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
                val listArgs = listOf("apps","themes","contacts")
                for (arg in listArgs) {
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
                val listArgs = listOf("-e=google","-e=duckduckgo","-e=brave","-e=bing","-e=yahoo","-e=ecosia","-e=startpage","-e=qwant","-e=you","-e=playstore","-u=")
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
                val sysInfoArgs= listOf("-os", "-host", "-kernel", "-uptime", "-apps", "-terminal", "-font", "-resolution", "-theme", "-cpu", "-memory")
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
            else if (effectivePrimaryCmd == "run") {
                try {
                    val scripts = getScripts(terminal.preferenceObject)
                    if (args.size>1) {
                        overrideLastWord = true
                        val regex = Regex(Pattern.quote(input.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                        for (sname in scripts) {
                            if (regex.containsMatchIn(sname)) {
                                suggestions.add(sname)
                            }
                        }
                    }
                    else {
                        for (sname in scripts) {
                            if (!suggestions.contains(sname)) {
                                suggestions.add(sname)
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
            val btn = Button(terminal.activity)
            btn.text = sug
            btn.setTextColor(terminal.theme.suggestionTextColor)
            if (terminal.preferenceObject.getBoolean("fontpack___purchased",true)) {
                btn.setTypeface(terminal.typeface, Typeface.BOLD)
            }
            else {
                btn.setTypeface(null, Typeface.BOLD)
            }
            btn.background = Color.TRANSPARENT.toDrawable()
            //set start and end margins
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(20, 0, 20, 0)
            btn.layoutParams = params


            btn.setOnClickListener {
                if (overrideLastWord) {
                    val newCmd = input.substring(0, input.length-args[args.size-1].length) + sug + " "
                    terminal.binding.cmdInput.setText(newCmd)
                }
                else {
                    terminal.binding.cmdInput.setText("$input $sug ")
                }
                terminal.binding.cmdInput.setSelection(terminal.binding.cmdInput.text.length)
                requestCmdInputFocusAndShowKeyboard(terminal.activity, terminal.binding)
                terminal.binding.suggestionsTab.removeView(it)

                val actOnSuggestionTap = terminal.preferenceObject.getBoolean("actOnSuggestionTap", false)
                if (!isPrimary && actOnSuggestionTap && executeOnTapViable) {
                    terminal.handleCommand(terminal.binding.cmdInput.text.toString().trim())
                    terminal.binding.cmdInput.setText("")
                }
            }
            if (isPrimary) {
                btn.setOnLongClickListener {
                    try {
                        val commandClass = terminal.commands[sug]
                        if (commandClass != null) {
                            val cmdMetadata = commandClass.getDeclaredConstructor(Terminal::class.java).newInstance(terminal).metadata
                            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                                .setTitle(cmdMetadata.helpTitle)
                                .setMessage(cmdMetadata.description)
                                .setPositiveButton(terminal.activity.getString(R.string.ok)) { helpDialog, _ ->
                                    helpDialog.dismiss()
                                }
                                    .show()
                        }
                    }
                    catch (e: Exception) {}
                    true
                }
            }
            terminal.activity.runOnUiThread {
                terminal.binding.suggestionsTab.addView(btn)
            }
        }
    }.start()
}

fun getFolders(terminal: Terminal): List<String> {
    val files = File(Environment.getExternalStorageDirectory().absolutePath + terminal.workingDir).listFiles()

    if (files == null) {
        return listOf()
    }

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
    val files = File(Environment.getExternalStorageDirectory().absolutePath + terminal.workingDir).listFiles()

    if (files == null) {
        return listOf()
    }

    val fullList = mutableListOf<String>()

    for (file in files) {
        if (file.isFile) {
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