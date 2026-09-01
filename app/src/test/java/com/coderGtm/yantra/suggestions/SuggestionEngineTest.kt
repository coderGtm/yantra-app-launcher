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

    @Test
    fun `primary command middle-letter match`() {
        val results = complete("nch")
        assertTrue(results.any { it.displayText == "launch" })
    }

    @Test
    fun `autocapitalized command gets secondary suggestions`() {
        val results = complete("Launch G")
        assertTrue(results.any { it.displayText == "Gmail" })
    }

    @Test
    fun `autocapitalized command via alias gets secondary suggestions`() {
        val results = engine.complete(
            input = CompletionInput(rawText = "RUN ba", cursor = 7),
            commands = setOf("run"),
            aliases = emptyMap(),
            sources = fakeSources,
            primarySuggestionsEnabled = true,
            secondarySuggestionsEnabled = true,
        )
        assertTrue(results.any { it.displayText == "backup" })
    }
}

class WeatherCompletionTest {

    private fun weatherEngine() = SuggestionEngine(
        mapOf(
            "weather" to CommandCompletionSpec(
                rules = listOf(
                    CompletionRule.DelimitedValue(
                        source = CandidateSource.LOCATIONS,
                        delimiter = { it.startsWith("-") },
                    ),
                    CompletionRule.RepeatChoice { listOf("-temp", "-humidity") },
                ),
                autoExecuteAllowed = false,
            ),
        )
    )

    private val weatherSources = object : SuggestionSources {
        override fun candidates(source: CandidateSource, context: CompletionContext): List<CompletionCandidate> =
            when (source) {
                CandidateSource.LOCATIONS -> listOf("New York", "Paris").map { CompletionCandidate(it) }
                else -> emptyList()
            }
    }

    private fun completeWeather(raw: String) = weatherEngine().complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = setOf("weather"),
        aliases = emptyMap(),
        sources = weatherSources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    @Test
    fun `weather location completion`() {
        val results = completeWeather("weather New Y")
        assertTrue(results.any { it.displayText.contains("New York") })
    }

    @Test
    fun `weather field completion after location`() {
        val results = completeWeather("weather New York -")
        assertTrue(results.any { it.displayText == "-temp" })
    }

    @Test
    fun `weather repeated field completion`() {
        val results = completeWeather("weather New York -temp -")
        assertTrue(results.any { it.displayText == "-humidity" })
    }

    @Test
    fun `weather location completion stops once a field is entered`() {
        val results = completeWeather("weather New York -")
        assertTrue(results.none { it.displayText == "New York" })
    }

    @Test
    fun `weather does not re-suggest an already used field`() {
        val results = completeWeather("weather New York -temp -")
        val texts = results.map { it.displayText }
        assertTrue(texts.contains("-humidity"))
        assertTrue(texts.none { it == "-temp" })
    }
}

class EngineEdgeCaseTest {

    private val edgeSpecs = mapOf(
        "run" to CommandCompletionSpec(
            rules = listOf(
                CompletionRule.Choice { listOf("-lua", "-clean") },
                CompletionRule.Remainder(CandidateSource.SCRIPTS),
            ),
        ),
        "scripts" to CommandCompletionSpec(
            rules = listOf(
                CompletionRule.Choice { listOf("-new", "-rm") },
                CompletionRule.Remainder(CandidateSource.SCRIPTS),
            ),
            autoExecuteAllowed = false,
        ),
        "launch" to CommandCompletionSpec(
            rules = listOf(
                CompletionRule.Choice { listOf("-s", "-p") },
                CompletionRule.Remainder(CandidateSource.APPS),
            ),
        ),
    )

    private val edgeSources = object : SuggestionSources {
        override fun candidates(source: CandidateSource, context: CompletionContext): List<CompletionCandidate> {
            val preceding = context.precedingConsumedArgument
            val list = when (source) {
                CandidateSource.SCRIPTS -> if (preceding == "-new") emptyList() else listOf("backup", "cleanup")
                CandidateSource.APPS -> when (preceding) {
                    "-s" -> listOf("My Shortcut")
                    "-p" -> listOf("com.google.maps")
                    else -> listOf("Google Maps", "Goat", "Google", "Gmail", "Mango")
                }
                else -> emptyList()
            }
            return list.map { CompletionCandidate(it) }
        }
    }

    private fun completeEdge(
        engine: SuggestionEngine,
        raw: String,
    ) = engine.complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = edgeSpecs.keys,
        aliases = emptyMap(),
        sources = edgeSources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    private fun completeScripts(raw: String) = completeEdge(SuggestionEngine(edgeSpecs), raw)
    private fun completeRun(raw: String) = completeEdge(SuggestionEngine(edgeSpecs), raw)
    private fun completeLaunch(raw: String) = completeEdge(SuggestionEngine(edgeSpecs), raw)

    private fun complete(raw: String) = completeEdge(SuggestionEngine(edgeSpecs), raw)

