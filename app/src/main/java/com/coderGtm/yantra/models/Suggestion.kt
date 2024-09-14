package com.coderGtm.yantra.models

data class Suggestion(
    val text: String,
    val depth: Int,
    val isEndOfCommand: Boolean,
    var isHidden: Boolean
)
