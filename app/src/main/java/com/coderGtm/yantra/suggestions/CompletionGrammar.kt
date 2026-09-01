package com.coderGtm.yantra.suggestions

data class CompletionContext(
    val commandName: String,
    val arguments: List<String>,
    val activeArgumentIndex: Int,
    val rawInput: String,
    val hasTrailingWhitespace: Boolean,
    val consumedArgumentCount: Int = 0,
) {
    /**
     * The number of argument positions consumed by rules before the active rule.
     * The engine fills this in via `copy(consumedArgumentCount = consumed)` when it
     * calls a source adapter. Used by adapters to inspect the preceding flag
     * (e.g. `launch -s` vs `launch -p`).
     */
    val precedingConsumedArgument: String?
        get() = arguments.getOrNull(consumedArgumentCount - 1)
}

sealed interface CompletionRule {
    data class Choice(
        val options: (CompletionContext) -> List<String>,
    ) : CompletionRule

    data class Remainder(
        val source: CandidateSource,
    ) : CompletionRule

    data class DelimitedValue(
        val source: CandidateSource,
        val delimiter: (String) -> Boolean,
    ) : CompletionRule

    data class RepeatChoice(
        val options: (CompletionContext) -> List<String>,
    ) : CompletionRule

    data object None : CompletionRule
}

data class CommandCompletionSpec(
    val rules: List<CompletionRule>,
    val autoExecuteAllowed: Boolean = true,
)