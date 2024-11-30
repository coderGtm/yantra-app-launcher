# web

The `web` command is used to open a web page in your default web browser.

## Example I
```
web https://reddit.com
```

Specifying the protocol is not necessary and the launcher will default to prefixing `http://` in that case. Most websites will automatically reroute http traffic to https if it's available so you can safely ignore the protocol.

## Example II
```
web reddit.com
```

??? note
    This command will only work if you have a browser installed on your device which handles requests to open URLs. Ofcourse, how will it work without a "web" browser?