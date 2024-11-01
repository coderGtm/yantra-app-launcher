# alias

Create short-hand commands for longer or frequently used commands.

!!! note annotate
    Yantra Launcher aliases are retained even after restarting the launcher session, unlike the aliases in _usual or conventional_ shells (1).

1.  In this context _usual or conventional shells_ refer to [shells](https://en.m.wikipedia.org/wiki/Shell_%28computing%29) like Bash, Zsh, CMD, Powershell, etc. Yantra is unusual in this regard because of its unique features for aliases, which is persistency.


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
    ``` title="Simple Command Alias"
    alias h = help
    ```
    This will create an alias `h` for the `help` command.

=== "Example II"
    ``` title="Alias With Arguments"
    alias ps = search -playstore
    ```
    This will create an alias `ps` for the `search -playstore` command. This will let you search the Play Store with the `ps` command.
    !!! example
        ```
        ps Yantra Launcher
        ```

=== "Example III"
    ``` title="Alias for App Launch"
    alias cod = launch call of duty
    ```
    This will create an alias `cod` for the `launch call of duty` command. This will let you launch Call of Duty with the `cod` command.