# location

The `location` command displays your current geographical information.

## Syntax
```
location [-refresh]
```

## Usage
- `location`: Shows address, coordinates (latitude, longitude), accuracy, and the source of the data.
- `location -refresh`: Skips cached data and requests a fresh update.

Without `-refresh`, Yantra Launcher first uses the most recent cached GPS or network location. If no cached location is available, it requests a fresh location instead. A fresh request prefers an enabled GPS provider, but can fall back to the network provider; it may time out if no provider responds.

The output also tells you whether the result came from GPS or network data, whether it was cached or fresh, and how old it is. The address is shown when Android's geocoder can provide one.

!!! requirement
    Location permissions must be granted to Yantra Launcher for this command to function.
