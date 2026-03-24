# termux

The `termux` command allows you to execute commands in the Termux environment and see the output in Yantra Launcher.

## Syntax
```
termux <cmd> [args]
```

## Usage
- `termux <command>`: Runs the specified command in Termux and logs the standard output (stdout).

!!! requirement
    1. Termux must be installed.
    2. `allow-external-apps` must be set to `true` in `~/.termux/termux.properties`.
    3. `RUN_COMMAND` permission must be granted in Yantra Launcher settings.

## Example
```
termux echo Hello from Termux
```
