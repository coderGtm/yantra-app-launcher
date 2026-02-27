# launch

This is the most basic command and also the most used command in Yantra Launcher. As the name suggests, it is used to launch apps in different ways. Apart from apps, it can even launch app shortcuts.

## Syntax
```
launch [-p/-s] app_name/package_name/shortcut_name
```

## Examples
To launch any app by its name, you can start typing the name of the app and select it from the suggestions bar (if you have kept it enabled). Also, you can configure Yantra Launcher to directly launch an app if it is the last remaining option. See `settings`.
```
launch whatsapp
```
You can also launch an app by its package name if needed, by providing the `-p` flag. This can be particularly useful when there are 2 apps with the exact same name and you don't want to face a selection popup everytime. Aliasing this command can be very useful in that case.
```
launch -p com.xyz.gallery
```
Also, you can directly launch app shortcuts (the ones that you see when you long-press an app icon on traditional GUI app launchers) using the -s flag You can list all shortcuts using the `list shortcuts` command. For example, this command will launch the Spotify screen that shows songs tailored for you.
```
launch -s Made for you
```
