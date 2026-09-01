package com.coderGtm.yantra.suggestions

import com.coderGtm.yantra.findSimilarity

fun bestFuzzyMatch(names: List<String>, query: String): String? {
    if (names.isEmpty() || query.isEmpty()) return null
    var bestName: String? = null
    var bestScore = -1.0
    for (name in names) {
        val score = findSimilarity(name.lowercase(), query)
        if (score > bestScore) {
            bestScore = score
            bestName = name
        }
    }
    return bestName
}
