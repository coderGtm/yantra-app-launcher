# Troubleshooting

Most Yantra Launcher problems are not mysterious. They are usually a permission, an unavailable companion app, an API key, or Android deciding that background work sounds suspicious. This page is the quick tour through the usual suspects.

## A Command Is Not Recognized

Run `help` and check the command name. Command names are case-insensitive, but spelling still matters. If a command exists only in Pro, it will not appear in the Minimal build. You can also use `help <command>` for its exact syntax.

## A Permission Dialog Never Appears

Open Android's App Settings for Yantra Launcher and check the relevant permission manually. For `screentime`, open Usage Access settings. For `lock`, check Accessibility or device-administrator setup. Android can remember a permanent denial and stop showing the first-time dialog.

## A Browser or App Will Not Open

Commands such as `web`, `open`, `search`, `gupt`, `email`, and `news` need a compatible app or browser. Install one, check that the URL is valid, and try again. `gupt` is the fallback when you want a private in-app browsing session instead of a separate browser.

## A Network Command Fails

Check your connection first, then check the command's provider requirements. `weather`, `dict`, `bored`, `search`, translation, AI, speedtest, and random wallpapers all depend on outside services. Those services can be unavailable, rate-limited, changed, or having a bad day.

## AI Does Not Respond

Confirm the provider base URL or domain, API key, and system prompt in `settings`. Just the domain (like `api.naga.ac`) or the full base URL (like `https://api.naga.ac/v1`) both work, since Yantra adds `/v1/chat/completions` automatically when it is missing. Then send a short test such as `ai hi` before attempting a very large request.

## Termux Commands Fail

Follow the [Termux setup guide](termux.md). Check that Termux is installed from a supported source, Run Command permission is granted, `allow-external-apps=true` is enabled, and Termux has been opened at least once.

## Yantra Launcher Does Not Return to the Home Screen

If Yantra Launcher is set as the default launcher, Android may restart it after `exit` or after a configuration restore. If that is not what you want, change the default launcher in Android settings.

Still stuck? Include the command you ran, your Android version, device model, build variant, and the exact error text when opening an issue. “It broke” is emotionally accurate, but not yet enough for debugging.
