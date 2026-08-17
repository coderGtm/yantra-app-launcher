package com.coderGtm.yantra.suggestions

data class CompletionToken(
    val text: String,
    val start: Int,
    val end: Int,
)

data class CompletionInput(
    val rawText: String,
    val cursor: Int = rawText.length,
) {
    val beforeCursor: String
        get() = rawText.substring(0, cursor.coerceIn(0, rawText.length))
}

data class TokenizedInput(
    val tokens: List<CompletionToken>,
    val hasTrailingWhitespace: Boolean,
    val activeToken: CompletionToken?,
)

fun tokenize(input: CompletionInput): TokenizedInput {
    val text = input.beforeCursor
    val tokens = mutableListOf<CompletionToken>()
    var i = 0
    while (i < text.length) {
        if (text[i].isWhitespace()) {
            i++
            continue
        }
        val start = i
        while (i < text.length && !text[i].isWhitespace()) {
            i++
        }
        tokens.add(CompletionToken(text = text.substring(start, i), start = start, end = i))
    }
    val hasTrailingWhitespace = text.lastOrNull()?.isWhitespace() == true
    val activeToken = if (hasTrailingWhitespace) null else tokens.lastOrNull()
    return TokenizedInput(tokens = tokens, hasTrailingWhitespace = hasTrailingWhitespace, activeToken = activeToken)
}