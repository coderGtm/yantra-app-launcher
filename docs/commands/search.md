# search

Query search engines then open the link on your default browser.

!!! tip
    You can use [`alias`](../alias) feature to set your favorite engine as the
    default.

## Syntax
```
search -e=<engine_name> -u=<URL>
```
## Options
=== "`-e` (engine)"
    Specify the search engine.

    **Default**: `google`

    **Available Search Engines**:

    - `google`
    - `duckduckgo`
    - `brave`
    - `bing`
    - `yahoo`
    - `ecosia`
    - `startpage`
    - `qwant`
    - `you`
    - `playstore`
    - `maps`
=== "`-u` (url)"
    If your favorite search engine is not listed for `-e` flag, you can use this flag
    to provide a custom search engine to use.

    Your search query will be inserted into the end of the URL.

    **Example**:

        - `-u=https://example.com/search?q=`

    !!! info
        Queries are URL-encoded before substitution.

## Examples

=== "Example I"
    ``` title="Searching With Default Engine"
    search Yantra Launcher
    ```
=== "Example II"
    ``` title="Searching With Another Search Engine"
    search -e=duckduckgo Yantra Launcher
    ```
=== "Example III"
    ``` title="Using Custom Search Engines"
    search -u=https://example.com/search?q= Yantra Launcher
    ```
