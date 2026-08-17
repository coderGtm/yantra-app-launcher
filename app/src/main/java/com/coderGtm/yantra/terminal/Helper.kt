package com.coderGtm.yantra.terminal

import android.app.Activity
import android.content.SharedPreferences
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.loadPrimarySuggestionsOrder
import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.Suggestion

fun showSuggestions(
    rawInput: String,
    getPrimarySuggestions: Boolean,
    getSecondarySuggestions: Boolean,
    terminal: Terminal,
) {
    terminal.scheduleSuggestions(
        rawInput = rawInput,
        primaryEnabled = getPrimarySuggestions,
        secondaryEnabled = getSecondarySuggestions,
    )
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
        all.add(Suggestion(it.key, false))
    }
    aliasList.forEach {
        all.add(Suggestion(it.key, false))
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
            "location" to com.coderGtm.yantra.commands.location.Command::class.java,
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
            "location" to com.coderGtm.yantra.commands.location.Command::class.java,
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