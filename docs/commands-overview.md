# Commands Overview
This page provides a quick overview of all commands in Yantra Launcher that you can glance. It does not try to explain every concept in detail. For getting a detailed perspective, see the individual command documentation.

!!! note
    You may not see all these commands in the Minimal version of Yantra Launcher.

## launch [-p/-s] [app/package name/shortcut label]
Launches specified app or shortcut. Example: 'launch Chrome'. Use the '-p' flag to launch app by package name. Example: 'launch com.android.chrome'. Use the '-s' flag to launch a Shortcut. Example: 'launch -s Chats'
## help | help [command_name]
Documentation/Manual for all commands of Yantra Launcher. Use 'help cmd_name' to get documentation for specific command.
## community [discord|reddit]
Redirects to the Discord server of Yantra Launcher. Here you can share and get Feedback, Suggestions, Insights, tips and CLI emotions from other users like you! Use the optional 'discord' or 'reddit' parameters to navigate to a specific platform.
## theme <name>
Applies specified theme to Yantra Launcher. Example: 'theme Tokyonight'
## call [name | number]
Calls specified contact name. If contact name is not found then the raw input (considered as phone number) is called.
## bluetooth [state]
Toggles bluetooth on/off. Example: 'bluetooth on' or 'bt 0'
## flash [state]
Toggles flashlight on/off. Example: 'flash on' or 'flash 0'. Use without any args to toggle the state.
## internet
Opens a panel containing settings to enable internet connection.
## ai [message/reset]
A simple tool to access chatGPT from the terminal. Based on OpenAI's gpt-3.5-turbo, you can chat with your own AI assistant. An API key is required to be entered in 'settings'. When you want to erase the message history to save tokens, you can use the 'reset' message ('ai reset').
## todo
A simple TODO utility. Use 'todo' to get list of tasks with their indexes and progress. Add a task like 'todo Go for a brisk walk'. Mark tasks as done using their index returned from 'todo' command, like 'todo 2' marks the 3rd task as done. Use 'todo -1' to clear list.

