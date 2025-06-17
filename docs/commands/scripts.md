# scripts

This is a command which allows you to create your own script on ```Lua``` language, or create the list of yantra's commands which will be executing command-by-command

!!! note
    Currently, the scripts folder is internal to Yantra Launcher and cannot be accessed or edited directly using external text editors or file management applications.

## Syntax
```
scripts
```

This command just open a dialog in which you can edit current scripts or create a new one.

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
