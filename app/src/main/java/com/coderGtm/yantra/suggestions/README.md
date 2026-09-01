# Yantra Suggestion Engine

The suggestion engine is a pure-Kotlin, unit-testable completion system that powers the
suggestion chips shown above the command input. It replaced the old 30-branch `showSuggestions`
implementation in `Terminal.kt` / `Helper.kt`.

The engine is **command-specific** and **token-aware**. It understands command argument
structures such as `run -lua script`, `weather New York -temp`, and `call John Smith`
without requiring quotes or escaping.

## Why it exists

The old implementation:

- Used `rawInput.split(" ")` and lost source spans, which caused text to be doubled when a
  suggestion was applied against a multi-word value (e.g. `launch Google M` → `launch Google
  Google Maps `).
- Was a giant `if/else` chain — one branch per command, hard to extend.
- Ran on a raw `Thread`, with no debounce or cancellation.

The new engine fixes all of these by returning **replacement edits (ranges)** instead of
appended strings, and by describing each command with a small completion grammar.

---

## Architecture overview

```
input text
   │
   ▼
┌──────────────────────────────┐
│ CompletionInput(rawText,     │
│              cursor)         │
└──────────────┬───────────────┘
               ▼
┌──────────────────────────────┐      ┌───────────────────────────────┐
│ tokenize()                   │─────▶│ TokenizedInput                │
│ CompletionToken.kt           │      │  tokens: List<CompletionToken>│
└──────────────┬───────────────┘      │  hasTrailingWhitespace        │
               ▼                       │  activeToken                 │
┌──────────────────────────────┐      └───────────────────────────────┘
│ SuggestionEngine.complete()  │
│ SuggestionEngine.kt          │
└───┬──────────────┬───────────┘
    │              │
    ▼              ▼
┌──────────────────┴─────────────────┐      ┌───────────────────────────────┐
│ CommandCompletionSpec (grammar)    │      │ SuggestionSources adapter     │
│ CommandCompletionSpecs.kt          │      │ (TerminalSuggestionSources    │◀── TerminalSuggestionState
└────────────────────────────────────┘      │  (terminal, state))           │    (main-thread snapshot of
                │                            └───────────────────────────────┘    apps/shortcuts/contacts/…)
                ▼
┌───────────────────────────────────────────────────────────────┐
│ List<CompletionResult>                                        │
│   CompletionResult(displayText, edit, isPrimary,              │
│                    allowAutoExecute, commandName)             │
└───────────────────────────────────────────────────────────────┘
                │
                ▼
      Terminal.renderSuggestions()  → suggestion chips (Compose UI)
```

The engine itself (`suggestions/`) has **zero Android dependencies**. All Android-specific
data access lives behind the `SuggestionSources` interface, so the whole engine is testable
with plain JUnit in `app/src/test/.../suggestions/SuggestionEngineTest.kt`.

---

## The files

| File | Responsibility |
|------|----------------|
| `CompletionToken.kt` | `CompletionToken(text, start, end)`, `CompletionInput(rawText, cursor)`, `TokenizedInput`, and the `tokenize()` function. Splits the input on whitespace while remembering each token's character span. |
| `CompletionGrammar.kt` | `CompletionContext`, the `CompletionRule` sealed interface (`Choice`, `Remainder`, `DelimitedValue`, `RepeatChoice`, `None`), and `CommandCompletionSpec`. |
| `CompletionCandidate.kt` | `CompletionCandidate(displayText, replacementText, preMatched)` — one suggestion produced by a source. `preMatched = true` marks a candidate the source has already matched, bypassing the engine's substring filter. |
| `FuzzyMatch.kt` | `bestFuzzyMatch(names, query)` — a pure, empty-guarded function returning the single best similarity match (used by the `launchf` special case). |
| `SuggestionSources.kt` | The `CandidateSource` enum and the `SuggestionSources` interface that adapters implement. |
| `SuggestionEngine.kt` | The pure engine: tokenizes input, walks a command's rules (merging flag and value candidates at a fresh position), matches candidates, and emits `CompletionResult` edits. |
| `CommandCompletionSpecs.kt` | `buildCommandCompletionSpecs(getThemes, getTodoArguments, getWeatherFields)` — the grammar for every command that has completion (e.g. `unalias` is `Choice { -1 }` followed by `Remainder(CandidateSource.ALIASES)`). `Terminal` filters the result per flavor (`.filterKeys { it in commands }`), so free builds get no suggestions for Pro-only commands. |
| `terminal/TerminalSuggestionSources.kt` | The production adapter, `TerminalSuggestionSources(terminal, state)`. Maps each `CandidateSource` to real data (apps, shortcuts, scripts, contacts, themes, files, ...). List data comes from the `TerminalSuggestionState` snapshot; disk I/O and `SharedPreferences` reads stay here. |

