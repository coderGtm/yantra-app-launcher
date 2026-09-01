package com.coderGtm.yantra.terminal

import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.coderGtm.yantra.BuildConfig
import com.coderGtm.yantra.DEFAULT_TERMINAL_FONT_NAME
import com.coderGtm.yantra.NO_LOG_COMMANDS
import com.coderGtm.yantra.R
import com.coderGtm.yantra.applyLauncherBackground
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.contactsManager
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.getAliases
import com.coderGtm.yantra.getCurrentTheme
import com.coderGtm.yantra.getInit
import com.coderGtm.yantra.getScripts
import com.coderGtm.yantra.getUserName
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.isPro
import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.ShortcutBlock
import com.coderGtm.yantra.models.Suggestion
import com.coderGtm.yantra.promoteProVersion
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.requestUpdateIfAvailable
import com.coderGtm.yantra.runInitTasks
import com.coderGtm.yantra.showRatingAndCommunityPopups
import com.coderGtm.yantra.suggestions.CompletionInput
import com.coderGtm.yantra.suggestions.CompletionResult
import com.coderGtm.yantra.suggestions.SuggestionEngine
import com.coderGtm.yantra.suggestions.SuggestionSources
import com.coderGtm.yantra.suggestions.buildCommandCompletionSpecs
import com.coderGtm.yantra.suggestions.tokenize
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import com.coderGtm.yantra.vibrate
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.TimerTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Terminal(
    val activity: Activity,
    val binding: MainActivityUiRefs,
    val preferenceObject: SharedPreferences
) {
    private val fontSize = preferenceObject.getInt("fontSize", 16).toFloat()
    private val hideKeyboardOnEnter = preferenceObject.getBoolean("hideKeyboardOnEnter", true)
    private val cacheSize = 5
    private val vibrationPermission = preferenceObject.getBoolean("vibrationPermission",true)
    private val getPrimarySuggestions = preferenceObject.getBoolean("getPrimarySuggestions",true)
    private val getSecondarySuggestions = preferenceObject.getBoolean("getSecondarySuggestions",true)
    
    private var commandQueue: MutableList<String> = mutableListOf()
    private var cmdHistoryCursor = -1
    private var commandCache = mutableListOf<Map<String, BaseCommand>>()

    val theme = getCurrentTheme(activity, preferenceObject)
    val commands = getAvailableCommands(activity)
    var primarySuggestions: MutableList<Suggestion> = mutableListOf()
    var initialized = false
    var typeface: Typeface? = Typeface.createFromAsset(activity.assets, "fonts/source_code_pro.ttf")
    var dominantFontColor: Int? = null
    var isSleeping = false
    var sleepTimer: TimerTask? = null
    var contactsFetched: Boolean = false
    var contactNames = HashSet<String>()
    var appListFetched: Boolean = false
    var shortcutListFetched: Boolean = false
    var workingDir = ""
    var cmdHistory = ArrayList<String>()
    var username = binding.username

    lateinit var appList: ArrayList<AppBlock>
    lateinit var shortcutList: ArrayList<ShortcutBlock>
    lateinit var aliasList: MutableList<Alias>

    val suggestionSources: SuggestionSources = TerminalSuggestionSources(this)
    val suggestionEngine: SuggestionEngine = SuggestionEngine(
        buildCommandCompletionSpecs(
            getScripts = { getScripts(preferenceObject) },
            getAliases = { aliasList.map { it.key } },
            getThemes = { terminalPreferenceThemeNames(preferenceObject) },
            getTodoArguments = { buildTodoArguments(preferenceObject) },
            getSfxNames = { buildSfxNames(activity) },
            getCommandNames = { commands.keys.toList() },
            getWeatherFields = { com.coderGtm.yantra.commands.weather.VALID_WEATHER_FIELDS },
        )
    )

    private val suggestionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var suggestionJob: Job? = null

    fun scheduleSuggestions(
        rawInput: String,
        primaryEnabled: Boolean,
        secondaryEnabled: Boolean,
    ) {
        suggestionJob?.cancel()
        suggestionJob = suggestionScope.launch {
            delay(75)
            val snapshotInput = rawInput
            val snapshotCommands = commands.keys.toSet()
            val snapshotAliases = aliasList.associate { it.key to it.value }
            // Preserve the old "contacts not fetched yet" message for the call command.
            val firstToken = snapshotInput.trim().split(" ").firstOrNull()
            val effectiveFirst = if (firstToken != null) snapshotAliases[firstToken] ?: firstToken.lowercase() else null
            if (effectiveFirst == "call" && !contactsFetched && secondaryEnabled) {
                binding.suggestionsTab.removeAllViews()
                binding.addSuggestion(
                    text = activity.getString(R.string.contacts_not_fetched_yet),
                    color = theme.suggestionTextColor,
                    fontSize = 14.5f,
                    typeface = typeface,
                    style = Typeface.BOLD_ITALIC,
                    onClick = {},
                )
                return@launch
            }
            val results = withContext(Dispatchers.Default) {
                suggestionEngine.complete(
                    input = CompletionInput(rawText = snapshotInput, cursor = snapshotInput.length),
                    commands = snapshotCommands,
                    aliases = snapshotAliases,
                    sources = suggestionSources,
                    primarySuggestionsEnabled = primaryEnabled,
                    secondarySuggestionsEnabled = secondaryEnabled,
                    orderedPrimarySuggestions = primarySuggestions.filter { !it.isHidden }.map { it.text },
                )
            }
            // Render only if this request is still the current one.
            if (suggestionJob?.isActive == true) {
                renderSuggestions(results, snapshotInput)
            }
        }
    }

    private fun renderSuggestions(results: List<CompletionResult>, rawInput: String) {
        binding.suggestionsTab.removeAllViews()
        results.forEach { result ->
            binding.addSuggestion(
                text = result.displayText,
                color = theme.suggestionTextColor,
                fontSize = 14.5f,
                typeface = typeface,
                style = Typeface.BOLD,
                onClick = {
                    val currentText = binding.cmdInput.text.toString()
                    val applied = currentText.substring(0, result.edit.start) +
                        result.edit.replacement +
                        currentText.substring(result.edit.end)
                    binding.cmdInput.setText(applied)
                    binding.cmdInput.setSelection(applied.length)
                    requestCmdInputFocusAndShowKeyboard(binding)
                    if (!result.isPrimary && result.allowAutoExecute && preferenceObject.getBoolean("actOnSuggestionTap", false)) {
                        this@Terminal.handleCommand(applied)
                        binding.cmdInput.setText("")
                    }
                },
                onLongClick = if (result.isPrimary) {
                    {
                        val commandClass = commands[result.commandName]
                        if (commandClass != null) {
                            val metadata = commandClass.getDeclaredConstructor(Terminal::class.java)
                                .newInstance(this@Terminal).metadata
                            YantraLauncherDialog(activity).showInfo(
                                title = metadata.helpTitle,
                                message = metadata.description,
                                positiveButton = activity.getString(R.string.ok),
                            )
                        }
                    }
                } else {
                    null
                },
            )
        }
        // Preserve actOnLastSecondarySuggestion auto-execute behavior.
        if (results.size == 1 && !results[0].isPrimary &&
            preferenceObject.getBoolean("actOnLastSecondarySuggestion", false)
        ) {
            val effectiveCommand = results[0].commandName
            // Don't auto-execute for some commands.
            if (effectiveCommand == "call" || effectiveCommand == "time" || effectiveCommand == "bg" ||
                effectiveCommand == "notepad" || effectiveCommand == "todo" || effectiveCommand == "run"
            ) {
                return
            }
            // Don't auto-execute if only flag suggestion.
            if (results[0].displayText.startsWith("-")) {
                return
            }
            // Don't auto-execute if no input after primary command.
            val tokens = tokenize(CompletionInput(rawText = rawInput, cursor = rawInput.length))
            if (tokens.tokens.size <= 1) {
                return
            }
            output(activity.getString(R.string.auto_executing_suggestion), theme.successTextColor, Typeface.ITALIC)
            val result = results.first()
            val currentText = binding.cmdInput.text.toString()
            val applied = currentText.substring(0, result.edit.start) +
                result.edit.replacement +
                currentText.substring(result.edit.end)
            binding.cmdInput.setText(applied)
            binding.suggestionsTab.removeAllViews()
            handleCommand(applied)
            binding.cmdInput.setText("")
            Thread {
                Thread.sleep(500)
                activity.runOnUiThread {
                    binding.cmdInput.setText("")
                }
            }.start()
        }
    }

    fun initialize() {
        if (preferenceObject.getBoolean("useModernPromptDesign", false)) {
            binding.modernPrompt.visible = true
            binding.modernPrompt.username = getUserName(preferenceObject)
            binding.username.visibility = android.view.View.GONE
        }

        activity.requestedOrientation = preferenceObject.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        goFullScreen()
        enforceThemeComponents()
        applyLauncherBackground(activity, binding, preferenceObject, theme.bgColor)
        setTypeface()
        setArrowKeys(preferenceObject, binding)
        binding.upBtn.setOnClickListener { cmdUp() }
        binding.downBtn.setOnClickListener { cmdDown() }
        setTextChangedListener()
        createTouchListeners()
        aliasList = getAliases(preferenceObject)
        primarySuggestions = reorderPrimarySuggestions(preferenceObject, getPrimarySuggestionsList(getAvailableCommands(activity), aliasList))
        checkAliasNames()
        setInputListener()
        setLauncherAppsListener(this@Terminal)
        appList = getAppsList(this@Terminal)
        shortcutList = getShortcutList(this@Terminal)
        showSuggestions(binding.cmdInput.text.toString(), getPrimarySuggestions, getSecondarySuggestions, this@Terminal)
        //fetching contacts if permitted
        if (ContextCompat.checkSelfPermission(activity.baseContext, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Thread {
                contactsManager(this)
            }.start()
        }
        Thread {
            requestUpdateIfAvailable(preferenceObject, activity)
        }.start()
    }

    private fun enforceThemeComponents() {
        username.textSize = fontSize
        binding.cmdInput.textSize = fontSize
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = Color.TRANSPARENT
        setPromptText()
        binding.suggestionsTab.backgroundColorInt = theme.suggestionBgColor
        username.setTextColor(theme.inputLineTextColor)
        binding.cmdInput.setTextColor(theme.inputLineTextColor)
        binding.cmdInput.cursorColorInt = theme.inputLineTextColor
        binding.upBtn.setTextColor(theme.resultTextColor)
        binding.downBtn.setTextColor(theme.resultTextColor)
    }
    private fun setTypeface() {
        val fontName = if (isPro(activity)) {
            preferenceObject.getString("font", DEFAULT_TERMINAL_FONT_NAME) ?: DEFAULT_TERMINAL_FONT_NAME
        }
        else {
            DEFAULT_TERMINAL_FONT_NAME
        }
        if (fontName.endsWith(".ttf")) {
            val fontFile = File(activity.filesDir, fontName)
            if (fontFile.exists()) {
                typeface = Typeface.createFromFile(fontFile)
                if (!preferenceObject.getBoolean("useModernPromptDesign", false)) {
                    username.setTypeface(typeface, Typeface.BOLD)
                }
                binding.cmdInput.typeface = typeface
                finishInitialization()
            }
            return
        }
        val request = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            fontName,
            R.array.com_google_android_gms_fonts_certs
        )
        val callback = object : FontsContractCompat.FontRequestCallback() {

            override fun onTypefaceRetrieved(rTypeface: Typeface) {
                typeface = rTypeface
                if (!preferenceObject.getBoolean("useModernPromptDesign", false)) {
                    username.setTypeface(typeface, Typeface.BOLD)
                }
                binding.cmdInput.typeface = typeface
                finishInitialization()
            }

            override fun onTypefaceRequestFailed(reason: Int) {
                typeface = Typeface.createFromAsset(activity.assets, "fonts/source_code_pro.ttf")
                if (!preferenceObject.getBoolean("useModernPromptDesign", false)) {
                    username.setTypeface(typeface, Typeface.BOLD)
                }
                binding.cmdInput.typeface = typeface
                finishInitialization()
            }
        }
        //make handler to fetch font in background
        val handler = Handler(Looper.getMainLooper())
        FontsContractCompat.requestFont(activity, request, callback, handler)
    }

    private fun setInputListener() {
        binding.cmdInput.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    val inputReceived = binding.cmdInput.text.toString().trim()
                    handleInput(inputReceived)
                    true
                }
                else -> true
            }
        }
    }
    private fun setTextChangedListener() {
        if (getPrimarySuggestions || getSecondarySuggestions) {
            registerTextChangedListener()
        }
    }
    private fun registerTextChangedListener() {
        binding.cmdInput.addTextChangedListener {
            showSuggestions(it.toString(), getPrimarySuggestions, getSecondarySuggestions, this@Terminal)
        }
    }
    fun handleInput(input: String) {
        suggestionJob?.cancel()
        binding.suggestionsTab.removeAllViews()
        handleCommand(input)
        binding.cmdInput.setText("")
        if (hideKeyboardOnEnter) hideSoftKeyboard()
        goFullScreen()
    }
    private fun hideSoftKeyboard() {
        binding.hideKeyboard()
    }
    private fun goFullScreen() {
        if (preferenceObject.getBoolean("fullScreen",false)) {
            val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
            // Hide the system bars.
            windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
    private fun setArrowKeys(preferenceObject: SharedPreferences, binding: MainActivityUiRefs) {
        val showArrowKeys = preferenceObject.getBoolean("showArrowKeys",true)
        if (showArrowKeys) {
            val arrowSize = preferenceObject.getInt("arrowSize", 65).toFloat()
            binding.upBtn.textSize = arrowSize
            binding.downBtn.textSize = arrowSize
            binding.upBtn.visibility = View.VISIBLE
            binding.downBtn.visibility = View.VISIBLE
        }
        else {
            binding.upBtn.visibility = View.GONE
            binding.downBtn.visibility = View.GONE
        }
    }
    fun executeCommandsInQueue() {
        while (commandQueue.isNotEmpty() && !isSleeping) {
            val cmdToExecute = commandQueue.removeAt(0)
            handleCommand(cmdToExecute)
        }
    }
    private fun createTouchListeners() {
        binding.scrollView.setGestureListenerCallback((activity as MainActivity))
        // for keyboard open
        binding.inputLineLayout.setOnClickListener {
            requestCmdInputFocusAndShowKeyboard(binding)
        }
    }

    private fun checkAliasNames() {
        val commandNames = commands.keys
        for (i in aliasList.indices) {
            if (commandNames.contains(aliasList[i].key)) {
                output("--> Alias name cannot be an existing command name. Hence, alias '${aliasList[i].key}' needs to be unaliased to use the '${aliasList[i].key}' command.", theme.warningTextColor, null)
            }
        }
    }
    private fun incrementNumOfCommandsEntered(
        preferenceObject: SharedPreferences,
        preferenceEditObject: SharedPreferences.Editor
    ) {
        val n = preferenceObject.getLong("numOfCmdsEntered",0)
        preferenceEditObject.putLong("numOfCmdsEntered",n+1).apply()
    }
    fun output(text: String, color: Int, style: Int?, markdown: Boolean = false): String {
        val renderColor = dominantFontColor ?: color
        var outputId: String? = null
        val addOutput = {
            outputId = binding.addTextOutput(
                text = text,
                color = renderColor,
                style = style,
                markdown = markdown,
                typeface = typeface,
                fontSize = fontSize,
            )
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            addOutput()
        } else {
            val latch = CountDownLatch(1)
            activity.runOnUiThread {
                addOutput()
                latch.countDown()
            }
            latch.await()
        }
        // if error then vibrate
        if (renderColor == theme.errorTextColor && vibrationPermission) {
            vibrate(activity = activity)
        }
        return outputId!!
    }

    fun updateOutputItem(id: String, text: String, color: Int, style: Int?, markdown: Boolean = false) {
        val renderColor = dominantFontColor ?: color
        activity.runOnUiThread {
            binding.updateTextOutput(
                id = id,
                text = text,
                color = renderColor,
                style = style,
                markdown = markdown,
                typeface = typeface,
                fontSize = fontSize,
            )
        }
        if (renderColor == theme.errorTextColor && vibrationPermission) {
            vibrate(activity = activity)
        }
    }
    fun setPromptText() {
        val isModern = preferenceObject.getBoolean("useModernPromptDesign", false)
        if (preferenceObject.getBoolean("showCurrentFolderInPrompt", false) && workingDir.isNotEmpty()) {
            val currentFolder = workingDir.split("/").last()
            if (isModern) {
                binding.modernPrompt.username = "${getUserName(preferenceObject)}/../$currentFolder"
                return
            }
            username.text = "${getUserNamePrefix(preferenceObject)}${getUserName(preferenceObject)}/../$currentFolder>"
            return
        }
        if (isModern) {
            binding.modernPrompt.username = getUserName(preferenceObject)
            return
        }
        username.text = "${getUserNamePrefix(preferenceObject)}${getUserName(preferenceObject)}>"
    }
    private fun getCommandInstance(commandName: String): BaseCommand? {
        val cachedCommand = commandCache.find { it.containsKey(commandName) }

        if (cachedCommand != null) {
            commandCache.remove(cachedCommand)
            commandCache.add(0, cachedCommand)
            return cachedCommand[commandName]
        }
        else {
            if (commandCache.size >= cacheSize) {
                commandCache.removeAt(commandCache.size - 1)
            }

            val commandClass = commands[commandName]
            if (commandClass != null) {
                val newCommand = mapOf(
                    commandName to commandClass.getDeclaredConstructor(Terminal::class.java)
                        .newInstance(this)
                )
                commandCache.add(0, newCommand)
                return newCommand[commandName]
            }
            return null
        }
    }
    private fun finishInitialization() {
        // Update reactive font state so the Compose input prompt recomposes with the real font
        binding.modernPrompt.fontFamily = typeface?.let { androidx.compose.ui.text.font.FontFamily(it) }
        printIntro()
        if (isPro(activity)) {
            Thread {
                val initList = getInit(preferenceObject)
                runInitTasks(initList, preferenceObject, this@Terminal)
            }.start()
        }
        initialized = true
    }

    private fun printIntro() {
        output("${activity.applicationInfo.loadLabel(activity.packageManager)} (v${BuildConfig.VERSION_NAME}) on ${Build.MANUFACTURER} ${Build.MODEL}",theme.resultTextColor, Typeface.BOLD)
        output(activity.getString(R.string.intro_help_or_community), theme.resultTextColor, Typeface.BOLD)
        output("==================",theme.resultTextColor, Typeface.BOLD)
    }

    fun cmdDown() {
        binding.cmdInput.requestFocus()
        if (cmdHistoryCursor<(cmdHistory.size-1)) {
            cmdHistoryCursor++
            binding.cmdInput.setText(cmdHistory[cmdHistoryCursor])
            binding.cmdInput.setSelection(binding.cmdInput.text!!.length)
            requestCmdInputFocusAndShowKeyboard(binding)
        }
    }
    fun cmdUp() {
        binding.cmdInput.requestFocus()
        if (cmdHistoryCursor>0) {
            cmdHistoryCursor--
            binding.cmdInput.setText(cmdHistory[cmdHistoryCursor])
            binding.cmdInput.setSelection(binding.cmdInput.text!!.length)
            requestCmdInputFocusAndShowKeyboard(binding)
        }
    }
    fun handleCommand(command: String, isAlias: Boolean = false, logCmd: Boolean = true) {
        if (isSleeping) {
            commandQueue.add(command)
            return
        }
        val commandName = command.trim().split(" ").firstOrNull()
        if (!isAlias) {
            if (logCmd && !NO_LOG_COMMANDS.contains(commandName?.lowercase())) {
                if (preferenceObject.getBoolean("useModernPromptDesign", false)) {
                    addChatBubble(getUserName(preferenceObject), command)
                } else {
                    output(getUserNamePrefix(preferenceObject)+getUserName(preferenceObject)+"> $command", theme.commandColor, null)
                }
            }
            if (command.trim()!="") {
                cmdHistory.add(command)
                cmdHistoryCursor = cmdHistory.size
                incrementNumOfCommandsEntered(preferenceObject, preferenceObject.edit())
                showRatingAndCommunityPopups(preferenceObject, preferenceObject.edit(), activity)
                promoteProVersion(this@Terminal, preferenceObject)
            }
        }
        commandName?.let { _ ->
            aliasList.find { it.key == commandName }?.let { alias ->
                val newCommand = command.replaceFirst(commandName, alias.value)
                handleCommand(newCommand, true)
                return@handleCommand
            }
        }
        val commandInstance = getCommandInstance(commandName.toString().lowercase())
        if (commandInstance != null) {
            commandInstance.execute(command.trim())
        }
        else {
            if (command.trim() == "") return
            // find most similar command and recommend
            var maxScore = 0.0
            var matchingName = "help"
            for (cmd in commands.keys) {
                val score = findSimilarity(cmd, commandName)
                if (score > maxScore) {
                    matchingName = cmd
                    maxScore = score
                }
            }
            output("$commandName is not a recognized command or alias. Did you mean $matchingName?", theme.errorTextColor, null)
        }
    }

    private fun addChatBubble(username: String, command: String) {
        activity.runOnUiThread {
            binding.addChatBubbleOutput(
                username = username,
                command = command,
                commandColor = theme.commandColor,
                fontSize = fontSize,
                typeface = typeface,
            )
        }
    }

    fun cancelSuggestionScope() {
        suggestionScope.cancel()
    }

}
