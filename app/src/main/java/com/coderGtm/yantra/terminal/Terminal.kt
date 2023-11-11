package com.coderGtm.yantra.terminal

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.coderGtm.yantra.BuildConfig
import com.coderGtm.yantra.DEFAULT_TERMINAL_FONT_NAME
import com.coderGtm.yantra.NO_LOG_COMMANDS
import com.coderGtm.yantra.R
import com.coderGtm.yantra.activities.MainActivity
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.contactsManager
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.findSimilarity
import com.coderGtm.yantra.getCurrentTheme
import com.coderGtm.yantra.getUserName
import com.coderGtm.yantra.getUserNamePrefix
import com.coderGtm.yantra.models.Alias
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.Theme
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.requestUpdateIfAvailable
import com.coderGtm.yantra.setSystemWallpaper
import com.coderGtm.yantra.showRatingAndCommandPopups
import com.coderGtm.yantra.vibrate
import java.util.TimerTask

class Terminal(
    val activity: Activity,
    val binding: ActivityMainBinding,
    val preferenceObject: SharedPreferences
) {
    private val fontSize = preferenceObject.getInt("fontSize", 16).toFloat()
    private val hideKeyboardOnEnter = preferenceObject.getBoolean("hideKeyboardOnEnter", true)
    private val cacheSize = 5
    private val vibrationPermission = preferenceObject.getBoolean("vibrationPermission",true)
    
    private var commandQueue: MutableList<String> = mutableListOf()
    private var cmdHistory = ArrayList<String>()
    private var cmdHistoryCursor = -1
    private var commandCache = mutableListOf<Map<String, BaseCommand>>()

    val theme = getCurrentTheme(preferenceObject)
    val commands = mapOf(
        "open" to com.coderGtm.yantra.commands.open.Command::class.java,
        "openf" to com.coderGtm.yantra.commands.openf.Command::class.java,
        "info" to com.coderGtm.yantra.commands.info.Command::class.java,
        "infof" to com.coderGtm.yantra.commands.infof.Command::class.java,
        "uninstall" to com.coderGtm.yantra.commands.uninstall.Command::class.java,
        "list" to com.coderGtm.yantra.commands.list.Command::class.java,
        "help" to com.coderGtm.yantra.commands.help.Command::class.java,
        "quote" to com.coderGtm.yantra.commands.quote.Command::class.java,
        "bg" to com.coderGtm.yantra.commands.bg.Command::class.java,
        "theme" to com.coderGtm.yantra.commands.theme.Command::class.java,
        "flash" to com.coderGtm.yantra.commands.flash.Command::class.java,
        "text" to com.coderGtm.yantra.commands.text.Command::class.java,
        "echo" to com.coderGtm.yantra.commands.echo.Command::class.java,
        "notify" to com.coderGtm.yantra.commands.notify.Command::class.java,
        "ai" to com.coderGtm.yantra.commands.ai.Command::class.java,
        "calc" to com.coderGtm.yantra.commands.calc.Command::class.java,
        "call" to com.coderGtm.yantra.commands.call.Command::class.java,
        "email" to com.coderGtm.yantra.commands.email.Command::class.java,
        "sleep" to com.coderGtm.yantra.commands.sleep.Command::class.java,
        "bluetooth" to com.coderGtm.yantra.commands.bluetooth.Command::class.java,
        "vibe" to com.coderGtm.yantra.commands.vibe.Command::class.java,
        "init" to com.coderGtm.yantra.commands.init.Command::class.java,
        "todo" to com.coderGtm.yantra.commands.todo.Command::class.java,
        "alias" to com.coderGtm.yantra.commands.alias.Command::class.java,
        "unalias" to com.coderGtm.yantra.commands.unalias.Command::class.java,
        "termux" to com.coderGtm.yantra.commands.termux.Command::class.java,
        "google" to com.coderGtm.yantra.commands.google.Command::class.java,
        "gupt" to com.coderGtm.yantra.commands.gupt.Command::class.java,
        "tts" to com.coderGtm.yantra.commands.tts.Command::class.java,
        "weather" to com.coderGtm.yantra.commands.weather.Command::class.java,
        "username" to com.coderGtm.yantra.commands.username.Command::class.java,
        "news" to com.coderGtm.yantra.commands.news.Command::class.java,
        "bored" to com.coderGtm.yantra.commands.bored.Command::class.java,
        "time" to com.coderGtm.yantra.commands.time.Command::class.java,
        "settings" to com.coderGtm.yantra.commands.settings.Command::class.java,
        "sysinfo" to com.coderGtm.yantra.commands.sysinfo.Command::class.java,
        "scripts" to com.coderGtm.yantra.commands.scripts.Command::class.java,
        "run" to com.coderGtm.yantra.commands.run.Command::class.java,
        "battery" to com.coderGtm.yantra.commands.battery.Command::class.java,
    )
    var typeface: Typeface? = Typeface.createFromAsset(activity.assets, "fonts/source_code_pro.ttf")
    var isSleeping = false
    var sleepTimer: TimerTask? = null
    var contactsFetched: Boolean = false
    var contactNames = HashSet<String>()
    var appListFetched: Boolean = false
    var uninstallCmdActive = false

    lateinit var appList: ArrayList<AppBlock>
    lateinit var wakeBtn: TextView
    lateinit var aliasList: MutableList<Alias>

    fun initialize() {
        activity.requestedOrientation = preferenceObject.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        goFullScreen()
        enforceThemeComponents()
        setTypeface()
        setArrowKeysVisibility(preferenceObject, binding)
        binding.upBtn.setOnClickListener { cmdUp() }
        binding.downBtn.setOnClickListener { cmdDown() }
        setWallpaperIfNeeded(preferenceObject, activity.applicationContext, theme)
        createWakeButton()
        setTextChangedListener()
        createTouchListeners()
        aliasList = getAliases()
        setInputListener()
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
        binding.username.textSize = fontSize
        binding.cmdInput.textSize = fontSize
        binding.cmdInput.textSize = fontSize
        activity.window.statusBarColor = Color.TRANSPARENT
        activity.window.navigationBarColor = theme.bgColor
        binding.username.text = getUserNamePrefix(preferenceObject)+getUserName(preferenceObject)+">"
        binding.suggestionsTab.background = theme.bgColor.toDrawable()
        binding.username.setTextColor(theme.buttonColor)
        binding.cmdInput.setTextColor(theme.buttonColor)
        val unwrappedCursorDrawable = AppCompatResources.getDrawable(activity,
            R.drawable.cursor_drawable
        )
        val wrappedCursorDrawable = DrawableCompat.wrap(unwrappedCursorDrawable!!)
        DrawableCompat.setTint(wrappedCursorDrawable, theme.buttonColor)
        binding.upBtn.setTextColor(theme.resultTextColor)
        binding.downBtn.setTextColor(theme.resultTextColor)
    }
    private fun setTypeface() {
        val fontName = preferenceObject.getString("font", DEFAULT_TERMINAL_FONT_NAME) ?: DEFAULT_TERMINAL_FONT_NAME
        val request = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            fontName,
            R.array.com_google_android_gms_fonts_certs
        )
        val callback = object : FontsContractCompat.FontRequestCallback() {

            override fun onTypefaceRetrieved(rTypeface: Typeface) {
                //set font as retrieved cliTypeface
                typeface = rTypeface
                binding.username.setTypeface(typeface, Typeface.BOLD)
                binding.cmdInput.typeface = typeface
                printIntro()
            }

            override fun onTypefaceRequestFailed(reason: Int) {
                //set font as source code pro from res folder
                typeface = Typeface.createFromAsset(activity.assets, "fonts/source_code_pro.ttf")
                binding.username.setTypeface(typeface, Typeface.BOLD)
                binding.cmdInput.typeface = typeface
                printIntro()
            }
        }
        //make handler to fetch font in background
        val handler = Handler(Looper.getMainLooper())
        FontsContractCompat.requestFont(activity, request, callback, handler)
    }
    private fun setWallpaperIfNeeded(preferenceObject: SharedPreferences, applicationContext: Context, curTheme: Theme, ) {
        if (preferenceObject.getBoolean("defaultWallpaper",true)) {
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            val colorDrawable = ColorDrawable(curTheme.bgColor)
            setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(applicationContext.resources.displayMetrics.widthPixels, applicationContext.resources.displayMetrics.heightPixels))
        }
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
        val getPrimarySuggestions = preferenceObject.getBoolean("getPrimarySuggestions",true)
        val getSecondarySuggestions = preferenceObject.getBoolean("getSecondarySuggestions",true)
        if (getPrimarySuggestions || getSecondarySuggestions) {
            registerTextChangedListener()
        }
    }
    private fun registerTextChangedListener() {
        binding.cmdInput.addTextChangedListener {
            showSuggestions()
        }
    }

    private fun showSuggestions() {

    }
    private fun handleInput(input: String) {
        handleCommand(input)
        binding.cmdInput.setText("")
        if (hideKeyboardOnEnter) hideSoftKeyboard()
        goFullScreen()
    }
    private fun hideSoftKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.cmdInput.windowToken, 0)
    }
    private fun goFullScreen() {
        if (preferenceObject.getBoolean("fullScreen",false)) {
            val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
            // Hide the system bars.
            windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
    private fun setArrowKeysVisibility(preferenceObject: SharedPreferences, binding: ActivityMainBinding) {
        val showArrowKeys = preferenceObject.getBoolean("showArrowKeys",true)
        if (showArrowKeys) {
            binding.upBtn.visibility = View.VISIBLE
            binding.downBtn.visibility = View.VISIBLE
        }
        else {
            binding.upBtn.visibility = View.GONE
            binding.downBtn.visibility = View.GONE
        }
    }
    private fun createWakeButton() {
        wakeBtn = TextView(activity)
        val spannable = SpannableString("Break")
        spannable.setSpan(UnderlineSpan(), 0, spannable.length, 0)
        wakeBtn.text = spannable
        wakeBtn.textSize = fontSize.toFloat()
        wakeBtn.setTextColor(theme.errorTextColor)
        wakeBtn.setOnClickListener {
            sleepTimer?.cancel()
            isSleeping = false
            binding.terminalOutput.removeView(wakeBtn)
            output("Yantra Launcher awakened mid-sleep (~_^)", theme.errorTextColor, null)
            binding.cmdInput.isEnabled = true
            executeCommandsInQueue()
        }
    }
    fun executeCommandsInQueue() {
        while (commandQueue.isNotEmpty() && !isSleeping) {
            val cmdToExecute = commandQueue.removeFirst()
            handleCommand(cmdToExecute)
        }
    }
    private fun createTouchListeners() {
        binding.scrollView.setGestureListenerCallback((activity as MainActivity))
        // for keyboard open
        binding.inputLineLayout.setOnClickListener {
            requestCmdInputFocusAndShowKeyboard(activity, binding)
        }
    }

    private fun getAliases(): MutableList<Alias> {
        //get alias list from shared preferences
        val defaultAliasList = arrayListOf(Alias("h", "help"),Alias("o", "open"), Alias("i", "info"), Alias("u", "uninstall"), Alias("bt", "bluetooth"), Alias("w", "weather"), Alias("tx", "termux"), Alias("cls", "clear"))
        val defaultStringSet = mutableSetOf<String>()
        for (i in defaultAliasList.indices) {
            defaultStringSet.add(defaultAliasList[i].key + "=" + defaultAliasList[i].value)
        }
        val aliasList = preferenceObject.getStringSet("aliasList", defaultStringSet)?.toMutableList()
        val aliasList2 = mutableListOf<Alias>() //convert to list of list
        for (i in aliasList!!.indices) {
            aliasList2.add(Alias(aliasList[i].split("=")[0],aliasList[i].split("=")[1]))
        }
        return aliasList2
    }
    private fun incrementNumOfCommandsEntered(
        preferenceObject: SharedPreferences,
        preferenceEditObject: SharedPreferences.Editor
    ) {
        val n = preferenceObject.getLong("numOfCmdsEntered",0)
        preferenceEditObject.putLong("numOfCmdsEntered",n+1).apply()
    }
    fun output(text: String, color: Int, style: Int?) {
        val t = TextView(activity)
        t.text = text
        t.setFont(typeface, style, color, fontSize)
        t.setTextIsSelectable(true)
        activity.runOnUiThread {
            binding.terminalOutput.addView(t)
        }
        // if error then vibrate
        if (color == theme.errorTextColor && vibrationPermission) {
            vibrate(activity = activity)
        }
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
    private fun printIntro() {
        output("Yantra Launcher (v${BuildConfig.VERSION_NAME}) on ${Build.MANUFACTURER} ${Build.MODEL}",theme.resultTextColor, Typeface.BOLD)
        output("Type 'help' or 'community' for more information.", theme.resultTextColor, Typeface.BOLD)
        output("==================",theme.resultTextColor, Typeface.BOLD)
    }
    fun cmdDown() {
        binding.cmdInput.requestFocus()
        if (cmdHistoryCursor<(cmdHistory.size-1)) {
            cmdHistoryCursor++
            binding.cmdInput.setText(cmdHistory[cmdHistoryCursor])
            binding.cmdInput.setSelection(binding.cmdInput.text.length)
            requestCmdInputFocusAndShowKeyboard(activity, binding)
        }
    }
    fun cmdUp() {
        binding.cmdInput.requestFocus()
        if (cmdHistoryCursor>0) {
            cmdHistoryCursor--
            binding.cmdInput.setText(cmdHistory[cmdHistoryCursor])
            binding.cmdInput.setSelection(binding.cmdInput.text.length)
            requestCmdInputFocusAndShowKeyboard(activity, binding)
        }
    }
    fun handleCommand(command: String, isAlias: Boolean = false, logCmd: Boolean = true) {
        if (isSleeping) {
            commandQueue.add(command)
            return
        }
        val commandName = command.trim().split(" ").firstOrNull()
        if (!isAlias) {
            if (logCmd && !NO_LOG_COMMANDS.contains(commandName)) {
                output(getUserNamePrefix(preferenceObject)+getUserName(preferenceObject)+"> $command", theme.commandColor, null)
            }
            if (command.trim()!="") {
                cmdHistory.add(command)
                cmdHistoryCursor = cmdHistory.size
                incrementNumOfCommandsEntered(preferenceObject, preferenceObject.edit())
                showRatingAndCommandPopups(preferenceObject, preferenceObject.edit(), activity)
            }
        }
        commandName?.let { _ ->
            aliasList.find { it.key == commandName }?.let { alias ->
                val newCommand = command.replaceFirst(commandName, alias.value)
                handleCommand(newCommand, true)
                return@handleCommand
            }
        }
        val commandInstance = getCommandInstance(commandName.toString())
        if (commandInstance != null) {
            commandInstance.execute(command.trim())
        }
        else if (command.trim() == "") {}
        else {
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
}

private fun TextView.setFont(typeface: Typeface?, style: Int?, state: Int, fontSize: Float) {
    if (style == null) {
        this.typeface = typeface
    }
    else {
        this.setTypeface(typeface, style)
    }
    this.setTextColor(state)
    this.textSize = fontSize
}