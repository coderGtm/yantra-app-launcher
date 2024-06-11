


# Yantra Launcher FAQs

## 1. How to integrate Yantra Launcher with Termux?
Ensure that termux is installed on your device. Follow the steps below to integrate Yantra Launcher with Termux:
1. Open Yantra Launcher.
2. Type any termux command to invoke the Permission Dialog. For example, enter
   ```
   termux top
   ```
3. Click on **Allow** in the permission dialog.
4. Now, manually launch the termux app. You can do it via the following Yantra Launcher command:
   ```
   launch termux
   ```
5. Set `allow-external-apps` property to `true`, by entering the following code in termux:
   ```
   value="true"; key="allow-external-apps"; file="/data/data/com.termux/files/home/.termux/termux.properties"; mkdir -p "$(dirname "$file")"; chmod 700 "$(dirname "$file")"; if ! grep -E '^'"$key"'=.*' $file &>/dev/null; then [[ -s "$file" && ! -z "$(tail -c 1 "$file")" ]] && newline=$'\n' || newline=""; echo "$newline$key=$value" >> "$file"; else sed -i'' -E 's/^'"$key"'=.*/'"$key=$value"'/' $file; fi
   ```
   *Source: https://github.com/termux/termux-tasker?tab=readme-ov-file#allow-external-apps-property-optional*

6. Now, you can easily execute Termux commands from your Yantra Launcher terminal. If you get an error though saying something about Termux not running in background, follow the steps mention here: https://github.com/coderGtm/yantra-app-launcher/issues/5#issuecomment-1778961986

*Feel free to open an issue or discuss in the [Discord community](https://discord.gg/sRZUG8rPjk) if you still face any problems!*


## 2. How to get the `ai` command working?
Getting ChatGPT like responses in your Home Screen Terminal is so cool that you don't wanna miss this feature. But to get it working, you need to follow some steps:

1. Choose an AI Provider. It could be anything that conforms to the ChatGPT API Response style. You can even use a paid OpenAI API in Yantra Launcher. Free alternatives may be found here: https://github.com/zukixa/cool-ai-stuff.
2. Get the base domain of the service. For example, Naga AI's API url is https://api.naga.ac/v1/chat/completions. So the base domain here would be "api.naga.ac". Enter this base domain only in the Yantra Launcher Settings by using the `settings` command.
3. Next, get the API Key. This is the most important part. How you get an API key can differe from one service provider to other, so it's best to check out their specific instructions. For example, to get Naga AI's API Key, you need to join their [Discord server](https://discord.naga.ac/) and send a message there. For more assistance you may ask in their server or in [Yantra Launcher's community server](https://discord.gg/sRZUG8rPjk).
4. Enter this API Key in Yantra Launcher's Settings.
5. If you followed all the steps correctly and your service provider is working properly then you can now use the `ai` from your Yantra Launcher terminal.Test it by sending a message like:
   ```
   ai Hello World
   ```
*Feel free to discuss in the [Discord community](https://discord.gg/sRZUG8rPjk) if you still face any issues!*

## 3. What is the 'gupt' command?
G.U.P.T stands for Get Undercover Private Tab. Tired of going to browser everytime and opening Incognito Tab. With GUPT command, you get a built-in Private incognito Tab. Example: 'gupt https://www.youtube.com'. Use without url to default to https://www.google.com. Salient features of G.U.P.T:

1. Launch a private browsing tab inside Yantra Launcher.
2. All the data is cleared after closing the tab.
3. You can also open a specific url in the private tab.
4. Hidden from the recent apps list.
5. No history is saved.
6. No cookies are saved.
7. No more going through the hassle of opening an incognito tab in your browser.