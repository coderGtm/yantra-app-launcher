# Privacy Policy

**Effective date:** September 9, 2026

Yantra Launcher is a minimal, CLI-based Android launcher. It is open source and privacy-first: your commands and data stay on your device by default.

Short version: **Yantra Launcher collects zero data by default. No analytics, no tracking, no ads, no third-party data sharing by the app itself. The only data that ever leaves your device for us is a crash report you explicitly choose to send.**

## Data We Do Not Collect

- No account is required to use Yantra Launcher.
- No analytics, advertising SDKs, or tracking frameworks are used to collect your behavior.
- We do not collect, store, or share your typed commands, app usage, contacts, location, or files on any server operated by us, except for a crash report you explicitly choose to send (see below).
- The source code is public at [coderGtm/yantra-app-launcher](https://github.com/coderGtm/yantra-app-launcher) — you can verify this yourself.

## Data Stored On Your Device

Yantra Launcher stores your configuration locally on your Android device so the app can work offline, including:

- Settings, themes, fonts, aliases, and username.
- Todos, notes, scripts (including Lua scripts), command history, and backups you create.
- AI provider base URL or domain, API key, and model settings you enter in Settings.

This data never leaves your device unless *you* cause it to — for example by creating a backup file and sharing it, exporting a theme, or running a command that contacts an outside service (see below).

You can clear app data or uninstall the app at any time via Android Settings to remove locally stored data.

## Crash Reports (Only If You Choose to Send One)

If the app crashes, Yantra Launcher shows a dialog asking you to send a crash report to the developer to help debug the crash. Pressing Ok sends the report; pressing Cancel sends nothing.

- What is sent: app/package info, app version, Android version, device brand/model/product, stack trace and stack-trace hash, app-start and crash dates, installation ID, thread details, plus the optional comment and email you enter.
- Email is optional. If provided, it is used only to contact you if necessary for crash resolution. It is not shared with anyone, is securely stored by the developer, and is deleted when the report is deleted on the server.
- Comment is also optional and can help describe what you were doing when the crash happened.
- Reports are sent to the developer's crash-report backend. To avoid sending anything, press Cancel in the crash dialog and leave the email field empty.

Avoid including sensitive information (such as passwords or private data) in the optional comment field.

## Permissions and How They Are Used

Android permissions are requested only when needed for the feature you invoke. See [Permissions & Prerequisites](guides/permissions.md) for details.

If you deny a permission, the related command(s) will not work, but the rest of the launcher still works. If a permission was denied permanently, grant it from Android App Settings.

## Third-Party Services You Choose to Contact

Some commands intentionally contact third-party services to fulfill your request. Yantra Launcher does not proxy or log these requests — your device talks to the service directly:

- `weather`, `dict`, `translate`, `search`, `web`, `news`, `speedtest`, `bored`, wallpaper/background fetch, and similar commands send your query (e.g. city name, word, URL) to that service.
- `ai` sends your prompt, conversation history, API key, and model settings to the AI provider *you* configured in Settings. That provider's billing, retention, and privacy policies apply — not this policy. Treat your API key like a password and never paste it into public issues, screenshots, scripts, or theme files.
- Lua scripts using `http.get/post/put/delete/patch` or `binding.exec()` can contact any URL or run Yantra commands with the app's permissions. Only run scripts you understand.
- `termux` runs commands in the Termux app on your device under Termux's own policies.

Avoid sending sensitive information in queries unless you have checked that service's privacy policy.

## Private Browsing (`gupt`)

`gupt` (“Get Undercover Private Tab”) provides in-app private browsing:

- Browsing data for the tab is cleared when the tab is closed.
- No history or cookies are saved by Yantra Launcher, and the tab is hidden from recent apps.

Private browsing is not invisibility: your network, DNS provider, visited websites, or Android itself may still observe activity.

## Backups, Themes, and Scripts

- Backups may contain personal configuration such as aliases, scripts, themes, and settings. Store backup files somewhere you trust.
- Exported themes may contain colors, fonts, and settings you chose. Check the file before sharing it publicly.

## Children's Privacy

Yantra Launcher does not knowingly collect personal information from anyone, including children. Since all data stays on-device by default, there is no server-side profile to delete. Parents/guardians can clear app data or uninstall the app from Android Settings.

## Changes to This Policy

If this policy changes, the updated version will be published here with a new effective date.

## Contact

For privacy questions, open an issue or discussion at:

- GitHub: [https://github.com/coderGtm/yantra-app-launcher](https://github.com/coderGtm/yantra-app-launcher)
- See also: [Security & Privacy guide](guides/security-and-privacy.md), [FAQ](faq.md)
