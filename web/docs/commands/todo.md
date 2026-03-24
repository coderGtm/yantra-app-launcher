# todo

The `todo` command is a simple utility to manage your tasks.

## Syntax
```
todo [task / index / -1 / -p]
```

## Usage
- `todo`: Lists all your tasks with their indexes and progress.
- `todo <task>`: Adds a new task to your list.
- `todo <index>`: Marks the task at the specified index as completed.
- `todo -1`: Clears your entire TODO list.
- `todo -p <index> <progress>`: Marks a specific task as a percentage done (e.g., `todo -p 0 50`).
- `todo -p -1`: Resets progress for all tasks.

## Example
```
todo Buy groceries
todo 0
todo -p 1 75
```
