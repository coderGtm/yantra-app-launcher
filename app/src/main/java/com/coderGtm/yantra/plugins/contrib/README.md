# Yantra Plugin Contributions

Source-distributed plugins live here. They are reviewed, built, and shipped with Yantra rather than downloaded at runtime.

## Folder Convention

Use one folder per plugin:

```text
plugins/
  contrib/
    <feature>_<githubUsername>_<version>/
      PluginClass.kt
      optional_helpers.kt
      README.md
      assets/              # optional source-side assets or notes
```

Example:

```text
plugins/contrib/autocomplete_yyppsk_v0_1_0/
```

Use lowercase folder names where possible. Convert semantic versions to Android-friendly names:

```text
v0.1.0 -> v0_1_0
```

## Contributor Checklist

1. Create a plugin folder using the naming convention above.
2. Implement one of the plugin host interfaces from `YantraPlugin.kt`.
3. Keep plugin-specific helpers, models, and documentation inside your plugin folder.
4. Set `author` to the GitHub username or team name that owns the contribution.
5. If runtime assets are needed, place them under `app/src/main/assets/plugins/<plugin_folder>/` and reference that path from your plugin.
6. Register the plugin in `PluginCatalog.kt`.
7. Build `freeDebug` and confirm the plugin appears in Settings > Plugins > Manage Your Plugins.

## Review Criteria

Plugins should avoid network calls, storage access, or UI mutation unless the feature clearly needs it. Keep pure logic in small helpers with JVM tests when possible, and prefer host methods over direct access to activity views.

## Local Testing

Run `./gradlew :app:assembleFreeDebug` before opening a PR. If the plugin has pure logic, add tests under `app/src/test/java/...` beside the plugin package and run the matching `testFreeDebugUnitTest` task.

This shape gives GitHub contributors a clear path without opening the app to unreviewed runtime code.
