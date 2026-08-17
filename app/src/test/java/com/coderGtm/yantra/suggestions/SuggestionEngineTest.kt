package com.coderGtm.yantra.suggestions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenizerTest {

    private fun tokenize(raw: String) =
        tokenize(CompletionInput(rawText = raw, cursor = raw.length))

    @Test
    fun `empty input has no tokens`() {
        val result = tokenize("")
        assertEquals(0, result.tokens.size)
        assertNull(result.activeToken)
        assertEquals(false, result.hasTrailingWhitespace)
    }

    @Test
    fun `single word`() {
        val result = tokenize("run")
        assertEquals(listOf("run"), result.tokens.map { it.text })
        assertEquals(0, result.tokens[0].start)
        assertEquals(3, result.tokens[0].end)
        assertEquals("run", result.activeToken?.text)
        assertEquals(false, result.hasTrailingWhitespace)
    }

    @Test
    fun `word followed by space has trailing whitespace`() {
        val result = tokenize("run ")
        assertEquals(listOf("run"), result.tokens.map { it.text })
        assertNull(result.activeToken)
        assertEquals(true, result.hasTrailingWhitespace)
    }

    @Test
    fun `two words active token is last`() {
        val result = tokenize("run -lua")
        assertEquals(listOf("run", "-lua"), result.tokens.map { it.text })
        assertEquals("-lua", result.activeToken?.text)
        assertEquals(4, result.activeToken?.start)
        assertEquals(8, result.activeToken?.end)
        assertEquals(false, result.hasTrailingWhitespace)
    }

    @Test
    fun `two words trailing space`() {
        val result = tokenize("run -lua ")
        assertEquals(listOf("run", "-lua"), result.tokens.map { it.text })
        assertNull(result.activeToken)
        assertEquals(true, result.hasTrailingWhitespace)
    }

    @Test
    fun `three words`() {
        val result = tokenize("run -lua scr")
        assertEquals(listOf("run", "-lua", "scr"), result.tokens.map { it.text })
        assertEquals("scr", result.activeToken?.text)
        assertEquals(9, result.activeToken?.start)
        assertEquals(12, result.activeToken?.end)
    }

    @Test
    fun `multi word value with spaces`() {
        val result = tokenize("launch Google M")
        assertEquals(listOf("launch", "Google", "M"), result.tokens.map { it.text })
        assertEquals("M", result.activeToken?.text)
    }

    @Test
    fun `repeated spaces preserved as boundaries`() {
        val result = tokenize("launch  Google")
        assertEquals(listOf("launch", "Google"), result.tokens.map { it.text })
        assertEquals(8, result.tokens[1].start)
        assertEquals("Google", result.activeToken?.text)
    }
}
class SuggestionEngineTest {

    private val fakeSpecs = mapOf(
        "run" to CommandCompletionSpec(
            rules = listOf(
                CompletionRule.Choice { listOf("-lua", "-clean") },
                CompletionRule.Remainder(CandidateSource.SCRIPTS),
            ),
        ),
        "launch" to CommandCompletionSpec(
            rules = listOf(
                CompletionRule.Choice { listOf("-s", "-p") },
                CompletionRule.Remainder(CandidateSource.APPS),
            ),
        ),
    )

    private val fakeSources = object : SuggestionSources {
        override fun candidates(source: CandidateSource, context: CompletionContext): List<CompletionCandidate> =
            when (source) {
                CandidateSource.APPS -> listOf("Gmail", "Google Maps", "Gallery").map { CompletionCandidate(it) }
                CandidateSource.SCRIPTS -> listOf("backup", "cleanup").map { CompletionCandidate(it) }
                else -> emptyList()
            }
    }

    private val engine = SuggestionEngine(fakeSpecs)

    private fun complete(raw: String) = engine.complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = setOf("run", "launch"),
        aliases = emptyMap(),
        sources = fakeSources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    @Test
    fun `primary command prefix`() {
        val results = complete("la")
        assertEquals(listOf("launch"), results.map { it.displayText })
        val r = results.first()
        assertEquals(true, r.isPrimary)
        assertEquals(0, r.edit.start)
        assertEquals(2, r.edit.end)
        assertEquals("launch ", r.edit.replacement)
    }

    @Test
    fun `run choice after dash`() {
        val results = complete("run -")
        assertEquals(setOf("-lua", "-clean"), results.map { it.displayText }.toSet())
    }

    @Test
    fun `run remainder after flag`() {
        val results = complete("run -lua ba")
        assertEquals(listOf("backup"), results.map { it.displayText })
        val r = results.first()
        assertEquals(9, r.edit.start)
        assertEquals(11, r.edit.end)
        assertEquals("backup ", r.edit.replacement)
    }

    @Test
    fun `run remainder replaces whole remainder no doubling`() {
        val results = complete("run -lua ba")
        val r = results.first()
        val full = "run -lua ba"
        val applied = full.substring(0, r.edit.start) + r.edit.replacement + full.substring(r.edit.end)
        assertEquals("run -lua backup ", applied)
    }

    @Test
    fun `launch remainder replaces multi word no doubling`() {
        val results = complete("launch Google M")
        val r = results.first { it.displayText == "Google Maps" }
        val full = "launch Google M"
        val applied = full.substring(0, r.edit.start) + r.edit.replacement + full.substring(r.edit.end)
        assertEquals("launch Google Maps ", applied)
    }

    @Test
    fun `no suggestions for unknown command`() {
        val results = complete("unknown -")
        assertEquals(0, results.size)
    }

    @Test
    fun `primary disabled`() {
        val results = engine.complete(
            input = CompletionInput(rawText = "la", cursor = 2),
            commands = setOf("launch"),
            aliases = emptyMap(),
            sources = fakeSources,
            primarySuggestionsEnabled = false,
            secondarySuggestionsEnabled = true,
        )
        assertEquals(0, results.size)
    }

    @Test
    fun `secondary disabled`() {
        val results = engine.complete(
            input = CompletionInput(rawText = "run -", cursor = 5),
            commands = setOf("run"),
            aliases = emptyMap(),
            sources = fakeSources,
            primarySuggestionsEnabled = true,
            secondarySuggestionsEnabled = false,
        )
        assertEquals(0, results.size)
    }

    @Test
    fun `empty input`() {
        val results = complete("")
        assertEquals(0, results.size)
    }
}