---

## How it works, step by step

### 1. Tokenize

`tokenize()` (in `CompletionToken.kt`) scans the text up to the cursor and splits it on
whitespace, keeping the `start`/`end` character offsets of every token. It also records:

- `hasTrailingWhitespace` — whether the text ends with a space. A trailing space means the
  user is starting a *new* argument, so there is no "active" in-progress token.
- `activeToken` — the last token, when the text does *not* end in whitespace (the token the
  user is still typing).

Repeated spaces are preserved as boundaries: `"launch  Google"` is two tokens,
`launch(0..5)` and `Google(8..13)`.

### 2. Primary vs. secondary suggestions

`SuggestionEngine.complete()` first checks whether the user is typing just the command name:

- **Single token and no trailing space** → primary suggestions (command names). The engine
  matches the typed text anywhere in the command name (case-insensitive substring, mirroring
  the old `containsMatchIn` behavior — typing `nch` still suggests `launch`), preserving the
  user-configured order. A fully-typed command is not re-suggested (`run` does not suggest
  `run`). `orderedPrimarySuggestions`, when supplied, is honored exactly so the user's
  reordering / hidden-command settings are preserved.
- **Anything else** (command + argument, or a trailing space) → secondary suggestions. The
  engine resolves the first token through the alias map with an **exact match first, then a
  lowercase fallback** (`aliases[firstToken.text] ?: firstToken.text.lowercase()`). This is
  autocapitalization-safe — `Launch ch` resolves to the `launch` spec — and mirrors how
  `Terminal.handleCommand` resolves aliases before lowercasing the command name.

### 3. Rule selection (the core)

Each command has a `CommandCompletionSpec` — an ordered `List<CompletionRule>`. The engine
walks the rules left to right, **consuming argument positions**:

- **`Choice`** — a fixed set of discrete options (e.g. `launch -s | -p`).
  - At a **completed** (non-active) slot it consumes the slot only when the token at that
    slot *exactly* matches one of its options (a finished flag). A consumed flag does
    **not** merge anything — the walk simply advances to the next rule (`launch -p com.gma`
    suggests packages only, not the `-s`/`-p` flags again).
  - At the **active** slot (the in-progress token, including the fresh position right after
    a trailing space) it claims the position when the partial text (possibly empty) is a
    substring of one of its options (case-insensitive). When it claims, a directly-following
    `Remainder` / `DelimitedValue` is **also** evaluated at the same position and its
    candidates are merged with the flag options — so a fresh argument position shows flags
    AND values together (`launch ` suggests `-s`, `-p`, and every app name). If the partial
    text matches no option, the Choice falls through **without** consuming, letting a
    following value rule claim the position. This is what lets `launch Google M` complete
    as `launch Google Maps ` (the whole remainder replaced) instead of doubling `Google`,
    while `run lua` still suggests `-lua`.
- **`Remainder`** — everything from the first unconsumed argument to the end of input is one
  logical value (e.g. `run -lua <script>` or `call <full name>`). It replaces the **whole
  span**, which fixes the old doubling bug. `Remainder` replaces the *entire* remainder, not
  just the active token.
- **`DelimitedValue`** — like `Remainder`, but stops at a delimiter token (e.g. weather:
  location words until the first `-`-prefixed field). Once a delimiter token is present, the
  walker skips past it to the following rule. (Exception: when merged behind a claimed
  `Choice`, the delimiter-position guard is skipped — see the caution under
  "Adding suggestions".)
- **`RepeatChoice`** — a discrete option list that can repeat (e.g. weather fields:
  `weather New York -temp -humidity`). Options already present in the input as complete
  arguments are filtered out (`weather nyc -temp -` suggests the other fields, not `-temp`
  again). Replaces only the active token.
- **`None`** — no more suggestions (e.g. after `search -e=google` or `echo -e`). The engine
  stops there.

Merged flag/value results are de-duplicated by display text, so a value that is also a flag
option appears once.

### 4. Candidate matching

- **Discrete options** (`Choice` / `RepeatChoice`) are matched case-insensitively against
  the active token. Order is: prefix matches first (in source order), then substring
  matches. An option exactly equal to the partial text is omitted.
