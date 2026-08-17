package com.coderGtm.yantra.terminal

import android.app.Activity
import android.content.SharedPreferences
import com.coderGtm.yantra.Croissant
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.commands.todo.getToDo
import com.coderGtm.yantra.commands.weather.VALID_WEATHER_FIELDS
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.suggestions.CandidateSource
import com.coderGtm.yantra.suggestions.CompletionCandidate
import com.coderGtm.yantra.suggestions.CompletionContext
import com.coderGtm.yantra.suggestions.SuggestionSources

class TerminalSuggestionSources(private val terminal: Terminal) : SuggestionSources {

    override fun candidates(
        source: CandidateSource,
        context: CompletionContext,
    ): List<CompletionCandidate> {
        // launchf is a special case: it returns a single best fuzzy match over app names.
        if (source == CandidateSource.APPS && context.commandName == "launchf") {
            return launchfMatch(context)
        }
        val precedingArg = context.precedingConsumedArgument
        return when (source) {
            CandidateSource.APPS -> {
                val effective = when (precedingArg) {
                    "-s" -> CandidateSource.SHORTCUTS
                    "-p" -> CandidateSource.PACKAGE_NAMES
                    else -> CandidateSource.APPS
                }
                when (effective) {
                    CandidateSource.SHORTCUTS -> terminal.shortcutList.map { CompletionCandidate(it.label) }
                    CandidateSource.PACKAGE_NAMES -> terminal.appList.map { CompletionCandidate(it.packageName) }
                    else -> terminal.appList.map { CompletionCandidate(it.appName) }
                }
            }
            CandidateSource.SHORTCUTS -> terminal.shortcutList.map { CompletionCandidate(it.label) }
            CandidateSource.PACKAGE_NAMES -> terminal.appList.map { CompletionCandidate(it.packageName) }
            CandidateSource.FILES -> getFiles(terminal).map { CompletionCandidate(it) }
            CandidateSource.FOLDERS -> getFolders(terminal).map { CompletionCandidate(it) }
            CandidateSource.SCRIPTS -> {
                if (precedingArg == "-new") emptyList()
                else getScripts(terminal.preferenceObject).map { CompletionCandidate(it) }
            }
            CandidateSource.CONTACTS -> terminal.contactNames.map { CompletionCandidate(it) }
            CandidateSource.SFX -> terminal.activity.filesDir
                .listFiles()
                .orEmpty()
                .filter { it.isFile && (it.name.endsWith(".mp3") || it.name.endsWith(".wav") || it.name.endsWith(".ogg")) }
                .map { CompletionCandidate(it.name.removeSuffix(".mp3").removeSuffix(".wav").removeSuffix(".ogg")) }
            CandidateSource.COMMANDS -> terminal.commands.keys.map { CompletionCandidate(it) }
            CandidateSource.ALIASES -> terminal.aliasList.map { CompletionCandidate(it.key) }
            CandidateSource.THEMES -> {
                val saved = terminal.preferenceObject.getString("savedThemeList", "")
                    ?.split(",")
                    ?.filter { it.isNotEmpty() }
                    .orEmpty()
                (Themes.entries.map { it.name } + saved).map { CompletionCandidate(it) }
            }
            CandidateSource.WEATHER_FIELDS -> VALID_WEATHER_FIELDS.map { CompletionCandidate("-$it") }
            CandidateSource.LOCATIONS -> emptyList() // no location data source exists
            CandidateSource.TODO_ARGUMENTS -> emptyList() // injected via specs builder
        }
    }

    private fun launchfMatch(context: CompletionContext): List<CompletionCandidate> {
        if (!terminal.appListFetched) return emptyList()
        val activeArg = context.activeArgument
        if (activeArg.isNullOrEmpty()) return emptyList()
        val name = context.rawInput.trim().removePrefix(context.commandName).trim().lowercase()
        if (name.isEmpty()) return emptyList()
        val scores = mutableListOf<Double>()
        for (app in terminal.appList) {
            scores.add(findSimilarity(app.appName.lowercase(), name))
        }
        val maxIndex = scores.indexOf(scores.max())
        return listOf(CompletionCandidate(terminal.appList[maxIndex].appName))
    }
}

internal fun terminalPreferenceThemeNames(preferenceObject: SharedPreferences): List<String> =
    (Themes.entries.map { it.name } +
        (preferenceObject.getString("savedThemeList", "")
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            .orEmpty()))

internal fun buildTodoArguments(preferenceObject: SharedPreferences): List<String> {
    val args = mutableListOf("-p", "-1")
    val todoSize = getToDo(preferenceObject).size
    for (i in 0 until todoSize) args.add(i.toString())
    return args
}

internal fun buildSfxNames(activity: Activity): List<String> =
    activity.filesDir
        .listFiles()
        .orEmpty()
        .filter { it.isFile && (it.name.endsWith(".mp3") || it.name.endsWith(".wav") || it.name.endsWith(".ogg")) }
        .map { it.name.removeSuffix(".mp3").removeSuffix(".wav").removeSuffix(".ogg") }

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