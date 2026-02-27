# call

The `call` command is used to make a phone call to any contact or phone number 📞.

!!! note
    You will need to provide the Calling permission to Yantra Launcher in order to make calls. When you issue this command without the permission, Yantra Launcher will ask you for the permission via a system prompt.

!!! tip
    In order to call a contact saved in your Contacts, Yantra Launcher needs access to read these contacts. You can quickly set this up by using the `list contacts` command which will ask you for the permission. Once granted, you should again use the command so that Yantra Launcher can read the list.

## Calling a saved contact

You can quickly call any of your contact by just specifying the contact name after the `call` command.

### Example

```
call Mumma
```

If multiple phone numbers exist for a given contact name, a popup dialog will be shown to select the number that you want to dial.

!!! tip
    You can avoid the hassle of selecting a phone number that you frequently call from a popup by making an alias to directly call the specified phone number.

## Directly calling a phone number

You can also directly call a raw phone number by providing it as the parameter to the call command.

### Example

```
call 1234567890
```

!!! warning
    If you provide a contact name and it is not in your contact list, Yantra Launcher will consider it as a raw phone number input and Android will parse it to call the parsed number.