- **Value sources** (`Remainder` / `DelimitedValue`) fetch candidates from the
  `SuggestionSources` adapter and match them against the whole span text (case-insensitive
  substring). Exact matches are omitted.
- **`preMatched` candidates** skip the substring filter entirely and surface as-is. This is
  how `launchf` shows its best match even when the query is not a substring of the app
  name.

### 5. Replacement edits

Every `CompletionResult` carries a `CompletionEdit(start, end, replacement)` — an absolute
range into the input captured when the suggestions were computed. The UI applies it as:

```kotlin
val applied = currentText.substring(0, result.edit.start) +
    result.edit.replacement +
    currentText.substring(result.edit.end)
```

A trailing space is always appended so the user can keep typing the next argument.

### 6. Rendering (Terminal)

`Terminal.scheduleSuggestions()` is the production entry point. It:

1. Cancels any in-flight request.
2. Debounces for **75 ms**.
3. Snapshots everything the completion needs **on the main thread**: the command set, the
   alias map, and a `TerminalSuggestionState` (via `Terminal.buildSuggestionState()`). The
   `call` command's "contacts not fetched yet" message is rendered here too, before any
   background work.
4. Runs `suggestionEngine.complete(...)` on `Dispatchers.Default`.
5. Renders the results directly on the main thread. A superseded request never gets this
   far: cancelling its job throws out of `withContext` before the render, so stale results
   are dropped.

`renderSuggestions()` builds the chips. Clicking a chip applies the edit; a primary chip's
long-press shows the command's help dialog. The `actOnSuggestionTap` and
`actOnLastSecondarySuggestion` settings are honored here (including the old exclusion list
for `call`, `time`, `bg`, `notepad`, `todo`, `run`, and dash-prefixed suggestions). The
`call` command's "contacts not fetched yet" message is also preserved.

The suggestion `CoroutineScope` is cancelled in `MainActivityCoordinator.onDestroy()`.

### 7. The main-thread snapshot (`TerminalSuggestionState`)

The adapter runs inside `withContext(Dispatchers.Default)`, but the lists it serves (apps,
shortcuts, contacts, commands, aliases) live in **mutable fields on `Terminal`**
(`appList`, `shortcutList`, `aliasList`, ...). The launcher mutates those lists *in place*
when apps are installed/uninstalled, so reading them from a background thread risks a
`ConcurrentModificationException` (or torn reads).

`Terminal.buildSuggestionState()` therefore copies them on `Dispatchers.Main`, right after
the debounce and before the background dispatch, into a `TerminalSuggestionState`
(`appNames`, `packageNames`, `shortcutLabels`, `contactNames`, `commandNames`,
`aliasKeys`). `TerminalSuggestionSources(terminal, state)` reads **only** this snapshot for
list data.

Two categories deliberately stay inside the adapter instead of the snapshot:

- **Disk I/O** — `FILES` / `FOLDERS` (via Croissant) and `SFX` (a `filesDir` listing):
  already background-appropriate.
- **`SharedPreferences`** — scripts, themes, and todo arguments: thread-safe to read from
  any thread.

`getFiles` / `getFolders` wrap their Croissant call in try/catch and return `emptyList()`
on failure: Croissant's error paths call `terminal.output`, a UI method, which would throw
when invoked from `Dispatchers.Default` — the guard keeps a failed listing from crashing
the completion coroutine.

### 8. `launchf` fuzzy matching

`launchf <query>` is special-cased in the adapter: when the `APPS` source is requested for
the `launchf` command, the adapter derives the query from the raw input after the first
whitespace token (trimmed and lowercased — alias- and autocapitalization-safe), runs
`bestFuzzyMatch(state.appNames, query)`, and returns the single best similarity match as
`CompletionCandidate(best, preMatched = true)`. The `preMatched` flag bypasses the engine's
substring filter, so the chip appears even when the query is not a substring of the app
name (e.g. `launchf gmaps` → `Google Maps`).

---

## Modifying existing suggestions

Most changes are a single edit in one of two places:

### Change a command's option list

Edit the rule in `CommandCompletionSpecs.kt`. For example, to add `-dark` to the `list`
command's subcommands:

```kotlin
"list" to CommandCompletionSpec(
    rules = listOf(
        CompletionRule.Choice { listOf("apps", "shortcuts", "themes", "contacts", "-dark") },
    ),
),
```

### Change where a command's values come from

