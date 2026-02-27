# Frequently Asked Questions

## 1. How to integrate Yantra Launcher with Termux?

Ensure that termux is installed on your device from github or f-droid. Follow the steps below to integrate Yantra Launcher with Termux:

  1. Open Yantra Launcher.
  2. Type any termux command to invoke the Permission Dialog. For example, enter

    ```
    termux ls
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

    *Source: <https://github.com/termux/termux-tasker?tab=readme-ov-file#allow-external-apps-property-optional>*

  6. Now, you can easily execute Termux commands from your Yantra Launcher terminal. If you get an error though saying something about Termux not running in background, follow the steps mention here: <https://github.com/coderGtm/yantra-app-launcher/issues/5#issuecomment-1778961986>

*Feel free to open an issue or discuss in the [Discord community](https://discord.gg/sRZUG8rPjk) if you still face any problems!*

## 2. How to get the `ai` command working?

Getting ChatGPT like responses in your Home Screen Terminal is so cool that you don't wanna miss this feature. But to get it working, you need to follow some steps:

1. Choose an AI Provider. It could be anything that conforms to the ChatGPT API Response style. You can even use a paid OpenAI API in Yantra Launcher. Free alternatives may be found here: <https://github.com/zukixa/cool-ai-stuff>.
2. Get the base domain of the service. For example, Naga AI's API url is <https://api.naga.ac/v1/chat/completions>. So the base domain here would be "api.naga.ac". Enter this base domain only in the Yantra Launcher Settings by using the `settings` command.
3. Next, get the API Key. This is the most important part. How you get an API key can differ from one service provider to other, so it's best to check out their specific instructions. For example, to get Naga AI's API Key, you need to join their [Discord server](https://discord.naga.ac/) and send a message there. For more assistance you may ask in their server or in [Yantra Launcher's community server](https://discord.gg/sRZUG8rPjk).
4. Enter this API Key in Yantra Launcher's Settings.
5. If you followed all the steps correctly and your service provider is working properly then you can now use the `ai` from your Yantra Launcher terminal.Test it by sending a message like:

   ```
   ai Hello World
   ```

*Feel free to discuss in the [Discord community](https://discord.gg/sRZUG8rPjk) if you still face any issues!*

## 3. What is the 'gupt' command?

G.U.P.T stands for **"Get Undercover Private Tab"**. Tired of going to browser everytime and opening Incognito Tab. With GUPT command, you get a built-in Private incognito Tab. Example:

```
gupt https://www.youtube.com
```

!!! tip
    Use without url to default to <https://www.google.com>.

Salient features of G.U.P.T:

1. Launch a private browsing tab inside Yantra Launcher.
2. All the data is cleared after closing the tab.
3. You can also open a specific url in the private tab.
4. Hidden from the recent apps list.
5. No history is saved.
6. No cookies are saved.
7. No more going through the hassle of opening an incognito tab in your browser.

## 4. How to use Lua scripting in Yantra Launcher?

Lua Scripting is a very powerful feature addition to Yantra Launcher using which you can literally do almost anything within your launcher. The reason being obvious: Lua is an entire programming language which has been embedded in the Launcher. This FAQ assumes that you already know how normal Yantra Launcher scripts work. If you don't know then pleas read the documentation of that command using the `help scripts` command.

Creating a Lua script has the same process as creating a normal Yantra Launcher commands script:

1. Enter the `scripts` command to open the scripts menu.
2. Click on the button to create a new script and enter the script name.
3. Enter the `scripts` command again to open the menu and click on your newly created script to open its editor.
4. Enter the Lua code that you want to execute and click on the Save button.

As you saw, the script creation process is the same. But the difference lies in execution, namely the `run` command. To tell Yantra Launcher that you want to execute a script as Lua code, you need to pass the `-lua` flag to the `run` command. For example, suppose your script name is "jokeNotifier", you can run it using:

```
run -lua jokeNotifier
```

Also, note that the embedded Lua language may not have the modules you are loking for. This is because most modules are written in C while this embedding is based on Java ([LuaJ](http://www.luaj.org/luaj.html)). But worry not, I have included 2 custom modules for the embedding using which you can do amazing things. The first one is the `http` module to create and send HTTP requests and parse their responses. The second one is a `binding` module to execute Yantra Launcher commands from Lua scripts. Also, there are custom `input` and `print` functions to facilitate IO operations in the absence of an stdout. Here are some code snippets to show the usage:

```lua
-- A script to ask for a name and wait for 5 seconds before using a Yantra Launcher command
print("Enter your name:")
name = input()

-- Sleep for 5 seconds
os.execute("sleep 5")

-- Use Yantra Launcher's 'text' command to broadcast greetings
binding.exec("text Hello "..name.."!")
```

```lua
-- Script to notify a random cat fact

-- Make an HTTP GET request
local response = http.get("https://catfact.ninja/fact", {headers={}})

-- Use the 'exec' function of 'binding' to execute a Yantra Launcher command string
binding.exec("notify "..response.body.fact)
```

```lua
-- Example POST request
local postUrl = "https://jsonplaceholder.typicode.com/posts"
local postData = '{"title": "foo", "body": "bar", "userId": 1}'
local headers = {
    ["Content-Type"] = "application/json"
}

-- Can use get, post, put, delete and patch
local response = http.post(postUrl, {body=postData, headers=headers})

-- Check for errors
if response.error then
    print("Error: " .. response.error)
else
    local body = response.body
    print("Response ID: " .. body.id)
    print("Title: " .. body.title)
    print("Body: " .. body.body)
    print("User ID: " .. body.userId)
end
```

I hope these examples are enough to get you started. If that's the case then probably you can now see the extent of power you have on your fingertips while using your Yantra Lancher.

*(Yes, you know what I am saying...AUTOMATE)*
