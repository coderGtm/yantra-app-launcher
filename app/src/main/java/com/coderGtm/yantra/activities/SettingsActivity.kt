package com.coderGtm.yantra.activities

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.core.widget.addTextChangedListener
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.DEFAULT_TERMINAL_FONT_NAME
import com.coderGtm.yantra.R
import com.coderGtm.yantra.databinding.ActivitySettingsBinding
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.misc.changedSettingsCallback
import com.coderGtm.yantra.misc.openAiApiKeySetter
import com.coderGtm.yantra.misc.openAiApiProviderSetter
import com.coderGtm.yantra.misc.openAiSystemPromptSetter
import com.coderGtm.yantra.misc.openAppSugOrderingSetter
import com.coderGtm.yantra.misc.openArrowSizeSetter
import com.coderGtm.yantra.misc.openDoubleTapActionSetter
import com.coderGtm.yantra.misc.openFontSizeSetter
import com.coderGtm.yantra.misc.openNewsWebsiteSetter
import com.coderGtm.yantra.misc.openOrientationSetter
import com.coderGtm.yantra.misc.openSwipeLeftActionSetter
import com.coderGtm.yantra.misc.openSwipeRightActionSetter
import com.coderGtm.yantra.misc.openSysinfoArtSetter
import com.coderGtm.yantra.misc.openTermuxCmdPathSelector
import com.coderGtm.yantra.misc.openTermuxCmdSessionActionSelector
import com.coderGtm.yantra.misc.openTermuxCmdWorkingDirSelector
import com.coderGtm.yantra.misc.openUsernamePrefixSetter
import com.coderGtm.yantra.misc.setAppSugOrderTvText
import com.coderGtm.yantra.misc.setOrientationTvText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.ktx.languages
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import org.json.JSONObject
import java.util.Locale


class SettingsActivity : AppCompatActivity() {

    private var getPrimarySuggestions = true
    private var getSecondarySuggestions = true
    private var fullscreenLauncher = false
    private var vibrationPermission = true  // permission to vibrate on error
    private var showArrowKeys = true
    private var showCurrentFolderInPrompt = false
    private var oneTapKeyboardActivation = true
    private var hideKeyboardOnEnter = true
    private var actOnSuggestionTap = false
    private var actOnLastSecondarySuggestion = false
    private var initCmdLog = false
    private var fontSize = 16
    private var arrowSize = 65
    private var orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    private var appSugOrderingMode = AppSortMode.A_TO_Z.value
    private var fontName = "Source Code Pro"
    private var appLocale = "en"

    private lateinit var binding: ActivitySettingsBinding

    private val supportedLocales = mapOf(
        "English" to "en",
        "Italiano" to "it",
        "Русский" to "ru",
        "Magyar" to "hu",
    )

