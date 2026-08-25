package com.coderGtm.yantra.plugins

import android.content.SharedPreferences
import com.coderGtm.yantra.terminal.Terminal

/**
 * Small host API for plugins that are contributed through the repository.
 *
 * A plugin should keep its implementation in one folder under `plugins/contrib`
 * and expose one class that implements this interface or a narrower child
 * interface such as [SuggestionPlugin].
 */
interface YantraPlugin {
    val id: String
    val name: String
    val author: String?
        get() = null
    val description: String
    val versionName: String
    val versionCode: Int
    val minHostPluginApiVersion: Int
        get() = 1
    val targetHostPluginApiVersion: Int
        get() = minHostPluginApiVersion
    val capabilities: List<String>
        get() = emptyList()
    val defaultEnabled: Boolean
        get() = true
    val preferenceKey: String
        get() = "plugin.$id.enabled"

    fun isEnabled(preferenceObject: SharedPreferences): Boolean {
        return preferenceObject.getBoolean(preferenceKey, defaultEnabled)
    }
}

data class SuggestionPluginContext(
    val rawInput: String,
    val input: String,
    val args: List<String>,
    val suggestions: List<String>,
    val isPrimary: Boolean,
    val overrideLastWord: Boolean
)

/**
 * Extension point for plugins that react to terminal suggestions.
 *
 * The host owns suggestion generation; plugins can observe that result and add
 * their own UI or behavior without changing the command parser.
 */
interface SuggestionPlugin : YantraPlugin {
    fun onSuggestionsUpdated(terminal: Terminal, context: SuggestionPluginContext)

    fun clear(terminal: Terminal) = Unit
}

data class PluginCompatibility(
    val label: String,
    val detail: String,
    val canEnable: Boolean
)
