# ai

The `ai` command is used to access ChatGPT-like AI models from Yantra Launcher.

## Syntax 
```
ai [message/reset]
```

## Setup
To use the `ai` command, you need 2 things:

1. An AI provider, and
2. An API Key
3. _(Optional) System prompt_

An AI provider is a service that follows the [OpenAI API Reference](https://platform.openai.com/docs/api-reference/making-requests). Although you can use the ChatGPT API directly, keep in mind that it is a paid service.

!!! note
    There are numerous free AI API providers that are compatible with the command as they follow the OpenAI standard. Some examples include NagaAI and Zukijourney.

The API Key is provided by the respective AI Service provider. The System Prompt is a type of instruction given to the AI model to follow for the entire conversation. Try playing with it to get interesting responses. Enter all these in the page opening by using the `settings` command.

Try out a simple message like
```
ai hi
```
If a response is received from the AI model then you have successfully setup the command!

## Usage
Now you can have a great conversation with your AI assistant right from your home screen!
```
ai why should i use a command line launcher?
```
Use the `ai reset` command to clear the conversation history from Yantra Launcher
!!! warning
    Keep in mind that the API you are using is charged by your provider based on the tokens used. The more you chat with `ai`, the more tokens are used as the entire conversation is being evaluated by the model. So, to keep usage minimum, consider using the `ai reset` command to clear the conversation history from Yantra Launcher when you no longer need the model to remember past conversations.