# yantra-app-launcher

![Yantra Launcher Store Cover](https://github.com/coderGtm/yantra-app-launcher/assets/66418526/1e998174-5481-4b5d-96bb-3ebc7e4d857e)

A Minimal CLI-based Android App Launcher.

---

[**Documentation**](https://codergtm.github.io/yantra-app-launcher/docs/) | [**Discord**](https://discord.gg/YW7hYbPrTh) | [**Reddit**](https://www.reddit.com/r/YantraLauncher/) | [**Play Store**](https://play.google.com/store/apps/details?id=com.coderGtm.yantra)

## Features

- **Blazing Fast:** Launch apps, call contacts, or search the web in seconds—no more digging through app drawers.
- **Productivity First:** Built-in tools for notes, todos, alarms, and calculators keep you in the flow.
- **Deeply Customizable:** From themes and fonts to custom aliases and scripts, make the terminal truly yours.
- **Power User Ready:** Execute Lua scripts, integrate with Termux, and automate your daily tasks with `init` scripts.

## Variants

Yantra Launcher is available in 2 variants:

1.  **Minimal:** For those who want the bare minimum set of commands and features.
2.  **Pro:** Includes a wide array of useful commands and features. Priced to support the project.

## Pre-built Official Versions

Pre-built packages are recommended for most users to ensure Google Play Services compatibility.

- [**Minimal Version**](https://play.google.com/store/apps/details?id=com.coderGtm.yantra)
- [**Pro Version**](https://play.google.com/store/apps/details?id=com.coderGtm.yantra.pro)

## Documentation

For a detailed guide on how to use Yantra Launcher, its commands, and customization options, visit our official documentation:

👉 [**https://codergtm.github.io/yantra-app-launcher/docs/**](https://codergtm.github.io/yantra-app-launcher/docs/)

## Building

> [!NOTE]
> Building and running locally might not give you the smoothest experience. Play Store builds are optimized for your device and language settings and are much smaller in size.

If you are new to Android development, follow these steps to build the project on your machine:

### 1. Prerequisites
- **Android Studio:** Download and install the latest version of [Android Studio](https://developer.android.com/studio).
- **Git:** Ensure you have [Git](https://git-scm.com/) installed to clone the repository.

### 2. Clone the Repository
Open your terminal or command prompt and run:
```bash
git clone https://github.com/coderGtm/yantra-app-launcher.git
```

### 3. Open the Project
1.  Launch **Android Studio**.
2.  Select **Open** and navigate to the folder where you cloned the repository.
3.  Wait for Android Studio to finish "indexing" and "Syncing Gradle" (this might take a few minutes the first time).

### 4. Configuration (`local.properties`)
Android Studio generates a `local.properties` file in the root directory of the project. You need to add some environment variables here for the app to function correctly:

1.  Open `local.properties` in the project root.
2.  Add the following lines:
    ```properties
    weatherAPIkey="your-weather-api-key"
    backupPassword="your-backup-password"
    ```
    *Note: You can leave these as-is, or get an API key from [weatherapi.com](https://www.weatherapi.com/) to use the weather feature.*

### 5. Select a Build Variant
Yantra has different "Variants" (Free and Pro). To choose one:
1.  Locate the **Build Variants** tab (usually found on the bottom-left edge of Android Studio).
2.  Click it and select a configuration from the dropdown for the `:app` module:
    - `freeDebug`: Standard minimal version for testing.
    - `proDebug`: Pro version with all features for testing.

### 6. Run the App
1.  Connect your Android device via USB (with **USB Debugging** enabled in Developer Options) or start an Emulator.
2.  Click the green **Run** (Play) button in the top toolbar of Android Studio.

## Community & Support

- **Discord:** [Join our Community](https://discord.gg/YW7hYbPrTh)
- **Reddit:** [r/YantraLauncher](https://www.reddit.com/r/YantraLauncher/)
- **Support:** See [support.md](/support.md) for ways to support the project.
- **FAQs:** Visit the [FAQs Page](https://codergtm.github.io/yantra-app-launcher/faq/) for quick answers.

## Contribution

- Please **open an issue** before developing large changes to discuss the implementation.
- Follow the existing coding style for consistency.
- **Translation:** We welcome help translating Yantra! See the [Translator's Guide](/Yantra_Launcher_Translators_Guide.pdf).

## Star History

<a href="https://star-history.com/#coderGtm/yantra-app-launcher&Date">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=coderGtm/yantra-app-launcher&type=Date&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=coderGtm/yantra-app-launcher&type=Date" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=coderGtm/yantra-app-launcher&type=Date" />
 </picture>
</a>

## Contributors

<a href="https://github.com/coderGtm/yantra-app-launcher/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=coderGtm/yantra-app-launcher" />
</a>

<br/><br/>

***LIVE THE CLI EXPERIENCE 😍***