    @Test
    fun `alias completion uses effective command`() {
        val engine = SuggestionEngine(edgeSpecs)
        val results = engine.complete(
            input = CompletionInput(rawText = "goo -", cursor = 5),
            commands = setOf("run"),
            aliases = mapOf("goo" to "run"),
            sources = edgeSources,
            primarySuggestionsEnabled = true,
            secondarySuggestionsEnabled = true,
        )
        assertTrue(results.any { it.displayText == "-lua" })
    }

    @Test
    fun `scripts rm suggests scripts`() {
        val results = completeScripts("scripts -rm ba")
        assertEquals(listOf("backup"), results.map { it.displayText })
    }

    @Test
    fun `scripts new suggests nothing`() {
        val results = completeScripts("scripts -new x")
        assertEquals(0, results.size)
    }

    @Test
    fun `run clean suggests scripts`() {
        val results = completeRun("run -clean ba")
        assertEquals(listOf("backup"), results.map { it.displayText })
    }

    @Test
    fun `run without flag suggests scripts`() {
        // The Choice rule falls through (no "-lua"/"-clean" prefix), so the
        // Remainder(SCRIPTS) rule claims position 0.
        val results = completeRun("run ba")
        assertEquals(listOf("backup"), results.map { it.displayText })
    }

    @Test
    fun `launch flag selects package names`() {
        val results = completeLaunch("launch -p com.google.ma")
        assertEquals(listOf("com.google.maps"), results.map { it.displayText })
    }

    @Test
    fun `launch flag selects shortcuts`() {
        // edgeSources maps APPS to SHORTCUTS when preceding consumed argument is "-s".
        val results = completeLaunch("launch -s My S")
        assertTrue(results.any { it.displayText.contains("My Shortcut") })
    }

    @Test
    fun `multiple internal spaces handled`() {
        val results = complete("run  -")
        assertTrue(results.any { it.displayText == "-lua" })
    }

    @Test
    fun `trailing space suggests all choices`() {
        val results = complete("run ")
        assertTrue(results.map { it.displayText }.containsAll(listOf("-lua", "-clean")))
    }

    @Test
    fun `partial choice completion`() {
        val results = complete("run -")
        assertTrue(results.map { it.displayText }.containsAll(listOf("-lua", "-clean")))
    }

    @Test
    fun `exact primary command not re-suggested`() {
        val results = complete("run")
        assertEquals(0, results.size)
    }

    @Test
    fun `candidate ordering prefix before substring`() {
        // APPS source order: [Google Maps, Goat, Google, Gmail, Mango]
        // Partial "go": prefix matches keep source order (Google Maps, Goat, Google);
        // substring match (Mango contains "go") comes after all prefix matches.
        val results = completeLaunch("launch go")
        assertEquals(listOf("Google Maps", "Goat", "Google", "Mango"), results.map { it.displayText })
    }

    @Test
    fun `unknown command no secondary suggestions`() {
        val results = complete("zzz x")
        assertEquals(0, results.size)
    }

    @Test
    fun `run choice matches from middle letters`() {
        // Old behavior: typing "run lua" (middle letters of "-lua") suggested "-lua".
        val results = completeRun("run lua")
        assertTrue(results.any { it.displayText == "-lua" })
    }

    @Test
    fun `launch value matches from middle letters`() {
        val results = completeLaunch("launch oog")
        assertTrue(results.any { it.displayText.contains("Google Maps") })
    }
}

class MergedChoiceAndValueTest {

    private val specs = mapOf(
        "launch" to CommandCompletionSpec(
            rules = listOf(
                CompletionRule.Choice { listOf("-s", "-p") },
                CompletionRule.Remainder(CandidateSource.APPS),
            ),
        ),
        "run" to CommandCompletionSpec(
            rules = listOf(
                CompletionRule.Choice { listOf("-lua", "-clean") },
                CompletionRule.Remainder(CandidateSource.SCRIPTS),
            ),
        ),
    )

    private val sources = object : SuggestionSources {
        override fun candidates(source: CandidateSource, context: CompletionContext): List<CompletionCandidate> =
            when (source) {
                CandidateSource.APPS -> listOf("Signal", "Slack", "Google Maps").map { CompletionCandidate(it) }
                CandidateSource.SCRIPTS -> listOf("luatest", "cleanup").map { CompletionCandidate(it) }
                else -> emptyList()
            }
    }

    private fun complete(raw: String) = SuggestionEngine(specs).complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = specs.keys,
        aliases = emptyMap(),
        sources = sources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    @Test
    fun `launch trailing space shows flags and apps`() {
        val results = complete("launch ")
        val texts = results.map { it.displayText }
        assertTrue(texts.containsAll(listOf("-s", "-p", "Signal", "Slack", "Google Maps")))
    }

    @Test
    fun `launch partial letter shows flag and matching apps`() {
        val results = complete("launch s")
        val texts = results.map { it.displayText }
        assertTrue(texts.contains("-s"))
        assertTrue(texts.contains("Signal"))
        assertTrue(texts.contains("Slack"))
    }

