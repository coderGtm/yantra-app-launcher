package com.coderGtm.yantra.suggestions

data class CompletionCandidate(
    val displayText: String,
    val replacementText: String = displayText,
    val score: Int = 0,
)