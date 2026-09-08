# time

The `time` command displays the current date and time.

## Syntax
```
time [utc] [offset]
```

## Usage
- `time`: Shows the current local date and time.
- `time utc`: Shows the current UTC time.
- `time utc +/-HH:MM`: Shows UTC time with a specified offset. The supported hour range is -12 to +14, with minutes from 00 to 59.

## Example
```
time
time utc +5:30
```
