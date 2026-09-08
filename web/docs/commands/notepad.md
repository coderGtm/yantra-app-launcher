# notepad

The `notepad` command is a simple in-app utility for taking quick notes.

## Syntax
```
notepad [list / new <name> / read <name> / delete <name> / edit <name>]
```

## Usage
- `notepad list`: Displays all your saved notes.
- `notepad new <note_name>`: Creates a new note with the specified name.
- `notepad read <note_name>`: Displays the content of an existing note.
- `notepad edit <note_name>`: Opens the editor to modify a note's content.
- `notepad delete <note_name>`: Deletes the specified note.

Note names are entered as one command-line word, so they cannot contain spaces. Avoid commas too: Yantra Launcher uses commas internally to store the note list, and a comma in a name can make that list ambiguous. `TodoToday` and `shopping_list` are good names; `shopping list` is not.

## Example
```
notepad new TodoToday
notepad read TodoToday
```
