# tts

The `tts` command stands for Text To Speech. As the name suggests, it is used for converting a text input to an audio output.

## Usage
```
tts <text goes here>
```

Yantra Launcher uses Android's installed Text-to-Speech service for this. That means the actual voice engine is the one selected or provided as the default on your device; it is not necessarily Google TTS. The engine is initialized for the request and shut down after speaking to preserve resources.

## Example
```
tts Yantra Launcher is cool
```
