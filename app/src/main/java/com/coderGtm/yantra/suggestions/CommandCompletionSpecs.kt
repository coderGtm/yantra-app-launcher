package com.coderGtm.yantra.suggestions

fun buildCommandCompletionSpecs(
    getScripts: () -> List<String>,
    getAliases: () -> List<String>,
    getThemes: () -> List<String>,
    getTodoArguments: () -> List<String>,
    getSfxNames: () -> List<String>,
    getCommandNames: () -> List<String>,
    getWeatherFields: () -> Set<String>,
): Map<String, CommandCompletionSpec> = mapOf(
    "launch" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("-s", "-p") },
            CompletionRule.Remainder(CandidateSource.APPS),
        ),
    ),
    "open" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.FILES))),
    "cd" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.FOLDERS))),
    "sfx" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.SFX))),
    "ls" to CommandCompletionSpec(listOf(CompletionRule.Choice { listOf("-a") })),
    "uninstall" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.APPS))),
    "info" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("-p") },
            CompletionRule.Remainder(CandidateSource.APPS),
        ),
    ),
    "launchf" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.APPS))),
    "screentime" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("-all") },
            CompletionRule.Remainder(CandidateSource.APPS),
        ),
    ),
    "call" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.CONTACTS))),
    "list" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("apps", "shortcuts", "themes", "contacts") },
        ),
    ),
    "notepad" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("list", "read", "new", "edit", "delete") },
        ),
    ),
    "community" to CommandCompletionSpec(
        rules = listOf(CompletionRule.Choice { listOf("discord", "reddit") }),
    ),
    "search" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice {
                listOf(
                    "-e=google", "-e=duckduckgo", "-e=brave", "-e=bing", "-e=yahoo",
                    "-e=ecosia", "-e=startpage", "-e=qwant", "-e=you", "-e=playstore",
                    "-e=maps", "-e=youtube", "-u=",
                )
            },
            CompletionRule.None,
        ),
        autoExecuteAllowed = false,
    ),
    "battery" to CommandCompletionSpec(listOf(CompletionRule.Choice { listOf("-bar") })),
    "location" to CommandCompletionSpec(listOf(CompletionRule.Choice { listOf("-refresh") })),
    "dict" to CommandCompletionSpec(
        rules = listOf(CompletionRule.Choice { listOf("-urban") }),
        autoExecuteAllowed = false,
    ),
    "flash" to CommandCompletionSpec(listOf(CompletionRule.Choice { listOf("1", "0", "on", "off") })),
    "bluetooth" to CommandCompletionSpec(listOf(CompletionRule.Choice { listOf("1", "0", "on", "off") })),
    "todo" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { getTodoArguments() },
            CompletionRule.None,
        ),
        autoExecuteAllowed = false,
    ),
    "help" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.COMMANDS))),
    "alias" to CommandCompletionSpec(listOf(CompletionRule.Choice { listOf("-1") })),
    "unalias" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("-1") },
            CompletionRule.Remainder(CandidateSource.ALIASES),
        ),
    ),
    "theme" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice {
                listOf("Custom") +
                    getThemes() +
                    listOf("-save", "-export", "-import", "-remove")
            },
        ),
    ),
    "sysinfo" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice {
                listOf(
                    "-os", "-host", "-kernel", "-uptime", "-apps", "-terminal", "-font",
                    "-resolution", "-theme", "-cpu", "-memory", "-art",
                )
            },
        ),
    ),
    "bg" to CommandCompletionSpec(
        rules = listOf(CompletionRule.Choice { listOf("-1", "random") }),
        autoExecuteAllowed = false,
    ),
    "echo" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("-e", "-s", "-w") },
            CompletionRule.None,
        ),
        autoExecuteAllowed = false,
    ),
    "time" to CommandCompletionSpec(
        rules = listOf(CompletionRule.Choice { listOf("utc") }),
        autoExecuteAllowed = false,
    ),
    "backup" to CommandCompletionSpec(listOf(CompletionRule.Choice { listOf("-i") })),
    "music" to CommandCompletionSpec(
        rules = listOf(CompletionRule.Choice { listOf("play", "pause", "prev", "next") }),
    ),
    "scripts" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("-new", "-rm") },
            CompletionRule.Remainder(CandidateSource.SCRIPTS),
        ),
        autoExecuteAllowed = false,
    ),
    "run" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.Choice { listOf("-lua", "-clean") },
            CompletionRule.Remainder(CandidateSource.SCRIPTS),
        ),
    ),
    "weather" to CommandCompletionSpec(
        rules = listOf(
            CompletionRule.DelimitedValue(
                source = CandidateSource.LOCATIONS,
                delimiter = { it.startsWith("-") },
            ),
            CompletionRule.RepeatChoice { getWeatherFields().map { "-$it" } },
        ),
        autoExecuteAllowed = false,
    ),
)