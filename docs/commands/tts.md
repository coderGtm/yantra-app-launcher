# tts

The `tts` command stands for Text To Speech. As the name suggests, it is used for converting a text input to an audio output.

## Usage
```
tts <text goes here>
```

Internally, it uses the Google Speech Synthesis engine for converting the text to audio. The engine is invoked for the period of the synthesis and then shut back down to preserve resources.

## Example
```
tts Yantra Launcher is cool
```