Edit the `CandidateSource` used by the rule, or the `TerminalSuggestionSources` adapter.
For example, to make `uninstall` suggest packages instead of app names:

```kotlin
"uninstall" to CommandCompletionSpec(listOf(CompletionRule.Remainder(CandidateSource.PACKAGE_NAMES))),
```

To change what a source returns (say, scripts should also suggest `-new`):

```kotlin
CandidateSource.SCRIPTS -> {
    if (precedingArg == "-new") emptyList()
    else getScripts(terminal.preferenceObject).map { CompletionCandidate(it) }
}
```

Note that list-backed sources read from the `state` snapshot, not from `Terminal`'s live
fields:

```kotlin
CandidateSource.APPS -> when (context.precedingConsumedArgument) {
    "-s" -> state.shortcutLabels.map { CompletionCandidate(it) }
    "-p" -> state.packageNames.map { CompletionCandidate(it) }
    else -> state.appNames.map { CompletionCandidate(it) }
}
```

### Change matching / ordering

- **Ordering**: the engine keeps source order, with prefix matches before substring matches.
  Reorder the list returned by the source or the `Choice` options to change display order.
- **Exact-match omission**: a candidate equal to the typed text is hidden. To re-suggest
  exact matches, remove the `lower == lowerPartial -> {}` branch in `matchDiscrete` /
  `matchValue`.
- **Case sensitivity**: matching is case-insensitive (`lowercase()`). Change it in
  `matchDiscrete` / `matchValue` if needed.
- **preMatched bypass**: candidates marked `preMatched` skip the substring filter entirely
  (`matchValue` returns them unconditionally). Only set it when the source has already done
  its own matching, as `launchf` does.
- **Where matching happens**: substring matching applies in three places — the primary path
  (command names), the `Choice`/`RepeatChoice` rule claim, and `matchValue`/`matchDiscrete`
  candidate filtering. Changing one leaves the others on the old behavior.

### Toggle auto-execute on tap

`CommandCompletionSpec(autoExecuteAllowed = false)` disables auto-execution for a command
when `actOnSuggestionTap` is on. Commands like `search`, `dict`, `todo`, `bg`, `echo`,
`time`, `scripts`, and `weather` already disable it.

---

## Adding suggestions for a new command

Say you just added a command `gift` with usage `gift <friend> [socks|hat]`. Do this:

### 1. Add a spec in `CommandCompletionSpecs.kt`

Inside the `mapOf(...)` in `buildCommandCompletionSpecs(getThemes, getTodoArguments,
getWeatherFields)`:

```kotlin
"gift" to CommandCompletionSpec(
    rules = listOf(
        CompletionRule.Remainder(CandidateSource.CONTACTS), // friend name
        CompletionRule.Choice { listOf("socks", "hat") },
    ),
),
```

The spec map is filtered per flavor at construction (`.filterKeys { it in commands }`), so
if `gift` is Pro-only, free builds automatically get no suggestions for it.

The `gift` command name is automatically available for **primary** completion (substring
matching against command names) — no extra work is needed there, because primary suggestions
come from
`getAvailableCommands()` / the alias list.

### 2. Provide the data

If you used an existing `CandidateSource` (e.g. `CONTACTS`), you're done. If your command
needs data that isn't covered yet, first check where the data lives:

- **Live list on `Terminal`** (like `appList`): add a field to `TerminalSuggestionState`
  and fill it in `Terminal.buildSuggestionState()`. The adapter must not read mutable
  Terminal state directly (see "The main-thread snapshot" above).
- **`SharedPreferences` or disk**: keep the read in the adapter.

  - **Add a new `CandidateSource` enum value** in `SuggestionSources.kt`:

    ```kotlin
    GIFTS,
    ```

  - **Handle it in `TerminalSuggestionSources.kt`**:

    ```kotlin
    CandidateSource.GIFTS -> terminal.preferenceObject.getString("gifts", "")
        ?.split(",")
        ?.filter { it.isNotEmpty() }
        .orEmpty()
        .map { CompletionCandidate(it) }
    ```

### 3. Handle flags / multi-argument structure

Use the rule that matches the shape of your command:

| Command shape | Rules |
|---------------|-------|
| `cmd <free text>` | `Remainder(source)` |
| `cmd <flag> <value>` | `Choice { flags }` then `Remainder(source)` |
| `cmd <value> <flag>` | `DelimitedValue(source, delimiter = { it.startsWith("-") })` then `RepeatChoice { ... }` |
| `cmd <fixed words>` | `Choice { words }` |
| `cmd <first value> <fixed word>` | `Remainder(source)` then `Choice { words }` |
| `cmd <flag>` then stop | `Choice { flags }` then `CompletionRule.None` |

