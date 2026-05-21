# setclr

The `setclr` command allows you to change the terminal text color to a specific hex code, overriding the current theme.

## Syntax
```
setclr <color>/-1
```

## Usage
- `setclr <8_digit_hex_code>`: Sets the terminal text color to the specified 8-digit hex code (without the #).
- `setclr -1`: Resets the terminal text color to the theme's default color.

## Example
```
setclr FF00FF00
setclr -1
```
