# Termux Setup

The `termux` command lets Yantra Launcher hand a command to Termux. It is a wonderfully useful combination, provided the two apps are allowed to talk to each other.

## Requirements

Install Termux from a supported source such as [F-Droid](https://f-droid.org/packages/com.termux/) or the [official GitHub releases](https://github.com/termux/termux-app/releases). The version from another source may not behave the same way.

Yantra Launcher also needs the Termux Run Command permission. The first time you use a Termux command, Yantra Launcher should ask for it.

## Enable External Commands

Termux must allow external applications to run commands. In Termux, run:

```sh
mkdir -p ~/.termux
printf 'allow-external-apps=true\n' >> ~/.termux/termux.properties
```

If the setting already exists, edit the file instead of adding a second copy. Then fully open Termux once before trying Yantra Launcher again:

```text
termux echo Hello from Termux
```

You can also start it from Yantra Launcher with:

```text
launch termux
```

## If It Says Termux Is Not Running

Android manufacturers often stop background applications aggressively. Open Termux manually, disable battery optimization for both apps if your device offers that setting, and try again. The [Termux troubleshooting issue](https://github.com/coderGtm/yantra-app-launcher/issues/5#issuecomment-1778961986) has additional device-specific notes.

The result is returned after Termux finishes the command. Long-running commands therefore keep the Yantra Launcher command flow waiting until Termux exits.
