package com.coderGtm.yantra.activities

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.DEFAULT_TERMINAL_FONT_NAME
import com.coderGtm.yantra.R
import com.coderGtm.yantra.copyFileToInternalStorage
import com.coderGtm.yantra.getFullName
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.misc.changedSettingsCallback
import com.coderGtm.yantra.misc.getAppSugOrderText
import com.coderGtm.yantra.misc.getOrientationText
import com.coderGtm.yantra.misc.openAiApiKeySetter
import com.coderGtm.yantra.misc.openAiApiProviderSetter
import com.coderGtm.yantra.misc.openAiModelSetter
import com.coderGtm.yantra.misc.openAiSystemPromptSetter
import com.coderGtm.yantra.misc.openAppSugOrderingSetter
import com.coderGtm.yantra.misc.openArrowSizeSetter
import com.coderGtm.yantra.misc.openDoubleTapActionSetter
import com.coderGtm.yantra.misc.openFontSizeSetter
import com.coderGtm.yantra.misc.openLauncherSelection
import com.coderGtm.yantra.misc.openNewsWebsiteSetter
import com.coderGtm.yantra.misc.openOrientationSetter
import com.coderGtm.yantra.misc.openPrimarySuggestionsOrderSetter
import com.coderGtm.yantra.misc.openSwipeLeftActionSetter
import com.coderGtm.yantra.misc.openSwipeRightActionSetter
import com.coderGtm.yantra.misc.openSysinfoArtSetter
import com.coderGtm.yantra.misc.openTermuxCmdPathSelector
import com.coderGtm.yantra.misc.openTermuxCmdSessionActionSelector
import com.coderGtm.yantra.misc.openTermuxCmdWorkingDirSelector
import com.coderGtm.yantra.misc.openUsernamePrefixSetter
import com.coderGtm.yantra.activities.helpers.openFontSelector
import com.coderGtm.yantra.activities.helpers.openLanguagePicker
import com.coderGtm.yantra.activities.helpers.openSoundEffectsList
import com.coderGtm.yantra.toast
import com.coderGtm.yantra.ui.components.containers.SettingsScreen
import com.coderGtm.yantra.ui.settings.groups.AiGroup
import com.coderGtm.yantra.ui.settings.groups.AppGroup
import com.coderGtm.yantra.ui.settings.groups.ArrowKeysGroup
import com.coderGtm.yantra.ui.settings.groups.DisplayGroup
import com.coderGtm.yantra.ui.settings.groups.FontGroup
import com.coderGtm.yantra.ui.settings.groups.GesturesGroup
import com.coderGtm.yantra.ui.settings.groups.KeyboardGroup
import com.coderGtm.yantra.ui.settings.groups.OtherGroup
import com.coderGtm.yantra.ui.settings.groups.PromptGroup
import com.coderGtm.yantra.ui.settings.groups.SuggestionsGroup
import com.coderGtm.yantra.ui.settings.groups.TermuxGroup
import com.coderGtm.yantra.ui.theme.YantraTheme
import com.google.android.play.core.ktx.languages
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import java.io.File


class SettingsActivity : AppCompatActivity() {

    private var getPrimarySuggestions        by mutableStateOf(true)
    private var getSecondarySuggestions      by mutableStateOf(true)
    private var useSystemWallpaper           by mutableStateOf(false)
    private var fullscreenLauncher           by mutableStateOf(false)
    private var vibrationPermission          by mutableStateOf(true)
    private var showArrowKeys                by mutableStateOf(true)
    private var showCurrentFolderInPrompt    by mutableStateOf(false)
    private var oneTapKeyboardActivation     by mutableStateOf(true)
    private var hideKeyboardOnEnter          by mutableStateOf(true)
    private var actOnSuggestionTap           by mutableStateOf(false)
    private var actOnLastSecondarySuggestion by mutableStateOf(false)
    private var initCmdLog                   by mutableStateOf(false)
    private var useModernPromptDesign        by mutableStateOf(false)
    private var disableAds                   by mutableStateOf(false)
    private var fontSizeText                 by mutableStateOf("16")
    private var arrowSizeText                by mutableStateOf("65")
    private var orientationText              by mutableStateOf("")
    private var appSugOrderText              by mutableStateOf("")
    internal var fontName                    by mutableStateOf(DEFAULT_TERMINAL_FONT_NAME)
    internal var localeDisplayName           by mutableStateOf("English")
    private var usernamePrefix               by mutableStateOf("$")

