# weather

The `weather` command fetches and displays the current weather report and forecast for a specified location.

## Syntax
```
weather <location> [-fields]
```

## Usage
- `weather <location>`: Displays a basic weather report for the given location.
- `weather list`: Lists all available weather fields you can use to customize the output.
- `weather <location> -field1 -field2`: Displays the weather for the location with specific fields (e.g., `-temp`, `-humidity`).

## Example
```
weather London
weather "New York" -temp -humidity
```

!!! tip
    Use `weather list` to see all supported fields like `-uv`, `-wind`, `-feels`, etc.
    If your location name has spaces, you can enter it directly or wrap it in quotes.
    All field arguments must come after the location.
