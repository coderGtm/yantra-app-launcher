package com.coderGtm.yantra.plugins.contrib.autocomplete_yyppsk_v0_1_0

import com.coderGtm.yantra.plugins.SuggestionPluginContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InlineAutocompleteCompletionTest {

    @Test
    fun `buildSuffix completes a partial primary command`() {
        val result = InlineAutocompleteCompletion.buildSuffix(
            context(
                rawInput = "la",
                input = "la",
                args = listOf("la"),
                suggestions = listOf("launch"),
                isPrimary = true,
                overrideLastWord = true
            )
        )

        assertEquals("unch ", result)
    }

    @Test
    fun `buildSuffix completes the current secondary argument`() {
        val result = InlineAutocompleteCompletion.buildSuffix(
            context(
                rawInput = "launch -",
                input = "launch -",
                args = listOf("launch", "-"),
                suggestions = listOf("-p"),
                isPrimary = false,
                overrideLastWord = true
            )
        )

        assertEquals("p ", result)
    }

    @Test
    fun `buildSuffix appends a secondary suggestion after a completed command`() {
        val result = InlineAutocompleteCompletion.buildSuffix(
            context(
                rawInput = "launch",
                input = "launch",
                args = listOf("launch"),
                suggestions = listOf("-p"),
                isPrimary = false,
                overrideLastWord = false
            )
        )

        assertEquals(" -p ", result)
    }

    @Test
    fun `buildSuffix skips suggestions that already match current input`() {
        val result = InlineAutocompleteCompletion.buildSuffix(
            context(
                rawInput = "launch",
                input = "launch",
                args = listOf("launch"),
                suggestions = listOf("launch"),
                isPrimary = true,
                overrideLastWord = true
            )
        )

        assertNull(result)
    }

    @Test
    fun `buildSuffix returns null when completion does not continue raw input`() {
        val result = InlineAutocompleteCompletion.buildSuffix(
            context(
                rawInput = "foo",
                input = "bar",
                args = listOf("bar"),
                suggestions = listOf("baz"),
                isPrimary = true,
                overrideLastWord = true
            )
        )

        assertNull(result)
    }

    @Test
    fun `buildSuffix returns null for blank input or empty suggestions`() {
        assertNull(
            InlineAutocompleteCompletion.buildSuffix(
                context(rawInput = " ", input = "", args = emptyList(), suggestions = listOf("launch"))
            )
        )
        assertNull(
            InlineAutocompleteCompletion.buildSuffix(
                context(rawInput = "la", input = "la", args = listOf("la"), suggestions = emptyList())
            )
        )
    }

    private fun context(
        rawInput: String,
        input: String,
        args: List<String>,
        suggestions: List<String>,
        isPrimary: Boolean = true,
        overrideLastWord: Boolean = true
    ): SuggestionPluginContext {
        return SuggestionPluginContext(
            rawInput = rawInput,
            input = input,
            args = args,
            suggestions = suggestions,
            isPrimary = isPrimary,
            overrideLastWord = overrideLastWord
        )
    }
}