    private val isProUser by lazy { isPro(this) }

    internal val supportedLocales = mapOf(
        "English"    to "en",
        "Italiano"   to "it",
        "Русский"    to "ru",
        "Español"    to "es",
        "Українська" to "uk",
        "Српски"     to "sr",
    )

    internal lateinit var splitInstallManager: SplitInstallManager
    internal var splitInstallSessionId = 0
    internal val splitInstallStateListener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == splitInstallSessionId) {
            when (state.status()) {
                SplitInstallSessionStatus.INSTALLED -> {
                    val keys     = supportedLocales.keys.toTypedArray()
                    val langCode = state.languages.firstOrNull() ?: "en"
                    localeDisplayName = keys[supportedLocales.values.indexOf(langCode)]
                    changedSettingsCallback(this@SettingsActivity)
                    Toast.makeText(this, getString(R.string.changed_app_language_to, localeDisplayName), Toast.LENGTH_LONG).show()
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
                }
                SplitInstallSessionStatus.FAILED -> {
                    Toast.makeText(this, getString(R.string.an_error_occurred_please_try_again), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val prefFile = "yantraSP"
    internal val preferenceObject: SharedPreferences
        get() = applicationContext.getSharedPreferences(prefFile, 0)
    internal val preferenceEditObject: SharedPreferences.Editor
        get() = applicationContext.getSharedPreferences(prefFile, 0).edit()

    // ── Activity-result launchers ────────────────────────────────────────────
    internal val selectFontLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val name = getFullName(result.data!!.data!!, this)
            if (name != null && name.endsWith(".ttf")) {
                copyFileToInternalStorage(this, result.data!!.data!!)
                if (File(filesDir, name).exists()) {
                    preferenceEditObject.putString("font", name).apply()
                    fontName = name.replace(".ttf", "")
                    Toast.makeText(this, getString(R.string.terminal_font_updated_to, name), Toast.LENGTH_SHORT).show()
                    changedSettingsCallback(this)
                    return@registerForActivityResult
                }
            } else {
                Toast.makeText(this, getString(R.string.incorrect_file), Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
        }
        Toast.makeText(this, getString(R.string.an_error_occurred_please_try_again), Toast.LENGTH_SHORT).show()
    }

    internal val selectSfxLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val name = getFullName(result.data!!.data!!, this)
            if (name != null && (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg"))) {
                copyFileToInternalStorage(this, result.data!!.data!!)
                if (File(filesDir, name).exists()) {
                    toast(baseContext, "Added sound effect $name")
                    return@registerForActivityResult
                }
            } else {
                toast(baseContext, getString(R.string.incorrect_file))
                return@registerForActivityResult
            }
        }
        Toast.makeText(this, getString(R.string.an_error_occurred_please_try_again), Toast.LENGTH_SHORT).show()
    }
    // ────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        splitInstallManager = SplitInstallManagerFactory.create(this)

        // Load preferences into state
        getPrimarySuggestions            = preferenceObject.getBoolean("getPrimarySuggestions", true)
        getSecondarySuggestions          = preferenceObject.getBoolean("getSecondarySuggestions", true)
        useSystemWallpaper               = preferenceObject.getBoolean("useSystemWallpaper", false)
        fullscreenLauncher               = preferenceObject.getBoolean("fullScreen", false)
        vibrationPermission              = preferenceObject.getBoolean("vibrationPermission", true)
        showArrowKeys                    = preferenceObject.getBoolean("showArrowKeys", true)
        showCurrentFolderInPrompt        = preferenceObject.getBoolean("showCurrentFolderInPrompt", false)
        oneTapKeyboardActivation         = preferenceObject.getBoolean("oneTapKeyboardActivation", true)
        hideKeyboardOnEnter              = preferenceObject.getBoolean("hideKeyboardOnEnter", true)
        actOnSuggestionTap               = preferenceObject.getBoolean("actOnSuggestionTap", false)
        actOnLastSecondarySuggestion     = preferenceObject.getBoolean("actOnLastSecondarySuggestion", false)
        initCmdLog                       = preferenceObject.getBoolean("initCmdLog", false)
        useModernPromptDesign            = preferenceObject.getBoolean("useModernPromptDesign", false)
        disableAds                       = preferenceObject.getBoolean("disableAds", false)
        fontSizeText                     = preferenceObject.getInt("fontSize", 16).toString()
        arrowSizeText                    = preferenceObject.getInt("arrowSize", 65).toString()
        orientationText                  = getOrientationText(this, preferenceObject.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT))
        appSugOrderText                  = getAppSugOrderText(this, preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value))
        fontName                         = if (isProUser) preferenceObject.getString("font", DEFAULT_TERMINAL_FONT_NAME) ?: DEFAULT_TERMINAL_FONT_NAME else DEFAULT_TERMINAL_FONT_NAME
        usernamePrefix                   = getUserNamePrefix(preferenceObject)
        localeDisplayName                = supportedLocales.filterValues { it == AppCompatDelegate.getApplicationLocales().toLanguageTags() }.keys.firstOrNull() ?: "English"

        setContent {
            YantraTheme {
                SettingsContent()
            }
        }
    }

    // ── Compose UI ───────────────────────────────────────────────────────────
    @Composable
    private fun SettingsContent() {
        SettingsScreen(modifier = Modifier.systemBarsPadding().imePadding()) {

            PromptGroup(
                usernamePrefix                  = usernamePrefix,
                onOpenUsernamePrefix            = { openUsernamePrefixSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) { usernamePrefix = it } },
                isProUser                       = isProUser,
                useModernPromptDesign           = useModernPromptDesign,
                onModernPromptDesignChange      = { useModernPromptDesign = it; preferenceEditObject.putBoolean("useModernPromptDesign", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                showCurrentFolderInPrompt       = showCurrentFolderInPrompt,
                onShowCurrentFolderInPromptChange = { showCurrentFolderInPrompt = it; preferenceEditObject.putBoolean("showCurrentFolderInPrompt", it).apply(); changedSettingsCallback(this@SettingsActivity) }
            )

            DisplayGroup(
                useSystemWallpaper        = useSystemWallpaper,
                onUseSystemWallpaperChange = { useSystemWallpaper = it; preferenceEditObject.putBoolean("useSystemWallpaper", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                fullscreenLauncher        = fullscreenLauncher,
                onFullscreenLauncherChange = { fullscreenLauncher = it; preferenceEditObject.putBoolean("fullScreen", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                orientationText           = orientationText,
                onOpenOrientationSetter   = { openOrientationSetter(this@SettingsActivity, preferenceEditObject) { orientationText = it } }
            )

            FontGroup(
                fontSizeText        = fontSizeText,
                onOpenFontSizeSetter = { openFontSizeSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) { fontSizeText = it } },
                isProUser           = isProUser,
                fontName            = fontName,
                onOpenFontSelector  = { openFontSelector() }
            )

            ArrowKeysGroup(
                showArrowKeys        = showArrowKeys,
                onShowArrowKeysChange = { showArrowKeys = it; preferenceEditObject.putBoolean("showArrowKeys", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                arrowSizeText        = arrowSizeText,
                onOpenArrowSizeSetter = { openArrowSizeSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) { arrowSizeText = it } }
            )

            SuggestionsGroup(
                getPrimarySuggestions               = getPrimarySuggestions,
                onGetPrimarySuggestionsChange        = { getPrimarySuggestions = it; preferenceEditObject.putBoolean("getPrimarySuggestions", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                getSecondarySuggestions             = getSecondarySuggestions,
                onGetSecondarySuggestionsChange      = { getSecondarySuggestions = it; preferenceEditObject.putBoolean("getSecondarySuggestions", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                appSugOrderText                     = appSugOrderText,
                onOpenAppSugOrderingSetter           = { openAppSugOrderingSetter(this@SettingsActivity, preferenceEditObject) { appSugOrderText = it } },
                onOpenPrimarySuggestionsOrderSetter  = { openPrimarySuggestionsOrderSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                actOnSuggestionTap                  = actOnSuggestionTap,
                onActOnSuggestionTapChange           = { actOnSuggestionTap = it; preferenceEditObject.putBoolean("actOnSuggestionTap", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                actOnLastSecondarySuggestion        = actOnLastSecondarySuggestion,
                onActOnLastSecondarySuggestionChange = { actOnLastSecondarySuggestion = it; preferenceEditObject.putBoolean("actOnLastSecondarySuggestion", it).apply(); changedSettingsCallback(this@SettingsActivity) }
            )

            GesturesGroup(
                isProUser                    = isProUser,
                onOpenDoubleTapActionSetter  = { openDoubleTapActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                onOpenSwipeRightActionSetter = { openSwipeRightActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                onOpenSwipeLeftActionSetter  = { openSwipeLeftActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
            )

            KeyboardGroup(
                oneTapKeyboardActivation         = oneTapKeyboardActivation,
                onOneTapKeyboardActivationChange  = { oneTapKeyboardActivation = it; preferenceEditObject.putBoolean("oneTapKeyboardActivation", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                hideKeyboardOnEnter              = hideKeyboardOnEnter,
                onHideKeyboardOnEnterChange       = { hideKeyboardOnEnter = it; preferenceEditObject.putBoolean("hideKeyboardOnEnter", it).apply(); changedSettingsCallback(this@SettingsActivity) }
            )

            if (isProUser) {
                TermuxGroup(
                    onOpenPathSelector          = { openTermuxCmdPathSelector(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                    onOpenWorkingDirSelector    = { openTermuxCmdWorkingDirSelector(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                    onOpenSessionActionSelector = { openTermuxCmdSessionActionSelector(this@SettingsActivity, preferenceObject, preferenceEditObject) }
                )
            }

            if (isProUser) {
                AiGroup(
                    onOpenApiProviderSetter    = { openAiApiProviderSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                    onOpenApiKeySetter         = { openAiApiKeySetter(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                    onOpenModelSetter          = { openAiModelSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                    onOpenSystemPromptSetter   = { openAiSystemPromptSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
                )
            }

            AppGroup(
                localeDisplayName        = localeDisplayName,
                onOpenLanguagePicker     = { openLanguagePicker() },
                onOpenLauncherSelection  = { openLauncherSelection(this@SettingsActivity) }
            )

            OtherGroup(
                vibrationPermission        = vibrationPermission,
                onVibrationPermissionChange = { vibrationPermission = it; preferenceEditObject.putBoolean("vibrationPermission", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                isProUser                  = isProUser,
                onOpenSoundEffectsList     = { openSoundEffectsList() },
                onOpenSysinfoArtSetter     = { openSysinfoArtSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) },
                initCmdLog                 = initCmdLog,
                onInitCmdLogChange          = { initCmdLog = it; preferenceEditObject.putBoolean("initCmdLog", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                disableAds                 = disableAds,
                onDisableAdsChange          = { disableAds = it; preferenceEditObject.putBoolean("disableAds", it).apply(); changedSettingsCallback(this@SettingsActivity) },
                onOpenNewsWebsiteSetter    = { openNewsWebsiteSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
            )
        }
    }
}