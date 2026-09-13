# Settings Reference

The `settings` command opens Yantra Launcher's control room. There are quite a few switches in there, so here is the map before you wander into the walls.

## Appearance

Use the settings page to adjust themes, fonts, font size, terminal colors, background behavior, the prompt, suggestions, arrow keys, and the modern prompt design. The `theme`, `setclr`, and `bg` commands are useful when you want to make a change without leaving the terminal.

## Launcher Behavior

Settings also control whether Yantra is fullscreen, whether it hides the keyboard after a command, how suggestions behave, which commands are shown first, and which gestures or actions launch commands.

If Yantra is your default launcher, this is also where you can select or change that behavior. Android itself may show a separate confirmation dialog.

## AI

The `ai` command needs an OpenAI-compatible provider, its base URL or domain, an API key, and optionally a system prompt. Enter these in the AI settings. The provider may be paid or free. Just the domain (like `api.openai.com`) or the full base URL (like `https://api.openai.com/v1`) both work — Yantra adds the chat completions path automatically when it is missing and assumes `https://` when no scheme is given.

## Termux

The Termux settings contain the permission and command-session options needed by `termux`. If the command cannot start, check the [Termux setup guide](termux.md) as well as these settings.

## Other Integrations

Depending on your build and Android version, settings also include news URL configuration, sound-effect management, custom scripts, system-info art, aliases, backup-related configuration, and language selection.

When a command says “check settings”, search this page first, then use the command's dedicated page for the remaining requirements.
