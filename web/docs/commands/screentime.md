# screentime

The `screentime` command displays the total time you've spent on your device today.

## Syntax
```
screentime [app-name] [-all]
```

## Usage
- `screentime`: Shows your total screen time for the current day.
- `screentime <app_name>`: Shows the screen time for a specific application today.
- `screentime -all`: Displays the screen time for all applications used today.

## Example
```
screentime
screentime Instagram
screentime -all
```

!!! requirement
    This command requires "Usage Access" permission. The launcher will prompt you to grant this permission if it's missing. Note that this feature requires at least Android 5.1.
