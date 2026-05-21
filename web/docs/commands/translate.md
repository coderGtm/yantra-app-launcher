# translate

The `translate` command uses Google Translate to translate text from one language to another.

## Syntax
```
translate <-language_code> <text>
```

## Usage
- `translate -<lang_code> <text>`: Translates the provided text into the language specified by the code. The source language is detected automatically.

## Example
```
translate -fr Hello
translate -es How are you?
```

!!! tip
    Use standard two-letter ISO language codes (like `fr` for French, `es` for Spanish, `hi` for Hindi, etc.).