    @Test
    fun `run trailing space shows flags and scripts`() {
        val results = complete("run ")
        val texts = results.map { it.displayText }
        assertTrue(texts.containsAll(listOf("-lua", "-clean", "luatest", "cleanup")))
    }

    @Test
    fun `run partial shows flag and matching scripts`() {
        val results = complete("run lua")
        val texts = results.map { it.displayText }
        assertTrue(texts.contains("-lua"))
        assertTrue(texts.contains("luatest"))
    }

    @Test
    fun `merge does not apply after a flag is consumed`() {
        val results = complete("launch -s ")
        val texts = results.map { it.displayText }
        assertTrue(texts.none { it == "-s" || it == "-p" })
    }
}

class PreMatchedCandidateTest {

    @Test
    fun `prematched candidate bypasses substring filter`() {
        val engine = SuggestionEngine(
            mapOf("lfz" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.SCRIPTS))))
        )
        val sources = object : SuggestionSources {
            override fun candidates(source: CandidateSource, context: CompletionContext) =
                when (source) {
                    CandidateSource.SCRIPTS -> listOf(CompletionCandidate("Google Maps", preMatched = true))
                    else -> emptyList()
                }
        }
        val results = engine.complete(
            input = CompletionInput(rawText = "lfz gmaps", cursor = 10),
            commands = setOf("lfz"),
            aliases = emptyMap(),
            sources = sources,
            primarySuggestionsEnabled = true,
            secondarySuggestionsEnabled = true,
        )
        assertTrue(results.any { it.displayText == "Google Maps" })
    }
}

class BestFuzzyMatchTest {

    @Test
    fun `picks best similarity even without substring relation`() {
        // Normalized Levenshtein similarity for "gmaps": gmail 0.6 > google maps 0.45 > gallery 0.14.
        // "gmaps" is a substring of none of them.
        assertEquals("Gmail", bestFuzzyMatch(listOf("Gallery", "Google Maps", "Gmail"), "gmaps"))
    }

    @Test
    fun `empty names or query returns null`() {
        assertNull(bestFuzzyMatch(emptyList(), "gmaps"))
        assertNull(bestFuzzyMatch(listOf("Gallery"), ""))
    }
}

class UnaliasCompletionTest {

    private val engine = SuggestionEngine(
        mapOf(
            "unalias" to CommandCompletionSpec(
                rules = listOf(
                    CompletionRule.Choice { listOf("-1") },
                    CompletionRule.Remainder(CandidateSource.ALIASES),
                ),
            ),
        )
    )

    private val sources = object : SuggestionSources {
        override fun candidates(source: CandidateSource, context: CompletionContext): List<CompletionCandidate> =
            when (source) {
                CandidateSource.ALIASES -> listOf("goo", "tun").map { CompletionCandidate(it) }
                else -> emptyList()
            }
    }

    private fun complete(raw: String) = engine.complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = setOf("unalias"),
        aliases = emptyMap(),
        sources = sources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    @Test
    fun `unalias trailing space shows flag and aliases`() {
        val texts = complete("unalias ").map { it.displayText }
        assertTrue(texts.containsAll(listOf("-1", "goo", "tun")))
    }

    @Test
    fun `unalias partial shows matching aliases`() {
        val texts = complete("unalias go").map { it.displayText }
        assertTrue(texts.contains("goo"))
        assertTrue(texts.none { it == "tun" })
    }

    @Test
    fun `unalias exact flag suggests nothing`() {
        assertEquals(0, complete("unalias -1").size)
    }
}

class ProductionUnaliasSpecTest {

    // Exercises the REAL production spec map: if the unalias entry loses its
    // Remainder(CandidateSource.ALIASES) rule, the flag-then-alias case regresses.
    private val engine = SuggestionEngine(
        buildCommandCompletionSpecs(
            getThemes = { emptyList() },
            getTodoArguments = { emptyList() },
            getWeatherFields = { emptySet() },
        )
    )

    private val sources = object : SuggestionSources {
        override fun candidates(source: CandidateSource, context: CompletionContext): List<CompletionCandidate> =
            when (source) {
                CandidateSource.ALIASES -> listOf("goo", "tun").map { CompletionCandidate(it) }
                else -> emptyList()
            }
    }

    private fun complete(raw: String) = engine.complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = setOf("unalias"),
        aliases = emptyMap(),
        sources = sources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    @Test
    fun `production unalias trailing space shows flag and aliases`() {
        val texts = complete("unalias ").map { it.displayText }
        assertTrue(texts.containsAll(listOf("-1", "goo", "tun")))
    }

    @Test
    fun `production unalias partial shows matching aliases`() {
        val texts = complete("unalias go").map { it.displayText }
        assertTrue(texts.contains("goo"))
        assertTrue(texts.none { it == "tun" })
    }

    @Test
    fun `production unalias flag then alias suggests aliases`() {
        val texts = complete("unalias -1 go").map { it.displayText }
        assertTrue(texts.contains("goo"))
    }
}
