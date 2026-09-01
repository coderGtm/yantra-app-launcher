package com.coderGtm.yantra.suggestions

enum class CandidateSource {
    APPS,
    SHORTCUTS,
    PACKAGE_NAMES,
    FILES,
    FOLDERS,
    SCRIPTS,
    CONTACTS,
    SFX,
    COMMANDS,
    ALIASES,
    THEMES,
    LOCATIONS,
}

interface SuggestionSources {
    fun candidates(
        source: CandidateSource,
        context: CompletionContext,
    ): List<CompletionCandidate>
}