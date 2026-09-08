# scripts

This is a command which allows you to create your own script on ```Lua``` language, or create the list of yantra's commands which will be executing command-by-command

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

You need to use input instead of io.read()
TO exe
