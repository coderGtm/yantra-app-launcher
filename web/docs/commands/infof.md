# infof

The `infof` command launches app settings using a fuzzy search algorithm.

## Syntax
```
infof [approx app name]
```

## Usage
- `infof <string>`: Matches the given string against installed app names using the Levenshtein distance algorithm and opens the settings for the closest match.

## Example
```
infof tube
```
This might open the system settings for YouTube.
