# alias

Create short-hand commands for longer or frequently used commands.

!!! note
    Yantra Launcher saves all aliases in memory and is retained even after restarting the Launcher session, unlike the _aliases_ in Unix terminals.

!!! note
    Pre-defined commands can not be aliased.

## Syntax
To set an alias:
```
alias alias_name = alias_cmd
```

To get value of an alias:
```
alias alias_name
```

To list all aliases:
```
alias
```

To reset all aliases to default:
```
alias -1
```

## Examples

=== "Example I"
    ``` title="Simple command alias"
    alias h = help
    ```
    This will create an alias `h` for the `help` command.

=== "Example II"
    ``` title="Alias with arguments"
    alias ps = search -playstore
    ```
    This will create an alias `ps` for the `search -playstore` command. This will let you search the Play Store with the `ps` command.
    ```
    ps Yantra Launcher
    ```

=== "Example III"
    ``` title="Alias for app launch"
    alias cod = launch call of duty
    ```
    This will create an alias `cod` for the `launch call of duty` command. This will let you launch Call of Duty with the `cod` command.