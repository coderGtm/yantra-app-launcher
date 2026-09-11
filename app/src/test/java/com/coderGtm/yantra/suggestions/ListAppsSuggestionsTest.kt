package com.coderGtm.yantra.suggestions

import org.junit.Assert.assertTrue
import org.junit.Test

class ListAppsSuggestionsTest {
    private val engine = SuggestionEngine(
        buildCommandCompletionSpecs(
            getThemes = { emptyList() },
            getTodoArguments = { emptyList() },
            getWeatherFields = { emptySet() },
            getAppCategories = { listOf("Games", "Social", "Other") },
        )
    )

    private val sources = object : SuggestionSources {
        override fun candidates(
            source: CandidateSource,
            context: CompletionContext
        ): List<CompletionCandidate> = emptyList()
    }

    private fun complete(raw: String) = engine.complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = setOf("list"),
        aliases = emptyMap(),
        sources = sources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    @Test
    fun `list apps trailing space suggests categories`() {
        val texts = complete("list apps ").map { it.displayText }
        assertTrue(texts.containsAll(listOf("Games", "Social", "Other")))
    }

    @Test
    fun `list apps partial filters categories`() {
        val texts = complete("list apps so").map { it.displayText }
        assertTrue(texts.contains("Social"))
        assertTrue(texts.none { it == "Games" })
    }

    @Test
    fun `list themes does not suggest app categories`() {
        val texts = complete("list themes ").map { it.displayText }
        assertTrue(texts.none { it == "Social" })
    }

    @Test
    fun `list apps trailing space suggests dash p`() {
        val texts = complete("list apps ").map { it.displayText }
        assertTrue(texts.contains("-p"))
    }

    @Test
    fun `list apps category trailing space suggests dash p`() {
        val texts = complete("list apps Social ").map { it.displayText }
        assertTrue(texts.contains("-p"))
    }
}
