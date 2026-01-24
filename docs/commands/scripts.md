# Scripts

The `scripts` command allows you to create and manage automation scripts inside **Yantra Launcher**. Scripts can either:

- Execute Yantra commands sequentially, or
- Run custom code written in **Lua**

!!! note
    The scripts directory is internal to Yantra Launcher.
    Script files **cannot be accessed directly** through the filesystem.
    However, you can **edit scripts using an External editor** through Yantra Launcher, even though direct file access is still restricted.

## Syntax
```text
scripts
```

This command just open a dialog in which you can edit current scripts or create a new one.

To learn how to execute scripts, run:
```text
help run
```

## Script Types

Yantra Launcher supports two types of scripts:

- **Yantra scripts** — command-by-command automation
- **Lua scripts** — programmable scripts using Lua

## Yantra Scripts

Yantra scripts consist of Yantra Launcher commands executed sequentially, one per line.

### Example

```yantra
termux python hs.py HELLO
launch discord
echo hi
```

Each line is executed in order, just as if it were typed directly into Yantra Launcher.

## Lua Scripts

Lua scripts allow more advanced logic, input handling, and automation.

### Simple Example

```lua
print("Hello, World!")
```

### Example: Guess the Number Game

```lua
math.randomseed(os.time())
local secretNumber = math.random(1, 100)
local guess = nil
local attempts = 0

print("Guess the number between 1 and 100!")

while guess ~= secretNumber do
    print("Enter your guess: ")
    guess = tonumber(input()) -- Use input() instead of io.read()
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

### Lua API Reference

The following methods are available inside Lua scripts:

| Method         | Description                                                    |
| -------------- | -------------------------------------------------------------- |
| `input`        | Requests input from the user                                   |
| `print`        | Displays output in Yantra Launcher                             |
| `binding.exec` | Executes Yantra Launcher commands                              |
| `http`         | Provides HTTP methods: `get`, `post`, `put`, `delete`, `patch` |

## Lua Notes

* Use `input()` instead of `io.read()` to receive user input.

