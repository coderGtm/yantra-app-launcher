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
│ CommandCompletionSpecs.kt          │      │ (TerminalSuggestionSources)    │
└────────────────────────────────────┘      └───────────────────────────────┘
               │                                      │
               ▼                                      ▼
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
| `CompletionCandidate.kt` | `CompletionCandidate(displayText, replacementText, score)` — one suggestion produced by a source. |
| `SuggestionSources.kt` | The `CandidateSource` enum and the `SuggestionSources` interface that adapters implement. |
| `SuggestionEngine.kt` | The pure engine: tokenizes input, walks a command's rules, matches candidates, and emits `CompletionResult` edits. |
| `CommandCompletionSpecs.kt` | `buildCommandCompletionSpecs(...)` — the grammar for every command that has completion. |
| `terminal/TerminalSuggestionSources.kt` | The production adapter: maps each `CandidateSource` to real data (apps, shortcuts, scripts, contacts, themes, files, ...). |

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
  prefix-matches the typed text against the available commands (and aliases). A fully-typed
  command is not re-suggested (`run` does not suggest `run`). `orderedPrimarySuggestions`,
  when supplied, is honored exactly so the user's reordering / hidden-command settings are
  preserved.
- **Anything else** (command + argument, or a trailing space) → secondary suggestions. The
  engine resolves the first token through the alias map, looks up the command's spec, and
  walks the grammar.

### 3. Rule selection (the core)

Each command has a `CommandCompletionSpec` — an ordered `List<CompletionRule>`. The engine
walks the rules left to right, **consuming argument positions**:

- **`Choice`** — a fixed set of discrete options (e.g. `launch -s | -p`). It consumes its
  slot only when the token at that slot *exactly* matches one of its options (a completed
  flag). When the slot is the in-progress token, it claims the position only if the partial
  text prefix-matches an option; otherwise it falls through **without** consuming, letting a
  following value rule claim the position. This is what lets `launch Google M` complete as
  `launch Google Maps ` instead of doubling `Google`.
- **`Remainder`** — everything from the first unconsumed argument to the end of input is one
  logical value (e.g. `run -lua <script>` or `call <full name>`). It replaces the **whole
  span**, which fixes the old doubling bug. `Remainder` replaces the *entire* remainder, not
  just the active token.
- **`DelimitedValue`** — like `Remainder`, but stops at a delimiter token (e.g. weather:
  location words until the first `-`-prefixed field). Once a delimiter token is present, the
  walker skips past it to the following rule.
- **`RepeatChoice`** — a discrete option list that can repeat (e.g. weather fields:
  `weather New York -temp -humidity`). Replaces only the active token.
- **`None`** — no more suggestions (e.g. after `search -e=google` or `echo -e`). The engine
  stops there.

### 4. Candidate matching

- **Discrete options** (`Choice` / `RepeatChoice`) are matched case-insensitively against
  the active token. Order is: prefix matches first (in source order), then substring
  matches. An option exactly equal to the partial text is omitted.
- **Value sources** (`Remainder` / `DelimitedValue`) fetch candidates from the
  `SuggestionSources` adapter and match them against the whole span text (case-insensitive
  substring). Exact matches are omitted.

### 5. Replacement edits

Every `CompletionResult` carries a `CompletionEdit(start, end, replacement, cursor)` — an
absolute range into the input captured when the suggestions were computed. The UI applies it
as:

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
3. Snapshots the input, command set, and alias map.
4. Runs `suggestionEngine.complete(...)` on `Dispatchers.Default`.
5. Renders the results on the main thread **only if this request is still the current one**
   (stale results are dropped).

`renderSuggestions()` builds the chips. Clicking a chip applies the edit; a primary chip's
long-press shows the command's help dialog. The `actOnSuggestionTap` and
`actOnLastSecondarySuggestion` settings are honored here (including the old exclusion list
for `call`, `time`, `bg`, `notepad`, `todo`, `run`, and dash-prefixed suggestions). The
`call` command's "contacts not fetched yet" message is also preserved.

The suggestion `CoroutineScope` is cancelled in `MainActivityCoordinator.onDestroy()`.

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
    else listOf("-new") + getScripts(terminal.preferenceObject).map { CompletionCandidate(it) }
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

### Toggle auto-execute on tap

`CommandCompletionSpec(autoExecuteAllowed = false)` disables auto-execution for a command
when `actOnSuggestionTap` is on. Commands like `search`, `dict`, `todo`, `bg`, `echo`,
`time`, `scripts`, and `weather` already disable it.

---

## Adding suggestions for a new command

Say you just added a command `gift` with usage `gift <friend> [socks|hat]`. Do this:

### 1. Add a spec in `CommandCompletionSpecs.kt`

Inside the `mapOf(...)`:

```kotlin
"gift" to CommandCompletionSpec(
    rules = listOf(
        CompletionRule.Remainder(CandidateSource.CONTACTS), // friend name
        CompletionRule.Choice { listOf("socks", "hat") },
    ),
),
```

The `gift` command name is automatically available for **primary** completion (command-name
prefix matching) — no extra work is needed there, because primary suggestions come from
`getAvailableCommands()` / the alias list.

### 2. Provide the data

If you used an existing `CandidateSource` (e.g. `CONTACTS`), you're done. If your command
needs data that isn't covered yet:

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

If a flag changes the meaning of a following value (like `launch -s` vs `launch -p`), the
adapter reads `context.precedingConsumedArgument` to decide what to return:

```kotlin
CandidateSource.APPS -> when (context.precedingConsumedArgument) {
    "-s" -> terminal.shortcutList.map { CompletionCandidate(it.label) }
    "-p" -> terminal.appList.map { CompletionCandidate(it.packageName) }
    else -> terminal.appList.map { CompletionCandidate(it.appName) }
}
```

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
- No quotes or escaping are ever required in commands.
- Existing command syntax is unchanged; completion is additive.
- Aliases are resolved the same way as `Terminal.handleCommand` (first token → alias map).
- `launchf` is special-cased in the adapter to return a single best fuzzy match.