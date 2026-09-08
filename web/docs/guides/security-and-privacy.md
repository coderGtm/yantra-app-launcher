# Security & Privacy

Yantra Launcher connects the command line to parts of your phone that are normally hidden behind icons. That is powerful, and power deserves a little awareness.

## AI Keys and Providers

Your AI API key is stored in the launcher's configuration and is sent to the provider you configure. Treat it like a password. Do not paste it into a script, issue, screenshot, or public theme file. Provider billing, retention, and privacy policies are controlled by that provider, not by Yantra Launcher.

## Location and Contacts

The `location` command uses Android location services and may use cached or network-provided data. The `call` and contact-list features require contact or phone-related access. Grant these permissions only if you want to use those features.

## Private Browsing

`gupt` provides an in-app private browsing experience and clears its browsing data when the tab is closed. Private browsing is not invisibility: your network, DNS provider, visited websites, or the Android system may still observe activity.

## External Services

Weather, dictionary, boredom, translation, search, speedtest, wallpaper, and AI commands contact third-party services. Avoid sending sensitive information in queries unless you have checked the service's privacy policy.

## Backups and Scripts

Backups may contain personal configuration such as aliases, scripts, themes, and settings. Store backup files somewhere you trust. Scripts can execute commands with the permissions available to Yantra Launcher, so only run scripts you understand.
