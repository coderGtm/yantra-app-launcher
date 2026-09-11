# list

The `list` command displays information about various components in Yantra Launcher.

## Syntax
```
list [apps [-p] [category]/themes/contacts/shortcuts]
```

## Usage
- `list apps`: Fetches and lists all installed applications, grouped by category. Shows app names only.
- `list apps -p`: Lists all installed applications grouped by category, including package names.
- `list apps <category>`: Lists applications in one category (e.g. `list apps Social`, `list apps Games`). Category names are case-insensitive.
- `list apps <category> -p`: Lists applications in one category, including package names (e.g. `list apps Social -p`).
- `list themes`: Displays all available themes, including built-in and custom ones.
- `list contacts`: Fetches and lists your contacts (requires contacts permission).
- `list shortcuts`: Lists available app shortcuts.
