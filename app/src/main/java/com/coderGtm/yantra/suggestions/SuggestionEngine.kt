package com.coderGtm.yantra.suggestions

data class CompletionEdit(
    val start: Int,
    val end: Int,
    val replacement: String,
    val cursor: Int,
)

data class CompletionResult(
    val displayText: String,
    val edit: CompletionEdit,
    val isPrimary: Boolean,
    val allowAutoExecute: Boolean,
    val commandName: String?,
)

class SuggestionEngine(
    private val specs: Map<String, CommandCompletionSpec>,
) {
    fun complete(
        input: CompletionInput,
        commands: Set<String>,
        aliases: Map<String, String>,
        sources: SuggestionSources,
        primarySuggestionsEnabled: Boolean,
        secondarySuggestionsEnabled: Boolean,
        orderedPrimarySuggestions: List<String>? = null,
    ): List<CompletionResult> {
        val tokenized = tokenize(input)
        val results = mutableListOf<CompletionResult>()

        if (tokenized.tokens.isEmpty()) {
            return results
        }

        val firstToken = tokenized.tokens.first()
        val isSingleToken = tokenized.tokens.size == 1 && !tokenized.hasTrailingWhitespace

        // Primary: the user is completing the command name itself.
        if (isSingleToken) {
            if (primarySuggestionsEnabled) {
                val raw = firstToken.text
                // When an ordered primary suggestion list is provided (already filtered and
                // ordered per the user's settings), honor its order exactly. Otherwise fall
                // back to the commands + aliases sets.
                val candidates = orderedPrimarySuggestions ?: (commands + aliases.keys).toList()
                // Exact match on a fully-typed command is omitted (mirrors the old
                // skip-when-equal behavior): type "run" and "run" is not re-suggested.
                val names = candidates
                    .filter { it.length > raw.length && it.startsWith(raw, ignoreCase = true) }
                    .distinct()
                names.forEach { name ->
                    results.add(
                        CompletionResult(
                            displayText = name,
                            edit = CompletionEdit(
                                start = firstToken.start,
                                end = firstToken.end,
                                replacement = "$name ",
                                cursor = firstToken.start + name.length + 1,
                            ),
                            isPrimary = true,
                            allowAutoExecute = false,
                            commandName = if (name in commands) name else aliases[name],
                        )
                    )
                }
            }
            return results
        }

        // Secondary: the user has typed a command plus at least one argument.
        if (!secondarySuggestionsEnabled) {
            return results
        }

        val effectiveCommand = aliases[firstToken.text] ?: firstToken.text
        val spec = specs[effectiveCommand] ?: return results

        val argumentTokens = tokenized.tokens.drop(1)
        val activeToken = if (tokenized.hasTrailingWhitespace) null else argumentTokens.lastOrNull()
        val activeArgumentIndex = if (tokenized.hasTrailingWhitespace) argumentTokens.size else argumentTokens.size - 1
        val context = CompletionContext(
            commandName = effectiveCommand,
            arguments = argumentTokens.map { it.text },
            activeArgumentIndex = activeArgumentIndex,
            rawInput = input.beforeCursor,
            hasTrailingWhitespace = tokenized.hasTrailingWhitespace,
        )

        // --- Rule selection: walk rules left to right, consuming argument positions ---
        // A Choice consumes its slot ONLY when the token at that slot exactly matches one
        // of its options (a completed flag). When the slot is the in-progress token, the
        // Choice claims the position only if the partial (possibly empty) prefix-matches an
        // option; otherwise it falls through WITHOUT consuming, so a following value rule
        // can claim the position. This is what lets `launch Google M` complete as
        // `launch Google Maps ` (the whole remainder replaced) instead of doubling `Google`.
        var consumed = 0
        var activeRule: CompletionRule? = null
        for (rule in spec.rules) {
            when (rule) {
                is CompletionRule.Choice -> {
                    val options = rule.options(context)
                    val tokenAtSlot = context.arguments.getOrNull(consumed)
                    val slotIsActive = (consumed == context.activeArgumentIndex)
                    if (slotIsActive) {
                        val prefixMatches = tokenAtSlot == null ||
                            (tokenAtSlot != null && options.any { it.startsWith(tokenAtSlot, ignoreCase = true) })
                        if (prefixMatches) {
                            activeRule = rule
                            break
                        }
                        // Active token does not prefix-match any option: fall through,
                        // do not consume this position.
                    } else {
                        val consumedExact = tokenAtSlot != null &&
                            options.any { it.equals(tokenAtSlot, ignoreCase = true) }
                        if (consumedExact) {
                            consumed += 1
                            continue
                        }
                        // Token at this slot does not match any option: fall through,
                        // do not consume this position.
                    }
                }
                is CompletionRule.Remainder -> {
                    if (context.activeArgumentIndex >= consumed) {
                        activeRule = rule
                        break
                    }
                }
                is CompletionRule.DelimitedValue -> {
                    val delimiterIndex = context.arguments.indices.firstOrNull { rule.delimiter(context.arguments[it]) }
                        ?: Int.MAX_VALUE
                    if (context.activeArgumentIndex < delimiterIndex) {
                        activeRule = rule
                        break
                    }
                    consumed = delimiterIndex
                }
                is CompletionRule.RepeatChoice -> {
                    if (context.activeArgumentIndex >= consumed) {
                        activeRule = rule
                        break
                    }
                }
                is CompletionRule.None -> {
                    if (context.activeArgumentIndex >= consumed) {
                        activeRule = rule
                        break
                    }
                }
            }
        }
        val rule = activeRule ?: return results

        // Start offset of the first unconsumed argument token (for value spans).
        val valueStart = if (consumed < argumentTokens.size) {
            argumentTokens[consumed].start
        } else {
            input.beforeCursor.length
        }

        when (rule) {
            is CompletionRule.Choice -> {
                val options = rule.options(context)
                val partial = activeToken?.text ?: ""
                val matches = matchDiscrete(options, partial)
                val spanStart = activeToken?.start ?: input.beforeCursor.length
                val spanEnd = activeToken?.end ?: spanStart
                matches.forEach { cand ->
                    results.add(
                        CompletionResult(
                            displayText = cand,
                            edit = CompletionEdit(
                                start = spanStart,
                                end = spanEnd,
                                replacement = "$cand ",
                                cursor = spanStart + cand.length + 1,
                            ),
                            isPrimary = false,
                            allowAutoExecute = spec.autoExecuteAllowed,
                            commandName = effectiveCommand,
                        )
                    )
                }
            }
            is CompletionRule.Remainder -> {
                val partial = input.beforeCursor.substring(valueStart)
                val matches = matchValue(
                    sources.candidates(rule.source, context.copy(consumedArgumentCount = consumed)),
                    partial,
                )
                matches.forEach { cand ->
                    results.add(
                        CompletionResult(
                            displayText = cand.displayText,
                            edit = CompletionEdit(
                                start = valueStart,
                                end = input.beforeCursor.length,
                                replacement = cand.replacementText + " ",
                                cursor = valueStart + cand.replacementText.length + 1,
                            ),
                            isPrimary = false,
                            allowAutoExecute = spec.autoExecuteAllowed,
                            commandName = effectiveCommand,
                        )
                    )
                }
            }
            is CompletionRule.DelimitedValue -> {
                val partial = input.beforeCursor.substring(valueStart)
                val matches = matchValue(
                    sources.candidates(rule.source, context.copy(consumedArgumentCount = consumed)),
                    partial,
                )
                matches.forEach { cand ->
                    results.add(
                        CompletionResult(
                            displayText = cand.displayText,
                            edit = CompletionEdit(
                                start = valueStart,
                                end = input.beforeCursor.length,
                                replacement = cand.replacementText + " ",
                                cursor = valueStart + cand.replacementText.length + 1,
                            ),
                            isPrimary = false,
                            allowAutoExecute = spec.autoExecuteAllowed,
                            commandName = effectiveCommand,
                        )
                    )
                }
            }
            is CompletionRule.RepeatChoice -> {
                val options = rule.options(context)
                val partial = activeToken?.text ?: ""
                val matches = matchDiscrete(options, partial)
                val spanStart = activeToken?.start ?: input.beforeCursor.length
                val spanEnd = activeToken?.end ?: spanStart
                matches.forEach { cand ->
                    results.add(
                        CompletionResult(
                            displayText = cand,
                            edit = CompletionEdit(
                                start = spanStart,
                                end = spanEnd,
                                replacement = "$cand ",
                                cursor = spanStart + cand.length + 1,
                            ),
                            isPrimary = false,
                            allowAutoExecute = spec.autoExecuteAllowed,
                            commandName = effectiveCommand,
                        )
                    )
                }
            }
            is CompletionRule.None -> {
                return results
            }
        }

        return results
    }

    private fun matchDiscrete(options: List<String>, partial: String): List<String> {
        val lowerPartial = partial.lowercase()
        if (lowerPartial.isEmpty()) return options.distinct()
        val prefix = mutableListOf<String>()
        val substring = mutableListOf<String>()
        options.distinct().forEach { opt ->
            val lower = opt.lowercase()
            when {
                // An option exactly equal to the non-empty partial is omitted: the
                // token is already complete. Mirrors the old skip-when-equal behavior.
                lower == lowerPartial -> {}
                lower.startsWith(lowerPartial) -> prefix.add(opt)
                lower.contains(lowerPartial) -> substring.add(opt)
            }
        }
        return prefix + substring
    }

    private fun matchValue(candidates: List<CompletionCandidate>, partial: String): List<CompletionCandidate> {
        val lowerPartial = partial.trim().lowercase()
        if (lowerPartial.isEmpty()) return candidates
        val prefix = mutableListOf<CompletionCandidate>()
        val substring = mutableListOf<CompletionCandidate>()
        candidates.forEach { cand ->
            val lower = cand.displayText.lowercase()
            when {
                lower == lowerPartial -> {}
                lower.startsWith(lowerPartial) -> prefix.add(cand)
                lower.contains(lowerPartial) -> substring.add(cand)
            }
        }
        return (prefix + substring).distinctBy { it.displayText }
    }
}