    private lateinit var splitInstallManager: SplitInstallManager
    private var splitInstallSessionId = 0
    private val splitInstallStateListener = SplitInstallStateUpdatedListener { state ->
        if (state.sessionId() == splitInstallSessionId) {
            // Read the status of the request to handle the state update.
            when (state.status()) {
                SplitInstallSessionStatus.INSTALLED -> {
                    // Module installed
                    val keys = supportedLocales.keys.toTypedArray()
                    val langCode = state.languages.firstOrNull() ?: "en"
                    appLocale = langCode
                    binding.currentLocale.text = keys[supportedLocales.values.indexOf(langCode)]
                    changedSettingsCallback(this@SettingsActivity)
                    Toast.makeText(this, getString(R.string.changed_app_language_to, keys[supportedLocales.values.indexOf(langCode)]), Toast.LENGTH_LONG).show()
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(appLocale))
                }
                SplitInstallSessionStatus.FAILED -> {
                    // Module installation failed
                    Toast.makeText(this, getString(R.string.an_error_occurred_please_try_again), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private val prefFile = "yantraSP"
    private val preferenceObject: SharedPreferences
        get() = applicationContext.getSharedPreferences(prefFile,0)

    private val preferenceEditObject: SharedPreferences.Editor
        get() {
            val pref: SharedPreferences = applicationContext.getSharedPreferences(prefFile,0)
            return pref.edit()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideProForNonProUsers()

        // temporarily hiding current directory setting
        binding.pUi1.visibility = View.GONE
        binding.pUi2.visibility = View.GONE

        splitInstallManager = SplitInstallManagerFactory.create(this)

        getPrimarySuggestions = preferenceObject.getBoolean("getPrimarySuggestions",true)
        getSecondarySuggestions = preferenceObject.getBoolean("getSecondarySuggestions",true)
        fullscreenLauncher = preferenceObject.getBoolean("fullScreen",false)
        vibrationPermission = preferenceObject.getBoolean("vibrationPermission",true)
        showArrowKeys = preferenceObject.getBoolean("showArrowKeys",true)
        showCurrentFolderInPrompt = preferenceObject.getBoolean("showCurrentFolderInPrompt", false)
        oneTapKeyboardActivation = preferenceObject.getBoolean("oneTapKeyboardActivation",true)
        hideKeyboardOnEnter = preferenceObject.getBoolean("hideKeyboardOnEnter", true)
        actOnSuggestionTap = preferenceObject.getBoolean("actOnSuggestionTap", false)
        actOnLastSecondarySuggestion = preferenceObject.getBoolean("actOnLastSecondarySuggestion", false)
        initCmdLog = preferenceObject.getBoolean("initCmdLog", false)
        fontSize = preferenceObject.getInt("fontSize",16)
        arrowSize = preferenceObject.getInt("arrowSize", 65)
        orientation = preferenceObject.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        appSugOrderingMode = preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value)
        fontName = if (isPro(this@SettingsActivity)) {
            preferenceObject.getString("font", DEFAULT_TERMINAL_FONT_NAME) ?: DEFAULT_TERMINAL_FONT_NAME
        }
        else {
            DEFAULT_TERMINAL_FONT_NAME
        }
        appLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()


        binding.usernamePrefix.text = getUserNamePrefix(preferenceObject)
        binding.fontSizeBtn.text = fontSize.toString()
        binding.arrowSizeBtn.text = arrowSize.toString()
        setOrientationTvText(binding, orientation)
        setAppSugOrderTvText(binding, appSugOrderingMode)
        binding.tvFontName.text = fontName
        binding.currentLocale.text = supportedLocales.filterValues { it == appLocale }.keys.firstOrNull() ?: "English"
        binding.prefixLayout.setOnClickListener { openUsernamePrefixSetter(this@SettingsActivity, binding, preferenceObject, preferenceEditObject) }
        binding.doubleTapActionLayout.setOnClickListener { openDoubleTapActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.rightSwipeActionLayout.setOnClickListener { openSwipeRightActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.leftSwipeActionLayout.setOnClickListener { openSwipeLeftActionSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.newsWebsiteLayout.setOnClickListener { openNewsWebsiteSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.fontSizeBtn.setOnClickListener { openFontSizeSetter(this@SettingsActivity, binding, preferenceObject, preferenceEditObject) }
        binding.arrowSizeBtn.setOnClickListener { openArrowSizeSetter(this@SettingsActivity, binding, preferenceObject, preferenceEditObject) }
        binding.orientationLay.setOnClickListener { openOrientationSetter(this@SettingsActivity, binding, preferenceEditObject) }
        binding.appSugOrderingLay.setOnClickListener { openAppSugOrderingSetter(this@SettingsActivity, binding, preferenceEditObject) }
        binding.sysinfoArtLayout.setOnClickListener { openSysinfoArtSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.termuxCmdPathLayout.setOnClickListener { openTermuxCmdPathSelector(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.termuxCmdWorkDirLayout.setOnClickListener { openTermuxCmdWorkingDirSelector(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.termuxCmdSessionActionLayout.setOnClickListener { openTermuxCmdSessionActionSelector(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.aiProviderLayout.setOnClickListener { openAiApiProviderSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.aiApiKeyLayout.setOnClickListener { openAiApiKeySetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }
        binding.aiSystemPromptLayout.setOnClickListener { openAiSystemPromptSetter(this@SettingsActivity, preferenceObject, preferenceEditObject) }

        binding.fontLay.setOnClickListener {
            if (preferenceObject.getBoolean("fontpack___purchased",true)) {
                Toast.makeText(this, getString(R.string.loading_fonts), Toast.LENGTH_SHORT).show()
                val url = "https://www.googleapis.com/webfonts/v1/webfonts?key=AIzaSyBFFPy6DsYRRQVlADHdCgKk5qd62CJxjqo"
                val queue = Volley.newRequestQueue(this)
                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        val jsonArray = JSONObject(response).getJSONArray("items")
                        val names = ArrayList<String>()
                        for (i in 0 until jsonArray.length()) {
                            names.add(jsonArray.getJSONObject(i).getString("family"))
                        }

                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
                        val dialogView = layoutInflater.inflate(R.layout.dialog_font_list, null) as View

                        val fontSelector = MaterialAlertDialogBuilder(this)
                            .setTitle(getString(R.string.select_a_font))
                            .setView(dialogView)
                            .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                                dialog.cancel()
                            }
                            .create()
                        if (!this@SettingsActivity.isFinishing) {
                            val listView = dialogView.findViewById<ListView>(R.id.fontList)
                            val searchBar = dialogView.findViewById<EditText>(R.id.searchBar)
                            listView.adapter = adapter
                            listView.setOnItemClickListener { _, _, position, _ ->
                                val selectedFontName = adapter.getItem(position) ?: DEFAULT_TERMINAL_FONT_NAME
                                downloadFont(selectedFontName)
                                fontSelector.cancel()
                            }
                            searchBar.addTextChangedListener { text ->
                                adapter.filter.filter(text)
                            }
                            fontSelector.show()
                        }
                    },
                    { error ->
                        if (error is NoConnectionError) {
                            Toast.makeText(this,
                                getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(this,
                                getString(R.string.an_error_occurred_please_try_again), Toast.LENGTH_SHORT).show()
                        }
                    })
                Thread {
                    queue.add(stringRequest)
                }.start()
            }
            else {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.font_pack_is_not_purchased))
                    .setMessage(getString(R.string.font_pack_not_purchased_description))
                    .setPositiveButton(getString(R.string.cool)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        binding.languageLay.setOnClickListener {
            val keys = supportedLocales.keys.toTypedArray()
            val values = supportedLocales.values.toTypedArray()
            MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.language_change_disclaimer))
                .setPositiveButton(getString(R.string.i_understand)) { dialog, _ ->
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.select_a_language))
                        .setItems(keys) { _, which ->
                            downloadLanguage(values[which])
                        }
                        .show()
                }
                .show()
        }

        binding.primarySugSwitch.isChecked = getPrimarySuggestions
        binding.primarySugSwitch.setOnCheckedChangeListener { _, isChecked ->
            getPrimarySuggestions = isChecked
            preferenceEditObject.putBoolean("getPrimarySuggestions",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.secondarySugSwitch.isChecked = getSecondarySuggestions
        binding.secondarySugSwitch.setOnCheckedChangeListener { _, isChecked ->
            getSecondarySuggestions = isChecked
            preferenceEditObject.putBoolean("getSecondarySuggestions",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.fullscreenSwitch.isChecked = fullscreenLauncher
        binding.fullscreenSwitch.setOnCheckedChangeListener { _, isChecked ->
            fullscreenLauncher = isChecked
            preferenceEditObject.putBoolean("fullScreen", isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.vibrationSwitch.isChecked = vibrationPermission
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            vibrationPermission = isChecked
            preferenceEditObject.putBoolean("vibrationPermission",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.showArrowSwitch.isChecked = showArrowKeys
        binding.showArrowSwitch.setOnCheckedChangeListener { _, isChecked ->
            showArrowKeys = isChecked
            preferenceEditObject.putBoolean("showArrowKeys",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.showCurrentFolderInPrompt.isChecked = showCurrentFolderInPrompt
        binding.showCurrentFolderInPrompt.setOnCheckedChangeListener { _, isChecked ->
            showCurrentFolderInPrompt = isChecked
            preferenceEditObject.putBoolean("showCurrentFolderInPrompt",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.oneTapKeyboardActivationSwitch.isChecked = oneTapKeyboardActivation
        binding.oneTapKeyboardActivationSwitch.setOnCheckedChangeListener { _, isChecked ->
            oneTapKeyboardActivation = isChecked
            preferenceEditObject.putBoolean("oneTapKeyboardActivation",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.hideKeyboardOnEnterSwitch.isChecked = hideKeyboardOnEnter
        binding.hideKeyboardOnEnterSwitch.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboardOnEnter = isChecked
            preferenceEditObject.putBoolean("hideKeyboardOnEnter",isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.actOnSuggestionTapSwitch.isChecked = actOnSuggestionTap
        binding.actOnSuggestionTapSwitch.setOnCheckedChangeListener { _, isChecked ->
            actOnSuggestionTap = isChecked
            preferenceEditObject.putBoolean("actOnSuggestionTap", isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.actOnLastSecSugSwitch.isChecked = actOnLastSecondarySuggestion
        binding.actOnLastSecSugSwitch.setOnCheckedChangeListener { _, isChecked ->
            actOnLastSecondarySuggestion = isChecked
            preferenceEditObject.putBoolean("actOnLastSecondarySuggestion", isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
        binding.initCmdLogSwitch.isChecked = initCmdLog
        binding.initCmdLogSwitch.setOnCheckedChangeListener { _, isChecked ->
            initCmdLog = isChecked
            preferenceEditObject.putBoolean("initCmdLog", isChecked).apply()
            changedSettingsCallback(this@SettingsActivity)
        }
    }

    private fun downloadFont(name: String) {
        val request = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            name,
            R.array.com_google_android_gms_fonts_certs
        )
        val callback = object : FontsContractCompat.FontRequestCallback() {

            override fun onTypefaceRetrieved(typeface: Typeface) {
                // font downloaded
                preferenceEditObject.putString("font",name).apply()
                binding.tvFontName.text = name
                Toast.makeText(this@SettingsActivity,
                    getString(R.string.terminal_font_updated_to, name), Toast.LENGTH_SHORT).show()
                changedSettingsCallback(this@SettingsActivity)
            }

            override fun onTypefaceRequestFailed(reason: Int) {
                //error. not yet downloaded
                Toast.makeText(this@SettingsActivity,
                    getString(R.string.error_downloading_font), Toast.LENGTH_LONG).show()
            }
        }
        //make handler to fetch font in background
        val handler = Handler()
        FontsContractCompat.requestFont(this, request, callback, handler)
    }

    private fun downloadLanguage(code: String) {
        val request = SplitInstallRequest.newBuilder()
            .addLanguage(Locale.forLanguageTag(code))
            .build()
        splitInstallManager.registerListener(splitInstallStateListener)
        splitInstallManager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                splitInstallSessionId = sessionId
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.an_error_occurred_please_try_again), Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                splitInstallManager.unregisterListener(splitInstallStateListener)
            }
    }

    private fun hideProForNonProUsers() {
        if (isPro(this@SettingsActivity))  return
        binding.pUi1.visibility = View.GONE
        binding.pUi2.visibility = View.GONE
        binding.pUi3.visibility = View.GONE
        binding.pUi4.visibility = View.GONE
        binding.pUi5.visibility = View.GONE
        binding.pUi6.visibility = View.GONE
        binding.pUi7.visibility = View.GONE
        binding.pUi8.visibility = View.GONE
        binding.pUi9.visibility = View.GONE
        binding.pUi10.visibility = View.GONE
        binding.pUi11.visibility = View.GONE
        binding.pUi12.visibility = View.GONE
        binding.pUi13.visibility = View.GONE
        binding.pUi14.visibility = View.GONE
        binding.rightSwipeActionLayout.visibility = View.GONE
        binding.leftSwipeActionLayout.visibility = View.GONE
        binding.newsWebsiteLayout.visibility = View.GONE
        binding.fontLay.visibility = View.GONE
        binding.termuxCmdPathLayout.visibility = View.GONE
        binding.termuxCmdWorkDirLayout.visibility = View.GONE
        binding.termuxCmdSessionActionLayout.visibility = View.GONE
        binding.aiProviderLayout.visibility = View.GONE
        binding.aiApiKeyLayout.visibility = View.GONE
        binding.aiSystemPromptLayout.visibility = View.GONE
    }
}