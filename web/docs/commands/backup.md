# backup

The `backup` command is a utility to backup and restore your Yantra Launcher configuration.

## Syntax
```
backup [-i]
```

## Usage
- `backup`: Exports your current configuration (themes, aliases, scripts, settings, etc.) to a file.
- `backup -i`: Imports a configuration file to restore settings.

Running `backup` opens Android's file picker so you can choose where to save the generated `.yantra` backup file. Running `backup -i` opens a file picker for choosing an existing backup. The imported file must have the `.yantra` extension.

!!! warning
    A valid import replaces your current configuration and restarts Yantra Launcher after the restore. If the file is invalid, the restore is rejected and the launcher does not restart.