Use the -p argument to mark optional progress for any task, like 'todo -p 1 30' marks the 2nd task as 30% done (Syntax: todo -p taskIndex progress). Use 'todo -p -1' to reset progress for all tasks.
## alias
Unix-like aliasing system to set short-hand commands for available commands. Note that Yantra Launcher saves all aliases in memory and is retained even after restarting the Launcher session. Pre-defined commands can not be aliased.
Usage (to set or update an alias): 'alias alias_name = alias_cmd'
Example: 'alias h = help'
Use 'alias alias_name' to get value of the alias. Use 'alias' to get list of all current aliases. Use 'alias -1' to reset to default aliases.
## weather <location>
Fetches the weather report for specified location. Example w new delhi
## username <new_username>
Used to change username. Example: username johnDoe
## pwd
Prints the current/working directory.
## cd [path]
Changes the current directory to the specified path.
Example: 'cd DCIM'
## ls [-a]
Lists contents in the current directory. Optionally, pass the '-a' flag to also show hidden files/folders.
## open [file name]
Opens specified file. Example: 'open certificate.pdf'
## search
Searches the internet for the provided query. Search engine can be specified with the -e flag (-e=google|duckduckgo|brave|bing|yahoo|ecosia|startpage|qwant|you|playstore). Default is google. You can use a custom search engine by specifying the url with the -u flag (-u=https://example.com/search?q=). The query is the only required argument that is provided at the end of the command. It is automatically URL encoded during execution of the command. Examples:
'search Yantra Launcher'
'search -e=duckduckgo Yantra Launcher'
'search -u=https://example.com/search?q= Yantra Launcher'
## web <url>
Opens the specified URL in your browser, if present, ofc!
## GUPT | GUPT [url]
G.U.P.T stands for Get Undercover Private Tab. Tired of going to browser everytime and opening Incognito Tab. With GUPT command, you get a built-in Private incognito Tab. Example: 'gupt https://www.youtube.com'. Use without url to default to https://www.google.com
## tts <text string>
Speaks provided text (Text-to-Speech). Example: 'tts Travel, World!'
## sfx
Play sound effects
## news
Opens the news website configured via settings. Defaults to Google News
## bored
Helps you find things to do when you're bored! (using Bored API)
## time [utc] [GMT]
Shows current local Date and Time. Use the utc arg to get UTC time. An optional time difference parameter can add or subtract that from UTC time. Example:
time
time utc
time utc +5:30
## alarm [time] [message]
Sets alarm using a supported Alarm Clock on your device. Use without args to open the list of alarms on your device default app. The time must be in 24 hr format. It can optionally be followed by a message string to display in alarm.
Examples:
'alarm 14:30' sets alarm for 2:30 pm
'alarm 0:15 Wish good night to ***' sets alarm for 12:15 am with a message to display.
## timer [length] [message]
Used to create a countdown timer using the default app on your device. Use without args to launch the timer app. The length must be in seconds and ranges from 1 to 86400 (24 hrs). It can optionally be followed by a message string to display in timer.
Examples:
'timer 60' sets timer for 1 minute
'timer 3600 Take out the trash' sets timer for 1 hour with a message to display.
## settings
Launches Settings for Yantra Launcher.
## sysinfo | sysinfo -component
Displays system information, much like 'Neofetch'
## screentime [app-name][-all]
Shows Total Screen time for the day! Give app name to get screen time for particular app, or use the '-all' flag to get screentime for all apps used today.
Example: 'screentime Instagram' or 'screentime -all'
## scripts
Opens dialog for creating, modifying and deleting custom scripts for Yantra Launcher, to execute multiple commands at once. Scripts can have Yantra Launcher commands in it. But you can also create exclusive Lua scripts for extended functionality. If you are using Lua code, you can even call Yantra Launcher commands from the script using the binding.exec() function.
Example: 'binding.exec(text Scripts are cool!)'. Also see the 'run' command.
## quote
Displays a random quote! What else do you expect?
## bg | bg random [-id=53] [-grayscale] [-blur=2]
'bg' is used to set custom Wallpaper from the Albums. Use 'bg -1' to remove custom Wallpaper and set to solid theme background. Use 'bg random' to fetch a random Wallpaper from the picsum.photos. You can fetch a specific image by passing its id as a flag parameter (full list at https://picsum.photos/images). An optional -grayscale flag is used to get a grayscale image. Get a blurred image by providing the blur flag with intensity from 1 to 10. Note that this command only changes the Home screen wallpaper, not the Lock screen one.
## text <msg>
Broadcasts text message. Example: 'text Yantra is cool!'
## translate <-language> <text>
Translator based on Google Translate. Provide a valid language code flag and the text to translate. The source language is automatically detected. Example:
translate -fr Hello
## music [play/pause/prev/next]
Control the local music state on your device. Use without any parameters to toggle the music state. Use 'play' to resume music, 'pause' to pause music, 'next' to play the next track and 'prev' to play the previous track in the queue.
## echo [-mode] <text>
Prints specified text to the terminal with the given mode. Here mode is an optional argument (e: Error text, s: Success text, w: Warning text) representing the nature of the text output. If mode is not specified, the text is printed (normal)ly
Examples:
'echo -e An error occurred.'
'echo Hello, World'
## speedtest
Opens a small GUI speedtest utility to check your internet speed, powered by openspeedtest.com.
## notify <message>
Fire a notification with the given message.
Example: notify Opened work apps
## calc [expression]
An in-built calculator to evaluate arithmetic expressions. It does addition, subtraction, multiplication, division, exponentiation (using the ^ symbol), and a few basic functions like sqrt, sin, cos and tan. It supports grouping using (...), and it gets the operator precedence and associativity rules correct.

Example: 'calc ((4 - 2^3 + 1) * -sqrt(3*3+4*4)) / 2' gives 7.5
## email <email-id>
Launches email app with recipient set. Example: 'email coderGtm@gmail.com'
## sleep <millis>
Pauses Yantra Launcher for specified milliseconds.
Usage: 'sleep numOfMilliseconds'
Example: 'sleep 5000'
## vibe <millis>
Vibrates the device for specified duration (in milliseconds).
Example: 'vibe 1000' does a 1 second vibration.
## init
A special script (function) to execute specified commands automatically whenever Launcher is opened or navigated to.
## launchf [approx app name]
Launches app by matching given app name string using fuzzy search algorithm (Levenshtein distance). Example: 'launchf tube' may launch YouTube.
## info [-p] [app/package name]
Launches app settings page for specified app. Example: 'info Big Battery Display' or 'i Farty Orbit'. Use the optional '-p' flag to launch app settings by package name. Example: 'info com.android.chrome'
## infof [approx app name]
Launches app settings by matching given app name string using fuzzy search algorithm (Levenshtein distance). Example: 'infof tube' may open system settings for YouTube.
## uninstall <app name>
Uninstalls the specified app. Example: 'u Instagram'
## list [component]
Lists specified component [apps/themes/contacts].
## unalias
Used to un-alias (remove) an alias.
Usage: 'unalias alias_name'
Example: 'unalias h'
Use 'unalias -1' to remove all aliases
## termux <cmd> [args]
Runs given command in termux if it is installed and logs the stdout generated. Note that the 'allow-external-apps' property must be set to true in ~/.termux/termux.properties in Termux app and RUN_COMMAND permission must be given from Yantra Launcher's settings page. Also, the result is returned only after the command is fully executed (exited) due to API restrictions.
Usage: termux cmd_name [args]
Example: 'termux echo Hello from Termux'
To resolve any issues still occurring, ask in Discord Community or mail me at coderGtm@gmail.com.
## run script_name
Used to run your custom scripts. Use the '-lua' flag to run the script as Lua code. Normal scripts can be run with the '-clean' flag to not print the command logs while execution and just log the output of the commands.
## backup [-i]
A utility to backup your Yantra Launcher configuration. Using the command will export your current configuration to a file. If you use the -i flag, you can import a configuration file that will replace your current configuration.
## dict [-urban] <word>
Search for the meaning of a word in an online dictionary (freeDictionaryAPI). Use the '-urban' flag to search in Urban Dictionary!
## history
Shows all executed command
## battery
Shows current Battery Level. Use the optional '-bar' argument to show just the battery percentage and charging status in visual form.
## lock
Applies Screen Lock to your Yantra (device). Note: Appropriate permissions are required.
## clear
Clears the console
## reset
Restarts the console (Launcher) completely.
## cmdrequest
In case you want any new commands to be added to Yantra Launcher, run this command to open the GitHub issue template for command request. Note that it is not guaranteed that the command request will be accepted but I'll try my best to see if I could accommodate it in the upcoming versions of Yantra Launcher, if suitable.
## feedback
Please provide your valuable feedback and any features you want in the next release of the app. Consider giving a 5 star review on the Play Store if you like the app.
## support
About supporting Yantra Launcher.
## exit
Exit Launcher. Note: Launcher will restart if set as default Launcher
