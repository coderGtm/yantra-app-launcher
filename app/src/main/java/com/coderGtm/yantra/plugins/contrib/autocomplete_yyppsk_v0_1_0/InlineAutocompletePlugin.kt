package com.coderGtm.yantra.plugins.contrib.autocomplete_yyppsk_v0_1_0

import com.coderGtm.yantra.plugins.SuggestionPlugin
import com.coderGtm.yantra.plugins.SuggestionPluginContext
import com.coderGtm.yantra.terminal.Terminal

class InlineAutocompletePlugin : SuggestionPlugin {
    override val id = ID
    override val name = "Inline Autocomplete"
    override val author = "yyppsk"
    override val description = "Shows a faded inline completion after the command input."
    override val versionName = "0.1.0"
    override val versionCode = 1
    override val minHostPluginApiVersion = 1
    override val targetHostPluginApiVersion = 1
    override val capabilities = listOf("Suggestions", "Input UI")
    override val preferenceKey = PREFERENCE_KEY

    override fun onSuggestionsUpdated(terminal: Terminal, context: SuggestionPluginContext) {
        val suffix = InlineAutocompleteCompletion.buildSuffix(context)
        terminal.setInlineCompletion(suffix)
    }

    override fun clear(terminal: Terminal) {
        terminal.setInlineCompletion(null)
    }

    companion object {
        const val ID = "inline_autocomplete"
        const val PREFERENCE_KEY = "plugin.inline_autocomplete.enabled"
    }
}

internal object InlineAutocompleteCompletion {
    fun buildSuffix(context: SuggestionPluginContext): String? {
        if (context.rawInput.isBlank() || context.suggestions.isEmpty()) {
            return null
        }

        val suggestion = context.suggestions.firstOrNull {
            !isCurrentInput(context, it)
        } ?: return null

        val completedInput = if (context.overrideLastWord) {
            val lastArg = context.args.lastOrNull().orEmpty()
            context.input.substring(0, context.input.length - lastArg.length) + suggestion + " "
        } else {
            context.input + " " + suggestion + " "
        }

        if (completedInput.length <= context.rawInput.length) {
            return null
        }

        if (!completedInput.startsWith(context.rawInput, ignoreCase = true)) {
            return null
        }

        return completedInput.substring(context.rawInput.length)
    }

    private fun isCurrentInput(context: SuggestionPluginContext, suggestion: String): Boolean {
        return if (context.isPrimary) {
            context.input.trim().equals(suggestion.trim(), ignoreCase = true)
        } else {
            val secondaryInput = context.input.removePrefix(context.args.firstOrNull().orEmpty()).trim()
            secondaryInput.equals(suggestion.trim(), ignoreCase = true)
        }
    }
}
