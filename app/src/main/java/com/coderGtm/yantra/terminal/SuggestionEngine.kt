package com.coderGtm.yantra.terminal

import android.app.Activity
import com.coderGtm.yantra.models.Suggestion

class SuggestionEngine(
    activity: Activity
) {
    private var source: MutableList<MutableList<String>> = mutableListOf()

    fun setSource(source: MutableList<MutableList<String>>) {
        this.source = source
    }

    fun getSuggestionsForInput(input: String): List<Suggestion> {
        return emptyList()
    }
}