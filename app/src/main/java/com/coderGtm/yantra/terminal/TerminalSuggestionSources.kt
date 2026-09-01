package com.coderGtm.yantra.terminal

import android.app.Activity
import android.content.SharedPreferences
import com.coderGtm.yantra.Croissant
import com.coderGtm.yantra.Themes
import com.coderGtm.yantra.commands.todo.getToDo
import com.coderGtm.yantra.commands.weather.VALID_WEATHER_FIELDS
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.suggestions.CandidateSource
import com.coderGtm.yantra.suggestions.CompletionCandidate
import com.coderGtm.yantra.suggestions.CompletionContext
import com.coderGtm.yantra.suggestions.SuggestionSources
import com.coderGtm.yantra.suggestions.bestFuzzyMatch

class TerminalSuggestionState(
    val appNames: List<String>,
    val packageNames: List<String>,
    val shortcutLabels: List<String>,
    val contactNames: List<String>,
    val commandNames: List<String>,
    val aliasKeys: List<String>,
)

class TerminalSuggestionSources(
    private val terminal: Terminal,
    private val state: TerminalSuggestionState,
) : SuggestionSources {

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
            CandidateSource.APPS -> when (precedingArg) {
                "-s" -> state.shortcutLabels.map { CompletionCandidate(it) }
                "-p" -> state.packageNames.map { CompletionCandidate(it) }
                else -> state.appNames.map { CompletionCandidate(it) }
            }
            CandidateSource.SHORTCUTS -> state.shortcutLabels.map { CompletionCandidate(it) }
            CandidateSource.PACKAGE_NAMES -> state.packageNames.map { CompletionCandidate(it) }
            CandidateSource.ALIASES -> state.aliasKeys.map { CompletionCandidate(it) }
            CandidateSource.FILES -> getFiles(terminal).map { CompletionCandidate(it) }
            CandidateSource.FOLDERS -> getFolders(terminal).map { CompletionCandidate(it) }
            CandidateSource.SCRIPTS -> {
                if (precedingArg == "-new") emptyList()
                else getScripts(terminal.preferenceObject).map { CompletionCandidate(it) }
            }
            CandidateSource.CONTACTS -> state.contactNames.map { CompletionCandidate(it) }
            CandidateSource.SFX -> buildSfxNames(terminal.activity).map { CompletionCandidate(it) }
            CandidateSource.COMMANDS -> state.commandNames.map { CompletionCandidate(it) }
            CandidateSource.THEMES -> terminalPreferenceThemeNames(terminal.preferenceObject).map { CompletionCandidate(it) }
            CandidateSource.LOCATIONS -> emptyList() // no location data source exists
            CandidateSource.WEATHER_FIELDS -> VALID_WEATHER_FIELDS.map { CompletionCandidate("-$it") }
            CandidateSource.TODO_ARGUMENTS -> emptyList() // injected via specs builder
        }
    }

    private fun launchfMatch(context: CompletionContext): List<CompletionCandidate> {
        val query = context.rawInput.trim().split(Regex("\\s+"), limit = 2).getOrNull(1)
            ?.trim()?.lowercase().orEmpty()
        val best = bestFuzzyMatch(state.appNames, query) ?: return emptyList()
        return listOf(CompletionCandidate(best, preMatched = true))
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
    // Croissant's error paths may call terminal.output and throw while parsing the
    // provider response; suggestions must never crash the completion coroutine.
    val files = try {
        Croissant().getListOfObjects(terminal, terminal.workingDir)
    } catch (_: Exception) {
        return emptyList()
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
    // Croissant's error paths may call terminal.output and throw while parsing the
    // provider response; suggestions must never crash the completion coroutine.
    val files = try {
        Croissant().getListOfObjects(terminal, terminal.workingDir)
    } catch (_: Exception) {
        return emptyList()
    }

    val fullList = mutableListOf<String>()

    for (file in files) {
        if (!file.isDirectory) {
            fullList.add(file.name)
        }
    }

    fullList.sort()
    return fullList
}