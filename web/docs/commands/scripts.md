# scripts

The `scripts` command lets you create your own Lua scripts or a list of Yantra Launcher commands that will be executed command-by-command. Same command, two flavors.

!!! note
    Scripts are stored internally by Yantra Launcher. You can edit a script in the launcher or choose an external editor from its edit dialog.

## Syntax
```
scripts
scripts -new <name>
scripts <name>
scripts -rm <name>
```

Use `scripts` to list scripts, `scripts -new <name>` to create one, `scripts <name>` to edit one, and `scripts -rm <name>` to delete one.

Creating or editing a script opens a choice between Yantra Launcher's editor and an external editor. The script is stored inside Yantra Launcher either way. `scripts <name>` edits a script; it does not run it.

## Running scripts

Use the `run` command to execute a saved script:

```
run <name>
run -clean <name>
run -lua <name>
```

Regular scripts contain one Yantra Launcher command per line. `run <name>` executes those lines in order and prints their command output; `run -clean <name>` executes them without printing the command log. Lua scripts must be started with `run -lua <name>` so they are passed to the Lua interpreter instead of being treated as terminal commands.

Lua scripts can use `print()` for output, `input()` for user input, and `binding.exec("command")` to run a Yantra Launcher command from Lua.

## Examples

Simple Lua code example:

```Lua
print("Hello, World!")
```

A game:
```Lua
math.randomseed(os.time())
local secretNumber = math.random(1, 100)
local guess = nil
local attempts = 0

print("Guess the number between 1 and 100!")

while guess ~= secretNumber do
    print("Enter your guess: ")
    guess = tonumber(input())  -- You need to use input instead of io.read()
    attempts = attempts + 1
    
    if guess < secretNumber then
        print("Too low!")
    elseif guess > secretNumber then
        print("Too high!")
    else
        print("Congratulations! You guessed the number in " .. attempts .. " attempts.")
    end
end
```

Yantra command-by-command script
```Yantra
termux python hs.py HELLO
launch discord
echo hi
```

## Notes about Lua:

You need to use `input()` instead of `io.read()` for input in Yantra Launcher. To execute Lua, use `run -lua <name>`.
