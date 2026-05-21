# echo

The `echo` command prints the specified text to the terminal.

## Syntax
```
echo [-mode] <text>
```

## Usage
- `echo <text>`: Prints the text in the normal theme color.
- `echo -e <text>`: Prints the text as an **Error** (usually red).
- `echo -s <text>`: Prints the text as a **Success** message (usually green).
- `echo -w <text>`: Prints the text as a **Warning** (usually yellow).

## Example
```
echo -e An error occurred.
echo Hello, World
```
