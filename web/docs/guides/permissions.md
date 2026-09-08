# Permissions & Prerequisites

Yantra Launcher can do a surprising amount from one little command line, but Android still asks for permission before it lets an app touch the interesting parts of your phone. If a command opens a permission dialog, that is usually Android being Android, not Yantra being dramatic.

## Common Permissions

| Feature | Commands | What may be required |
| --- | --- | --- |
| Contacts | `call`, `list contacts` | Contacts permission |
| Location | `location` | Location permission and an enabled location provider |
| Screen time | `screentime` | Usage Access permission |
| Phone actions | `call`, `lock` | Phone-related permission or device/security setup, depending on Android version |
| Notifications | `notify` | Notification permission on Android versions that require it |
| Screen locking | `lock` | Yantra Accessibility Service or device administrator setup |
| Bluetooth | `bluetooth` | Bluetooth permissions and Android-version support |
| Termux execution | `termux` | Termux, Run Command permission, and Termux external-app access |

## A Few Useful Notes

- `screentime` needs Usage Access, not merely ordinary app permissions.
- `lock` may ask you to enable a service in Android settings before it can work.
- `location` can report cached data. Use `location -refresh` when you need a fresh update.
- Some features are limited by the Android version or by the device manufacturer.
- If a permission was denied permanently, open Android's App Settings and grant it there. Repeating the command will not magically negotiate with Android on your behalf.

For a command-specific explanation, use `help <command>` inside Yantra or open that command's page in this wiki.