> **Caution:** avoid a `Choice` directly followed by a `DelimitedValue`. The merge rule
> evaluates a directly-following value rule *without* its delimiter-position guard, so when
> the Choice claims the active slot the `DelimitedValue` is matched at that same position
> even if a delimiter token is already present. No current spec uses this shape — avoid it,
> or handle the interaction deliberately.

If a flag changes the meaning of a following value (like `launch -s` vs `launch -p`), the
adapter reads `context.precedingConsumedArgument` to decide what to return (see the
`CandidateSource.APPS` example above).

### 4. Write a test

Add a test class (or method) in `app/src/test/java/com/coderGtm/yantra/suggestions/SuggestionEngineTest.kt`.
Follow the existing patterns:

```kotlin
class GiftCompletionTest {

    private val giftEngine = SuggestionEngine(
        mapOf(
            "gift" to CommandCompletionSpec(
                rules = listOf(
                    CompletionRule.Remainder(CandidateSource.CONTACTS),
                    CompletionRule.Choice { listOf("socks", "hat") },
                ),
            ),
        )
    )

    private val giftSources = object : SuggestionSources {
        override fun candidates(source: CandidateSource, context: CompletionContext): List<CompletionCandidate> =
            when (source) {
                CandidateSource.CONTACTS -> listOf("Alice", "Bob").map { CompletionCandidate(it) }
                else -> emptyList()
            }
    }

    private fun completeGift(raw: String) = giftEngine.complete(
        input = CompletionInput(rawText = raw, cursor = raw.length),
        commands = setOf("gift"),
        aliases = emptyMap(),
        sources = giftSources,
        primarySuggestionsEnabled = true,
        secondarySuggestionsEnabled = true,
    )

    @org.junit.Test
    fun `gift contact completion`() {
        val results = completeGift("gift Al")
        assertTrue(results.any { it.displayText == "Alice" })
    }

    @org.junit.Test
    fun `gift choice after contact`() {
        val results = completeGift("gift Alice ")
        assertTrue(results.map { it.displayText }.containsAll(listOf("socks", "hat")))
    }
}
```

Run the tests:

```bash
./gradlew testFreeDebugUnitTest --tests '*GiftCompletionTest'
```

### 5. (Optional) Disable auto-execute

If the command should never auto-execute on suggestion tap, add `autoExecuteAllowed = false`
to the spec. If it should also be excluded from `actOnLastSecondarySuggestion` auto-execution,
add its name to the exclusion list in `Terminal.renderSuggestions()`.

---

## Design decisions (do not re-litigate)

- The engine is synchronous and pure; only request scheduling uses coroutines.
- `Remainder` / `DelimitedValue` replace the entire logical value span, which fixes the
  doubling bug.
- `Choice` / `RepeatChoice` replace only the active token.
- **Merge at a fresh position** (user decision, 2026-09-01): when a `Choice` claims the
  active argument slot (a fresh trailing-space position, or a partial substring match on an
  option), a directly-following `Remainder` / `DelimitedValue` is also evaluated at that
  position and its candidates merged — fresh argument positions show flags AND values. A
  consumed flag (exact match at a completed slot) does not merge.
- **Main-thread snapshot**: the adapter reads list data only from `TerminalSuggestionState`,
  built on `Dispatchers.Main` before the background dispatch. Live mutable `Terminal` state
  must not be read on `Dispatchers.Default` — the launcher mutates app/shortcut/alias lists
  in place, risking `ConcurrentModificationException`. Disk I/O and `SharedPreferences`
  reads stay in the adapter.
- **`launchf` fuzzy match**: the adapter returns the single best `bestFuzzyMatch` candidate
  marked `preMatched`, which bypasses the engine's substring filter so the best match shows
  even when the query is not a substring of the app name.
- Command resolution is **alias exact match, then lowercase** — autocapitalization-safe and
  identical to `Terminal.handleCommand`.
- `RepeatChoice` filters out options already present as complete arguments.
- Specs are filtered per flavor (`.filterKeys { it in commands }`); free builds get no
  suggestions for Pro-only commands.
- `unalias` is `Choice { -1 }` followed by `Remainder(CandidateSource.ALIASES)`.
- No quotes or escaping are ever required in commands.
- Existing command syntax is unchanged; completion is additive.
