# Command Conventions

Yantra Launcher commands are designed to feel like terminal commands, but they live inside an Android launcher. As a result, you may feel that they are non-standard and different from what you are used to, but that is an intentional design decision to make the experience of using commands easy and enjoyable on a mobile device with soft keyboard. A few conventions make the experience much less mysterious.

## Command Names

Command names are matched case-insensitively. Arguments are generally passed as space-separated words, so commands do not provide a full shell parser. In particular, do not assume that shell quotes will group words into one argument.

## Flags

Flags begin with `-`. Some commands use a flag followed by a value, such as `search -e=duckduckgo`, while others use separate words, such as `todo -p 1 30`. The command's help page is the authority for that command's format.

## Aliases

Use `alias` to list aliases, `alias name = command` to create or update one, and `unalias name` to remove one. Built-in command names cannot be reused as alias names. Aliases are stored in Yantra Launcher's configuration and survive launcher sessions.

## Indexes

Some commands use indexes instead of names. Todo indexes are zero-based: the first item is `0`, the second is `1`, and so on. Check the output before marking or removing an item.

## Spaces and Names

Commands that accept free text, such as `echo`, `notify`, `text`, and `tts`, can use spaces in the message. Commands that identify one stored item, such as notes or scripts, may require a single word for the name.

## Suggestions and Help

The suggestion bar offers command and argument completions as you type. Long-press a primary command suggestion for a quick help popup. For the complete runtime list, use `help`; for one command, use `help <command>`.

## Execution

Most commands return immediately after starting their action. Network requests, Termux commands, timers, scripts, and other long-running operations may continue or wait for an external result. Read the command page when timing matters.
