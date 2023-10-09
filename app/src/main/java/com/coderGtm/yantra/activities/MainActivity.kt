package com.coderGtm.yantra.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.SearchManager
import android.app.WallpaperManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import com.android.billingclient.api.*
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.BuildConfig
import com.coderGtm.yantra.Constants
import com.coderGtm.yantra.R
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.Contacts
import com.coderGtm.yantra.utils.CustomFlag
import com.coderGtm.yantra.utils.TerminalGestureListenerCallback
import com.coderGtm.yantra.utils.clearConsole
import com.coderGtm.yantra.utils.createNotificationChannel
import com.coderGtm.yantra.utils.defaultWallpaperManager
import com.coderGtm.yantra.utils.eval
import com.coderGtm.yantra.utils.feedback
import com.coderGtm.yantra.utils.findSimilarity
import com.coderGtm.yantra.utils.getCPUSpeed
import com.coderGtm.yantra.utils.getInit
import com.coderGtm.yantra.utils.getScripts
import com.coderGtm.yantra.utils.getToDo
import com.coderGtm.yantra.utils.getToDoProgressList
import com.coderGtm.yantra.utils.getUserName
import com.coderGtm.yantra.utils.getUserNamePrefix
import com.coderGtm.yantra.utils.goFullScreen
import com.coderGtm.yantra.utils.incrementNumOfCommandsEntered
import com.coderGtm.yantra.utils.isNetworkAvailable
import com.coderGtm.yantra.utils.lockDeviceByAccessibilityService
import com.coderGtm.yantra.utils.lockDeviceByAdmin
import com.coderGtm.yantra.utils.openURL
import com.coderGtm.yantra.utils.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.utils.requestCommand
import com.coderGtm.yantra.utils.requestUpdateIfAvailable
import com.coderGtm.yantra.utils.setArrowKeysVisibility
import com.coderGtm.yantra.utils.setToDo
import com.coderGtm.yantra.utils.setToDoProgress
import com.coderGtm.yantra.utils.setupPermissions
import com.coderGtm.yantra.utils.tintBitMap
import com.coderGtm.yantra.utils.toast
import com.coderGtm.yantra.utils.updateAliasList
import com.coderGtm.yantra.utils.verifyValidSignature
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.schedule
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TerminalGestureListenerCallback {

    private lateinit var curTheme: ArrayList<String>
    private lateinit var appList: ArrayList<AppBlock>
    private var commandQueue: MutableList<String> = mutableListOf()
    private var cmdHistory = ArrayList<String>()
    private var cmdHistoryCursor = -1
    private var tts: TextToSpeech? = null
    private var ttsTxt = ""
    private var contactNames = HashSet<String>()
    private var appListFetched: Boolean = false
    private var contactsFetched: Boolean = false
    private var fontSize = 16
    private var isSleeping = false
    private var sleepTimer: TimerTask? = null
    private var uninstallCmdActive = false
    private var cliTypeface: Typeface? = null
    private lateinit var wakeBtn: TextView
    private lateinit var billingClient: BillingClient
    private lateinit var aliasList: MutableList<List<String>>

    private lateinit var binding: ActivityMainBinding

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        if (preferenceObject.getBoolean("fullScreen",false)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        setContentView(binding.root)

        performPreInitTasks()
        createWakeButton()
        performInputChores()
        createTouchListeners()
        aliasList = getAliasList()
        showSuggestions()
        Thread {
            requestUpdateIfAvailable(preferenceObject, preferenceEditObject, this@MainActivity)
        }.start()
    }

    override fun onStart() {
        super.onStart()
        Thread {
            appList = getAppsList(this)
            val initList = getInit(preferenceObject, preferenceEditObject)
            runInitTasks(initList)
        }.start()
    }

    override fun onRestart() {
        super.onRestart()
        val unwrappedCursorDrawable = AppCompatResources.getDrawable(this,
            R.drawable.cursor_drawable
        )
        val wrappedCursorDrawable = DrawableCompat.wrap(unwrappedCursorDrawable!!)
        DrawableCompat.setTint(wrappedCursorDrawable, Color.parseColor(curTheme[3]))
        Thread {
            requestUpdateIfAvailable(preferenceObject, preferenceEditObject, this@MainActivity)
        }.start()
    }

    override fun onResume() {
        super.onResume()
        if (uninstallCmdActive) {
            uninstallCmdActive = false
            appList = getAppsList(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            billingClient.endConnection()
        }
        catch(_: java.lang.Exception) {}
    }

    private fun runInitTasks(initList: String?) {
        if (initList?.trim() != "") {
            runOnUiThread {
                initList?.lines()?.forEach {
                    cmdHandler(it.trim())
                }
            }
        }
    }
    private fun getAppsList(context: Context): ArrayList<AppBlock> {
        val alreadyFetched = appListFetched
        appListFetched = false
        if (!alreadyFetched){
            appList = ArrayList<AppBlock>()
        }

        try {
            val collator = Collator.getInstance()
            // get list of all apps which are launchable
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            val apps = pm.queryIntentActivities(intent, 0)
            for (app in apps) {
                if (app.activityInfo.packageName != context.packageName) {
                    val appBlock = AppBlock(
                        app.loadLabel(pm).toString(),
                        app.activityInfo.packageName
                    )
                    if (!appList.contains(appBlock)) {
                        appList.add(appBlock)
                    }
                }
            }

            if (alreadyFetched) {
                val newAppList = appList
                for (appBlock in appList) {
                    try {
                        pm.getPackageInfo(appBlock.packageName, PackageManager.GET_META_DATA)
                    } catch (e: Exception) {
                        // package does not exist now. Is deleted!
                        val indexToRemove = newAppList.indexOfFirst {
                            it.packageName == appBlock.packageName
                        }
                        newAppList.removeAt(indexToRemove)
                    }
                }
                appList = newAppList
            }

            if (!alreadyFetched || preferenceObject.getInt("appSortMode", Constants().appSortMode_alphabetically) == Constants().appSortMode_alphabetically) {
                appList.sortWith { app1, app2 ->
                    collator.compare(app1.appName, app2.appName)
                }
            }

        } catch (e: Exception) {
            printToConsole("An error occurred while fetching apps list", 5)
        }
        appListFetched = true
        return appList
    }

    override fun onBackPressed() {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data
                val bg: Drawable = try {
                    val inputStream = contentResolver.openInputStream(uri!!)
                    Drawable.createFromStream(inputStream, uri.toString())!!
                } catch (e: FileNotFoundException) {
                    Color.parseColor(curTheme[0]).toDrawable()
                }
                val wallpaperManager = WallpaperManager.getInstance(this)
                wallpaperManager.setBitmap((bg as BitmapDrawable).bitmap)
                preferenceEditObject.putBoolean("defaultWallpaper",false).apply()
                printToConsole("Selected Wallpaper applied!", 6)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants().storagePermission -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    printToConsole("Permission denied!",5)
                } else {
                    printToConsole("Permission Granted",6)
                }
            }
            Constants().callPermission -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    printToConsole("Permission denied!",5)
                }
                else {
                    printToConsole("Permission Granted",6)
                }
            }
            Constants().contactsPermission -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    printToConsole("Permission denied!",5)
                }
                else {
                    printToConsole("Permission Granted",6)
                }
            }
            Constants().bluetoothPermission -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    printToConsole("Permission denied!",5)
                }
                else {
                    printToConsole("Permission Granted",6)
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                runOnUiThread { printToConsole("Error: TTS language not supported!",5) }
            } else {
                tts!!.setSpeechRate(.7f)
                tts!!.speak(ttsTxt, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                runOnUiThread { printToConsole("TTS synthesized! Playing now...",6) }
            }
            override fun onDone(utteranceId: String) {
                runOnUiThread { printToConsole("Shutting down TTS engine...", 4) }

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                runOnUiThread { printToConsole("TTS engine shutdown.", 4) }
            }
            override fun onError(utteranceId: String) {
                runOnUiThread { printToConsole("TTS error!!",5)
                    printToConsole("Shutting down TTS engine...", 4) }

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                runOnUiThread { printToConsole("TTS engine shutdown.", 4) }

            }
        })
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            cmdUp()
        }
        else if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            cmdDown()
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onSingleTap() {
        val oneTapKeyboardActivation = preferenceObject.getBoolean("oneTapKeyboardActivation",true)
        if (oneTapKeyboardActivation) {
            requestCmdInputFocusAndShowKeyboard(this@MainActivity, binding)
        }
    }

    override fun onDoubleTap() {
        val cmdToExecute = preferenceObject.getString("doubleTapCommand", "lock")
        if (cmdToExecute != "") {
            //execute command
            cmdHandler(cmdToExecute!!)
        }
    }

    private fun performInputChores() {
        binding.cmdInput.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    val inputRcvd = binding.cmdInput.text.toString().trim()
                    cmdHandler(inputRcvd)
                    binding.cmdInput.setText("")
                    val hideKeyboardOnEnter = preferenceObject.getBoolean("hideKeyboardOnEnter", true)
                    if (hideKeyboardOnEnter) {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.cmdInput.windowToken, 0)
                    }
                    goFullScreen(preferenceObject, this)
                    true
                }
                else -> false
            }
        }
        binding.cmdInput.addTextChangedListener {
            showSuggestions()
        }
    }

    private fun createTouchListeners() {
        binding.scrollView.setGestureListenerCallback(this)
        // for keyboard open
        binding.inputLineLayout.setOnClickListener {
            requestCmdInputFocusAndShowKeyboard(this@MainActivity, binding)
        }
    }

    private fun showSuggestions() {
        Thread {
            runOnUiThread {
                binding.suggestionsTab.removeAllViews()
            }
            val cmdEntered = binding.cmdInput.text.toString().trim()
            val suggestions = ArrayList<String>()
            val args = cmdEntered.split(" ")
            var overrideLastWord = false
            var isPrimary = true
            val getPrimarySuggestions = preferenceObject.getBoolean("getPrimarySuggestions",true)
            val getSecondarySuggestions = preferenceObject.getBoolean("getSecondarySuggestions",true)

            if ((args.isEmpty() || (args.size == 1 && binding.cmdInput.text.toString().lastOrNull() != ' ')) && getPrimarySuggestions) {
                overrideLastWord = true
                val regex = Regex(Pattern.quote(args[0]), RegexOption.IGNORE_CASE)
                val allPrimarySuggestions : ArrayList<String> = Constants().primarySuggestions
                aliasList.forEach {
                    allPrimarySuggestions.add(it[0])
                }
                for (ps in allPrimarySuggestions) {
                    if (regex.containsMatchIn(ps)) {
                        suggestions.add(ps)
                    }
                }
            }
            else if ((args.size > 1 || (args.size == 1 && binding.cmdInput.text.toString().lastOrNull() == ' ')) && getSecondarySuggestions) {
                // check for alias
                val effectivePrimaryCmd: String
                val isAliasCmd = aliasList.any { it[0] == args[0] }
                effectivePrimaryCmd = if (isAliasCmd) {
                    aliasList.first { it[0] == args[0] }[1]
                } else {
                    args[0].lowercase()
                }
                if (effectivePrimaryCmd == "open") {
                    if (!appListFetched) {
                        return@Thread
                    }
                    if (args.size>1) {
                        //search using regex
                        overrideLastWord = true
                        val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                        for (app in appList) {
                            if (regex.containsMatchIn(app.appName) && !suggestions.contains(app.appName)) {
                                suggestions.add(app.appName)
                            }
                        }
                    }
                    else {
                        for (app in appList) {
                            if (!suggestions.contains(app.appName)) {
                                suggestions.add(app.appName)
                            }
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "uninstall") {
                    if (!appListFetched) {
                        return@Thread
                    }
                    if (args.size>1) {
                        //search using regex
                        overrideLastWord = true
                        val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                        for (app in appList) {
                            if (regex.containsMatchIn(app.appName) && !suggestions.contains(app.appName)) {
                                suggestions.add(app.appName)
                            }
                        }
                    }
                    else {
                        for (app in appList) {
                            if (!suggestions.contains(app.appName)) {
                                suggestions.add(app.appName)
                            }
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "info") {
                    if (!appListFetched) {
                        return@Thread
                    }
                    if (args.size>1) {
                        //search using regex
                        overrideLastWord = true
                        val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                        for (app in appList) {
                            if (regex.containsMatchIn(app.appName) && !suggestions.contains(app.appName)) {
                                suggestions.add(app.appName)
                            }
                        }
                    }
                    else {
                        for (app in appList) {
                            if (!suggestions.contains(app.appName)) {
                                suggestions.add(app.appName)
                            }
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "openf") {
                    if (!appListFetched) {
                        return@Thread
                    }
                    if (args.size>1) {
                        //search using regex
                        overrideLastWord = true
                        val name = cmdEntered.removePrefix(args[0]).trim().lowercase()
                        val candidates = mutableListOf<Double>()
                        for (app in appList) {
                            val score = findSimilarity(app.appName.lowercase(), name)
                            candidates.add(score)
                            //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
                        }
                        val maxIndex = candidates.indexOf(candidates.max())
                        val appBlock = appList[maxIndex]
                        suggestions.add(appBlock.appName)
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "call") {
                    if (!contactsFetched) {
                        runOnUiThread {
                            binding.suggestionsTab.removeAllViews()
                        }
                        val tv = TextView(this)
                        tv.text = "   Contacts not yet fetched! Use 'list contacts' command to fetch contacts first to get suggestions.   "
                        tv.setTextColor(Color.parseColor(curTheme.elementAt(2)))
                        //italics
                        tv.setTypeface(cliTypeface, Typeface.BOLD_ITALIC)
                        runOnUiThread {
                            binding.suggestionsTab.addView(tv)
                        }
                        return@Thread
                    }
                    else if (args.size>1) {
                        //search using regex
                        overrideLastWord = true
                        val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                        for (contact in contactNames) {
                            if (regex.containsMatchIn(contact)) {
                                suggestions.add(contact)
                            }
                        }
                    }
                    else {
                        for (contact in contactNames) {
                            if (!suggestions.contains(contact)) {
                                suggestions.add(contact)
                            }
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "list") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val listArgs = listOf("apps","themes","contacts")
                    for (arg in listArgs) {
                        if (regex.containsMatchIn(arg)) {
                            suggestions.add(arg)
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "battery") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val batteryArgs = listOf("-bar")
                    for (arg in batteryArgs) {
                        if (regex.containsMatchIn(arg)) {
                            suggestions.add(arg)
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "flash" || effectivePrimaryCmd == "bluetooth") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val listArgs = listOf("1","0","on","off")
                    for (arg in listArgs) {
                        if (regex.containsMatchIn(arg)) {
                            suggestions.add(arg)
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "todo") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val listArgs = mutableListOf("-p","-1")
                    val todoSize = getToDo(preferenceObject).size
                    for (i in 0 until todoSize) {
                        listArgs.add(i.toString())
                    }
                    for (arg in listArgs) {
                        if (regex.containsMatchIn(arg)) {
                            suggestions.add(arg)
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "help") {
                    if (args.size>1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    for (cmd in Constants().helpArray) {
                        if (regex.containsMatchIn(cmd)) {
                            suggestions.add(cmd)
                        }
                    }
                    isPrimary = false

                }
                else if (effectivePrimaryCmd == "alias") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    if (regex.containsMatchIn("-1")) {
                        suggestions.add("-1")
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "unalias") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val unaliasArgs = aliasList.toMutableList()
                    unaliasArgs.add(0, listOf("-1",""))
                    for (aliasPair in unaliasArgs) {
                        if (regex.containsMatchIn(aliasPair[0])) {
                            suggestions.add(aliasPair[0])
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "theme") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val themeArgs = mutableListOf("-1")
                    Constants().themeList.indices.forEach {
                        themeArgs.add(it.toString())
                    }
                    for (arg in themeArgs) {
                        if (regex.containsMatchIn(arg)) {
                            suggestions.add(arg)
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "bg") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val bgArgs= listOf("-1", "random")
                    for (arg in bgArgs) {
                        if (regex.containsMatchIn(arg)) {
                            suggestions.add(arg)
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "echo") {
                    if (args.size > 1) {
                        overrideLastWord = true
                    }
                    val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                    val echoArgs= listOf("-e", "-s", "-w")
                    for (arg in echoArgs) {
                        if (regex.containsMatchIn(arg)) {
                            suggestions.add(arg)
                        }
                    }
                    isPrimary = false
                }
                else if (effectivePrimaryCmd == "run") {
                    try {
                        val scripts = getScripts(preferenceObject)
                        if (args.size>1) {
                            overrideLastWord = true
                            val regex = Regex(Pattern.quote(cmdEntered.removePrefix(args[0]).trim()), RegexOption.IGNORE_CASE)
                            for (sname in scripts) {
                                if (regex.containsMatchIn(sname)) {
                                    suggestions.add(sname)
                                }
                            }
                        }
                        else {
                            for (sname in scripts) {
                                if (!suggestions.contains(sname)) {
                                    suggestions.add(sname)
                                }
                            }
                        }
                        isPrimary = false
                    }
                    catch (e: java.lang.Exception) {
                        return@Thread
                    }
                }

            }

            suggestions.forEach { sug ->
                if ((isPrimary && (cmdEntered.trim() == sug.trim())) || (!isPrimary && (cmdEntered.removePrefix(args[0]).trim() == sug.trim()))) {
                    return@forEach
                }
                val btn = Button(this)
                btn.text = sug
                btn.setTextColor(Color.parseColor(curTheme.elementAt(2)))
                if (preferenceObject.getBoolean("fontpack___purchased",false)) {
                    btn.setTypeface(cliTypeface, Typeface.BOLD)
                }
                else {
                    btn.setTypeface(null, Typeface.BOLD)
                }
                btn.background = Color.TRANSPARENT.toDrawable()
                //set start and end margins
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(20, 0, 20, 0)
                btn.layoutParams = params


                btn.setOnClickListener {
                    if (overrideLastWord) {
                        val newCmd = cmdEntered.substring(0, cmdEntered.length-args[args.size-1].length) + sug + " "
                        binding.cmdInput.setText(newCmd)
                    }
                    else {
                        binding.cmdInput.setText("$cmdEntered $sug ")
                    }
                    binding.cmdInput.setSelection(binding.cmdInput.text.length)
                    requestCmdInputFocusAndShowKeyboard(this@MainActivity, binding)
                    binding.suggestionsTab.removeView(it)

                    val actOnSuggestionTap = preferenceObject.getBoolean("actOnSuggestionTap", false)
                    if (!isPrimary && actOnSuggestionTap) {
                        cmdHandler(binding.cmdInput.text.toString().trim())
                        binding.cmdInput.setText("")
                    }
                }
                if (isPrimary) {
                    btn.setOnLongClickListener {
                        val helpDescriptionArray = Constants().helpDescriptionArray
                        for (i in helpDescriptionArray.indices) {
                            if (helpDescriptionArray[i][0].split(" ")[0] == sug) {
                                runOnUiThread {
                                    MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                                        .setTitle(helpDescriptionArray[i][0])
                                        .setMessage(helpDescriptionArray[i][1])
                                        .setPositiveButton("Ok") { helpDialog, _ ->
                                            helpDialog.dismiss()
                                        }
                                        .show()
                                }
                            }
                        }
                        true
                    }
                }
                runOnUiThread {
                    binding.suggestionsTab.addView(btn)
                }
            }
        }.start()
    }

    private fun enforceTheme(id: Int) {
        //0 bg, 1 cmds, 2 suggestions, 3 input&btns, 4 result, 5 error, 6 success, 7 warn
        when (id) {
            -1 -> {
                curTheme = preferenceObject.getString("customThemeClrs", "#121212,#A0A0A0,#E1BEE7,#FAEBD7,#EBEBEB,#F00000,#00C853,#FFD600")!!.split(",").toMutableList() as ArrayList<String>
            }
            0 -> {
               //default
                curTheme = Constants().defaultTheme
            }
            1 -> {
                //light
                curTheme = Constants().lightTheme
            }
            2 -> {
                //hacker
                curTheme = Constants().hackerTheme
            }
            3 -> {
                //ocean
                curTheme = Constants().oceanTheme
            }
            4 -> {
                //gruvbox
                curTheme = Constants().gruvboxTheme
            }
            5 -> {
                //material
                curTheme = Constants().materialTheme
            }
            6 -> {
                //dark
                curTheme = Constants().darkTheme
            }
            7 -> {
                //solarized
                curTheme = Constants().solarizedTheme
            }
            8 -> {
                //dracula
                curTheme = Constants().draculaTheme
            }
            9 -> {
                //monokai
                curTheme = Constants().monokaiTheme
            }
            10 -> {
                //green
                curTheme = Constants().greenTheme
            }
            11 -> {
                //red
                curTheme = Constants().redTheme
            }
            12 -> {
                //blue
                curTheme = Constants().blueTheme
            }
            13 -> {
                //yellow
                curTheme = Constants().yellowTheme
            }
            14 -> {
                //purple
                curTheme = Constants().purpleTheme
            }
            15 -> {
                //orange
                curTheme = Constants().orangeTheme
            }
            16 -> {
                //pink
                curTheme = Constants().pinkTheme
            }
            17 -> {
                //ubuntu
                curTheme = Constants().ubuntuTheme
            }
        }
    }
    private fun performPreInitTasks() {
        requestedOrientation = preferenceObject.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        goFullScreen(preferenceObject, this)
        enforceTheme(preferenceObject.getInt("theme", 0))
        enforceThemeComponents()
        setFonts()
        setArrowKeysVisibility(preferenceObject, binding)
        defaultWallpaperManager(preferenceObject, applicationContext, curTheme)
        binding.upBtn.setOnClickListener { cmdUp() }
        binding.downBtn.setOnClickListener { cmdDown() }
        //fetching contacts if permitted
        if (ContextCompat.checkSelfPermission(baseContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Thread {
                fetchContacts()
            }.start()
        }
    }
    private fun enforceThemeComponents() {
        fontSize = preferenceObject.getInt("fontSize", 16)
        binding.username.textSize = fontSize.toFloat()
        binding.cmdInput.textSize = fontSize.toFloat()
        binding.cmdInput.textSize = fontSize.toFloat()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor(curTheme[0])
        binding.username.text = getUserNamePrefix(preferenceObject)+getUserName(preferenceObject)+">"
        binding.suggestionsTab.background = Color.parseColor(curTheme.elementAt(0)).toDrawable()
        binding.username.setTextColor(Color.parseColor(curTheme[3]))
        binding.cmdInput.setTextColor(Color.parseColor(curTheme[3]))
        val unwrappedCursorDrawable = AppCompatResources.getDrawable(this,
            R.drawable.cursor_drawable
        )
        val wrappedCursorDrawable = DrawableCompat.wrap(unwrappedCursorDrawable!!)
        DrawableCompat.setTint(wrappedCursorDrawable, Color.parseColor(curTheme[3]))
        binding.upBtn.setTextColor(Color.parseColor(curTheme[4]))
        binding.downBtn.setTextColor(Color.parseColor(curTheme[4]))
    }
    private fun setFonts() {
        val fontName = preferenceObject.getString("font", Constants().defaultFontName) ?: Constants().defaultFontName
        val request = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            fontName,
            R.array.com_google_android_gms_fonts_certs
        )
        val callback = object : FontsContractCompat.FontRequestCallback() {

            override fun onTypefaceRetrieved(typeface: Typeface) {
                //set font as retrieved cliTypeface
                cliTypeface = typeface
                binding.username.setTypeface(cliTypeface, Typeface.BOLD)
                binding.cmdInput.typeface = cliTypeface
                printIntro()
            }

            override fun onTypefaceRequestFailed(reason: Int) {
                //set font as source code pro from res folder
                cliTypeface = Typeface.createFromAsset(assets, "fonts/source_code_pro.ttf")
                binding.username.setTypeface(cliTypeface, Typeface.BOLD)
                binding.cmdInput.typeface = cliTypeface
                printIntro()
            }
        }
        //make handler to fetch font in background
        val handler = Handler(Looper.getMainLooper())
        FontsContractCompat.requestFont(this, request, callback, handler)
    }

    private fun printIntro() {
        printToConsole("Yantra Launcher (v${BuildConfig.VERSION_NAME}) on ${Build.MANUFACTURER} ${Build.MODEL}",4, Typeface.BOLD)
        printToConsole("Type 'help' or 'community' for more information.", 4, Typeface.BOLD)
        printToConsole("==================",4, Typeface.BOLD)
    }
    private fun cmdHandler(cmd: String, isAlias: Boolean = false) {
        if (isSleeping) {
            commandQueue.add(cmd)
            return
        }
        if (!isAlias) {
            printToConsole(getUserNamePrefix(preferenceObject)+getUserName(preferenceObject)+"> $cmd", 1)
            if (cmd!="") {
                cmdHistory.add(cmd)
                cmdHistoryCursor = cmdHistory.size
                incrementNumOfCommandsEntered(preferenceObject, preferenceEditObject, this@MainActivity)
            }
        }
        val args = cmd.split(" ")

        for (alias in aliasList) {
            if (args[0] == alias[0]) {
                val newCmd = cmd.replaceFirst(args[0], alias[1])
                cmdHandler(newCmd, true)
                return
            }
        }
//        if (args[0].lowercase() == "crash") {
//            if (args.size > 1) throw RuntimeException("Test Crash")
//            else throw RuntimeException(cmd.trim().removePrefix(args[0]).trim())
//        }
        if (args[0].lowercase() == "open") {
            if (args.size > 1) openApp(cmd.removePrefix(args[0]))
            else printToConsole("Please specify an app to open", 5)
        }
        else if (args[0].lowercase() == "openf") {
            if (args.size > 1) openAppFuzzy(cmd.removePrefix(args[0]))
            else printToConsole("Please give a string to open app with fuzzy search.", 5)
        }
        else if (args[0].lowercase() == "uninstall") {
            if (args.size > 1) uninstallApp(cmd.removePrefix(args[0]))
            else printToConsole("Please specify an app to uninstall", 5)
        }
        else if(args[0].lowercase() == "info") {
            if (args.size > 1) openAppSettings(cmd.removePrefix(args[0]))
            else printToConsole("Please specify an app name", 5)
        }
        else if (args[0].lowercase() == "infof") {
            if (args.size > 1) openAppSettingsFuzzy(cmd.removePrefix(args[0]))
            else printToConsole("Please give a string to open app settings with fuzzy search.", 5)
        }
        else if (args[0].lowercase() == "list") {
            listCmd(args)
        }
        else if (args[0] == "quote") {
            if (args.size > 1) printToConsole("'quote' command does not take any parameters", 5)
            else showQuote()
        }
        else if (args[0].lowercase() == "help") {
            showHelp(args)
        }
        else if (args[0].lowercase() == "bg") {
            setBG(cmd)
        }
        else if (args[0].lowercase() == "theme") {
            if (args.size > 1) setTheme(cmd.removePrefix(args[0]).trim())
            else printToConsole("Please specify a theme id", 5)
        }
        else if (args[0].lowercase() == "echo") {
            if (args.size > 1) echo(cmd.removePrefix(args[0]).trim())
            else printToConsole("Invalid Syntax. See 'help echo' for usage info.", 5)
        }
        else if (args[0].lowercase() == "notify") {
            if (args.size > 1) notify(cmd.removePrefix(args[0]))
            else printToConsole("Please specify the message to fire the notification.", 5)
        }
        else if (args[0].lowercase() == "ai") {
            if (args.size > 1) chatWithAi(cmd.removePrefix(args[0]))
            else printToConsole("Please specify the message to send to AI.", 5)
        }
        else if (args[0].lowercase() == "calc") {
            if (args.size > 1) calculate(cmd.removePrefix(args[0]))
            else printToConsole("Please specify an expression to evaluate.", 5)
        }
        else if (args[0].lowercase() == "call") {
            if (args.size > 1) call(cmd.removePrefix(args[0]))
            else printToConsole("Please specify a contact name to call", 5)
        }
        else if (args[0].lowercase() == "text") {
            if (args.size > 1) sendText(cmd.removePrefix(args[0]))
            else printToConsole("Please specify the message string.", 5)
        }
        else if (args[0].lowercase() == "email") {
            if (args.size > 1) email(cmd.removePrefix(args[0]))
            else printToConsole("Please specify an Email address.", 5)
        }
        else if (args[0].lowercase() == "flash") {
            if (args.size > 1) toggleFlash(args[1])
            else printToConsole("Please specify a state for flashlight", 5)
        }
        else if (args[0].lowercase() == "bluetooth") {
            if (args.size > 1) toggleBT(args[1])
            else printToConsole("Please specify a state for bluetooth", 5)
        }
        else if (args[0].lowercase() == "sleep") {
            if (args.size > 1) sleep(args[1].toLongOrNull())
            else printToConsole("Please specify sleep duration in milliseconds.", 5)
        }
        else if (args[0].lowercase() == "vibe") {
            if (args.size > 1) vibrate(args[1].toLongOrNull())
            else printToConsole("Please specify vibration duration in milliseconds.", 5)
        }
        else if (args[0].lowercase() == "init") {
            if (args.size > 1) printToConsole("'init' command does not take any arguments.",5)
            else init()
        }
        else if (args[0].lowercase() == "todo") {
            todo(cmd)
        }
        else if (args[0].lowercase() == "alias") {
            alias(cmd)
        }
        else if (args[0].lowercase() == "unalias") {
            unalias(cmd)
        }
        else if (args[0].lowercase() == "termux") {
            runInTermux(cmd.removePrefix(args[0]).trim())
        }
        else if (args[0].lowercase() == "google") {
            if (args.size > 1) searchGoogle(cmd.removePrefix(args[0]))
            else printToConsole("Please specify a query to search", 5)
        }
        else if (args[0].lowercase() == "ddg") {
            if (args.size > 1) searchDDG(cmd.removePrefix(args[0]))
            else printToConsole("Please specify a query to search", 5)
        }
        else if (args[0].lowercase() == "brave") {
            if (args.size > 1) searchBrave(cmd.removePrefix(args[0]))
            else printToConsole("Please specify a query to search", 5)
        }
        else if (args[0].lowercase() == "gupt") {
            gupt(cmd)
        }
        else if (args[0].lowercase() == "tts") {
            if (args.size > 1) speakText(cmd.removePrefix(args[0]))
            else printToConsole("Please specify text to speak",1)
        }
        else if (args[0].lowercase() == "weather") {
            if (args.size > 1) getWeather(cmd.removePrefix(args[0]))
            else printToConsole("Please specify a location", 5)
        }
        else if (args[0].lowercase() == "username") {
            if (args.size > 1) setUserName(cmd.removePrefix(args[0]))
            else printToConsole("Please specify a username", 5)
        }
        else if (args[0].lowercase() == "news") {
            if (args.size > 1) printToConsole("'news' command does not take any parameters", 5)
            else {printToConsole("Opening News...", 4)
            openURL(preferenceObject.getString("newsWebsite","https://news.google.com/")!!, this@MainActivity)}
        }
        else if (args[0].lowercase() == "bored") {
            if (args.size > 1) printToConsole("'bored' command does not take any parameters", 5)
            else getRandomActivity()
        }
        else if (args[0].lowercase() == "time") {
            if (args.size > 1) printToConsole("'time' command does not take any parameters", 5)
            else showTime()
        }
        else if (args[0].lowercase() == "settings") {
            if (args.size > 1) printToConsole("'settings' command does not take any parameters", 5)
            else openYantraSettings()
        }
        else if (args[0].lowercase() == "sysinfo") {
            showSystemInfo()
        }
        else if (args[0].lowercase() == "scripts") {
            if (args.size > 1) printToConsole("'scripts' command does not take any parameters", 5)
            else yantraScripts()
        }
        else if (args[0].lowercase() == "run") {
            if (args.size > 1) runScript(args.drop(1))
            else printToConsole("Please specify a script name to run", 5)
        }
        else if (args[0].lowercase() == "battery") {
            showBattery(args.drop(1))
        }
        else if (args[0].lowercase() == "lock") {
            if (args.size > 1) printToConsole("'lock' command does not take any parameters", 5)
            else lockDevice()
        }
        else if (args[0].lowercase() == "clear") {
            if (args.size > 1) printToConsole("'clear' command does not take any parameters", 5)
            else clearConsole(binding)
        }
        else if (args[0].lowercase() == "reset") {
            if (args.size > 1) printToConsole("'reset' command does not take any parameters", 5)
            else recreate()
        }
        else if (args[0].lowercase() == "cmdrequest") {
            if (args.size > 1) printToConsole("'cmdrequest' command does not take any parameters", 5)
            else requestCommand(this@MainActivity, packageManager)
        }
        else if (args[0].lowercase() == "feedback") {
            if (args.size > 1) printToConsole("'feedback' command does not take any parameters", 5)
            else feedback(this@MainActivity, packageManager)
        }
        else if (args[0].lowercase() == "community") {
            if (args.size > 1) printToConsole("'community' command does not take any parameters", 5)
            else openCommunity()
        }
        else if (args[0].lowercase() == "fontpack") {
            if (args.size > 1) printToConsole("'fontpack' command does not take any parameters", 5)
            else fontPack()
        }
        else if (args[0].lowercase() == "exit") {
            if (args.size > 1) printToConsole("'exit' command does not take any parameters", 5)
            else exitApp()
        }
        else if (cmd==""){}
        else {
            // find most similar command and recommend
            val candidates = mutableListOf<Double>()
            val allCmds = Constants().primarySuggestions
            for (commandName in allCmds) {
                val score = findSimilarity(cmd, commandName)
                candidates.add(score)
                //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
            }
            val maxIndex = candidates.indexOf(candidates.max())
            val mostSimilarCmd = allCmds[maxIndex]
            printToConsole("${args[0]} is not a recognized command or alias. Did you mean $mostSimilarCmd?",5)
        }
    }

    private fun setTheme(id: String) {
        if (id.toIntOrNull() != null) {
            if (id.toInt() in Constants().themeList.indices || id.toInt() == -1) {
                if (id.toInt() == -1) {
                    if (!preferenceObject.getBoolean("customtheme___purchased",false)) {
                        printToConsole("[-] Custom Theme Design is a paid add-on feature. Consider buying it to enable it.",5)
                        printToConsole("Salient Features of Custom Theme Design:",7, Typeface.BOLD)
                        printToConsole("--------------------------",7)
                        printToConsole("1. You can customize the colors of the Terminal to your liking.",4)
                        printToConsole("2. All Customizable options: - Background - Input - Command - Normal Text and Arrow - Error Text - Positive Text - Warning Text - Suggestions",4)
                        printToConsole("3. Fine-tune the CLI to your liking and make it your own!",4)
                        printToConsole("--------------------------",7)
                        initializeProductPurchase("customtheme")
                        return
                    }
                    else {
                        printToConsole("[+] Opening Custom Theme Designer",4, Typeface.ITALIC)
                        val dialog = MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                            .setTitle("Customize Your Theme")
                            .setView(R.layout.custom_theme_dialog)
                        val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_theme_dialog, null)
                        val bgColorBtn = dialogView?.findViewById<ImageButton>(R.id.bgColorBtn)
                        val cmdColorBtn = dialogView?.findViewById<ImageButton>(R.id.cmdColorBtn)
                        val suggestionsColorBtn = dialogView?.findViewById<ImageButton>(R.id.suggestionsColorBtn)
                        val inputAndBtnsColorBtn = dialogView?.findViewById<ImageButton>(R.id.inputAndBtnsColorBtn)
                        val resultColorBtn = dialogView?.findViewById<ImageButton>(R.id.resultColorBtn)
                        val errorColorBtn = dialogView?.findViewById<ImageButton>(R.id.errorColorBtn)
                        val successColorBtn = dialogView?.findViewById<ImageButton>(R.id.successColorBtn)
                        val warnColorBtn = dialogView?.findViewById<ImageButton>(R.id.warnColorBtn)
                        val customThemeColors = preferenceObject.getString("customThemeClrs", "#000000,#A0A0A0,#E1BEE7,#FAEBD7,#EBEBEB,#F00000,#00C853,#FFD600")!!.split(",").toMutableList() as ArrayList<String>
                        var i = 0
                        listOf(bgColorBtn, cmdColorBtn, suggestionsColorBtn, inputAndBtnsColorBtn, resultColorBtn, errorColorBtn, successColorBtn, warnColorBtn).forEach { imgBtn ->
                            imgBtn?.setImageDrawable(ColorDrawable(Color.parseColor(customThemeColors[i])))
                            imgBtn?.tag = customThemeColors[i]
                            imgBtn?.setOnClickListener {
                                val colorDialogBuilder = ColorPickerDialog.Builder(this)
                                    .setTitle("Select Color")
                                    .setPositiveButton("Set", ColorEnvelopeListener(){ envelope, _->
                                        toast(baseContext, envelope.hexCode.drop(2).prependIndent("#"))
                                            imgBtn.setImageDrawable(ColorDrawable(Color.parseColor(envelope.hexCode.drop(2).prependIndent("#"))))
                                            imgBtn.tag = envelope.hexCode.drop(2).prependIndent("#")
                                        })
                                    .setNegativeButton("Cancel") { dialogInterface, i ->
                                        dialogInterface.dismiss()
                                    }
                                    .attachAlphaSlideBar(false) // the default value is true.
                                    .attachBrightnessSlideBar(true) // the default value is true.
                                    .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                                //val bubbleFlag = BubbleFlag(this)
                                //bubbleFlag.flagMode = FlagMode.FADE
                                colorDialogBuilder.colorPickerView.flagView = CustomFlag(this,
                                    R.layout.color_picker_flag_view
                                )
                                colorDialogBuilder.colorPickerView.setInitialColor(Color.parseColor("#FF"+imgBtn.tag.toString().drop(1)))
                                colorDialogBuilder.show()
                            }
                            i++
                        }
                        dialog.setView(dialogView)
                        dialog.setPositiveButton("Apply") { _, _ ->
                            //get all colors in hex format
                            val bgColor = bgColorBtn?.tag.toString()
                            val cmdColor = cmdColorBtn?.tag.toString()
                            val suggestionsColor = suggestionsColorBtn?.tag.toString()
                            val inputAndBtnsColor = inputAndBtnsColorBtn?.tag.toString()
                            val resultColor = resultColorBtn?.tag.toString()
                            val errorColor = errorColorBtn?.tag.toString()
                            val successColor = successColorBtn?.tag.toString()
                            val warnColor = warnColorBtn?.tag.toString()
                            val customTheme = listOf(bgColor, cmdColor, suggestionsColor, inputAndBtnsColor, resultColor, errorColor, successColor, warnColor)
                            //addToPrevTxt(customTheme.toString().drop(1).dropLast(1),4)
                            //return@setPositiveButton
                            preferenceEditObject.putString("customThemeClrs", customTheme.toString().drop(1).dropLast(1).replace(" ","")).commit()
                            preferenceEditObject.putInt("theme",-1).apply()
                            enforceTheme(-1)
                            toast(baseContext, "Setting theme to Custom")
                            recreate()
                        }
                        dialog.show()
                    }
                    return
                }
                preferenceEditObject.putInt("theme", id.toInt()).commit()
                enforceTheme(id.toInt())
                if (preferenceObject.getBoolean("defaultWallpaper",true)) {
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    val colorDrawable = ColorDrawable(Color.parseColor(curTheme[0]))
                    wallpaperManager.setBitmap(colorDrawable.toBitmap(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels))
                }
                toast(baseContext, "Setting theme to ${Constants().themeList[id.toInt()]}")
                recreate()
            }
            else printToConsole("Theme id out of range(-1 to ${Constants().themeList.size-1})", 7)
        }
        else {
            printToConsole("Theme id must be an integer", 5)
        }
    }

    private fun printToConsole(text: String, clr: Int, style: Int? = null) {
        val t = TextView(this)
        t.text = text
        t.setTextColor(Color.parseColor(curTheme[clr]))
        t.textSize = fontSize.toFloat()
        if (style != null) {
            t.setTypeface(cliTypeface, style)
        }
        else {
            t.typeface = cliTypeface
        }
        t.setTextIsSelectable(true)
        runOnUiThread {
            binding.terminalOutput.addView(t)
        }
        // if error then vibrate
        val vibrationPermission = preferenceObject.getBoolean("vibrationPermission",true)
        if (clr == 5 && vibrationPermission) {
            vibrate()
        }
    }

    private fun sleep(milliseconds: Long?) {
        if (milliseconds == null) {
            printToConsole("Invalid usage. 'sleep' command takes only 1 argument: time to sleep in milliseconds.",5)
            return
        }
        isSleeping = true
        runOnUiThread {
            binding.terminalOutput.addView(wakeBtn)
        }
        wakeBtn.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }
        binding.cmdInput.isEnabled = false
        sleepTimer = Timer().schedule(milliseconds) {
            isSleeping = false
            runOnUiThread {
                binding.terminalOutput.removeView(wakeBtn)
                binding.terminalOutput.removeViewAt(binding.terminalOutput.childCount - 1)  // remove command
                binding.cmdInput.isEnabled = true
                executeCommandsInQueue()
            }
        }
    }
    private fun executeCommandsInQueue() {
        while (commandQueue.isNotEmpty() && !isSleeping) {
            val cmdToExecute = commandQueue.removeFirst()
            cmdHandler(cmdToExecute)
        }
    }
    private fun yantraScripts() {
        // for user-defined scripts
        printToConsole("Opening Yantra Scripts...",4)
        val scripts = getScripts(preferenceObject)
        val scriptsMainDialog = MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
            .setTitle("Yantra Scripts")
        if (scripts.isNotEmpty()) {
            scriptsMainDialog.setItems(scripts.toTypedArray()) { _, which ->
                val scriptName = scripts.elementAt(which)
                val scriptEditor = MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                    .setTitle(scriptName)
                    .setMessage("View, Edit or Delete this script. Note: Enter 1 command per line, just as you normally enter in the Yantra terminal.")
                    .setView(R.layout.dialog_multiline_input)
                    .setCancelable(false)
                    .setPositiveButton("Save") { dialog, _ ->
                        val scriptBody = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
                        preferenceEditObject.putString("script_$scriptName", scriptBody).apply()
                        printToConsole("Script $scriptName saved successfully!",6)
                    }
                    .setNegativeButton("Delete") { _, _ ->
                        preferenceEditObject.remove("script_$scriptName").apply()
                        scripts.remove(scriptName)
                        preferenceEditObject.putString("scripts",scripts.joinToString(";")).apply()
                        printToConsole("Script '$scriptName' deleted.",6)
                    }
                    .setNeutralButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                scriptEditor.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("script_$scriptName",""))
            }
        }
        else {
            scriptsMainDialog.setMessage("No Scripts found! Try creating one.")
        }
        scriptsMainDialog.setPositiveButton("Add") { _, _ ->
            MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                .setTitle("New Script")
                .setMessage("Enter Script name")
                .setView(R.layout.dialog_singleline_input)
                .setPositiveButton("Create") { dialog, _ ->
                    val name = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString().trim()
                    if (name.contains(";") || name == "") {
                        printToConsole("Script name cannot contain ';' or be empty.", 5)
                    }
                    else if (!name[0].isLetter() || name.contains(' ')) {
                        printToConsole("Script name must start with a letter and cannot contain any spaces",5)
                    }
                    else if (scripts.contains(name)) {
                        printToConsole("This Name is already taken",7)
                    }
                    else {
                        scripts.add(name)
                        preferenceEditObject.putString("scripts",scripts.joinToString(";")).apply()
                        printToConsole("Script '$name' created successfully! You can now edit it.",6)
                        dialog.dismiss()
                    }
                }
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun runScript(args: List<String>) {
        if (args.size > 1) {
            printToConsole("Invalid number of arguments. See 'help run' for usage info.",5)
            return
        }
        val rcvdScriptName = args[0]
        val scripts = getScripts(preferenceObject)

        if (rcvdScriptName in scripts) {
            val scriptBody = preferenceObject.getString("script_$rcvdScriptName","") ?: ""
            val cmdsInScript = scriptBody.split("\n")
            cmdsInScript.forEach {
                cmdHandler(it.trim())
            }
        }
        else {
            printToConsole("Script '$rcvdScriptName' is not defined. Use 'scripts' command to create your own Yantra Scripts.",5)
            return
        }
    }

    private fun vibrate(millis: Long? = 100) {
        if (millis == null) {
            printToConsole("Invalid usage. 'vibe' command takes only 1 argument: time to vibrate in milliseconds.",5)
            return
        }
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(millis)
        }

    }

    private fun cmdDown() {
        binding.cmdInput.requestFocus()
        if (cmdHistoryCursor<(cmdHistory.size-1)) {
            cmdHistoryCursor++
            binding.cmdInput.setText(cmdHistory[cmdHistoryCursor])
            binding.cmdInput.setSelection(binding.cmdInput.text.length)
            requestCmdInputFocusAndShowKeyboard(this@MainActivity, binding)
        }
    }
    private fun cmdUp() {
        binding.cmdInput.requestFocus()
        if (cmdHistoryCursor>0) {
            cmdHistoryCursor--
            binding.cmdInput.setText(cmdHistory[cmdHistoryCursor])
            binding.cmdInput.setSelection(binding.cmdInput.text.length)
            requestCmdInputFocusAndShowKeyboard(this@MainActivity, binding)
        }
    }

    private fun listCmd(args: List<String>) {
        if (args.size > 1) {
            if (args[1].lowercase() == "apps") {
                printToConsole("Fetching apps...",4)
                listApps()
            }
            else if (args[1].lowercase() == "contacts") {
                printToConsole("Fetching Contacts...",4)
                getContacts()
            }
            else if (args[1].lowercase() == "themes") {
                listThemes()
            }
            else {
                printToConsole("${args[1]} is not a recognized parameter. Try using 'apps'.", 7)
            }
        }
        else {
            printToConsole("Please specify a list parameter", 5)
        }
    }
    private fun setUserName(name: String) {
        val newUsername = name.trim()
        preferenceEditObject.putString("username",newUsername).apply()
        binding.username.text = getUserNamePrefix(preferenceObject)+newUsername+">"
        printToConsole("Username set to $name", 6)
    }

    fun setAppLockPswd(newPass: String) {
        preferenceEditObject.putString("appLockPswd",newPass).apply()
    }
    private fun listThemes() {
        printToConsole("Available themes:", 4)
        printToConsole("-1: Custom", 4)
        for (i in Constants().themeList.indices) {
            printToConsole("$i: ${Constants().themeList[i]}", 4)
        }
    }
    private fun showHelp(argsP: List<String>) {
        var args = argsP
        for (i in 0 until args.count { it == ""}) {
            args = args.minusElement("")
        }
        val helpDescriptionArray = Constants().helpDescriptionArray
        when (args.size) {
            1 -> {
                printToConsole("---Yantra Launcher Help---",6, Typeface.BOLD_ITALIC)
                printToConsole("-------------------------",4)
                for (i in helpDescriptionArray.indices) {
                    printToConsole(helpDescriptionArray[i][0] ,7, Typeface.BOLD)
                    printToConsole(helpDescriptionArray[i][1] ,4)
                    printToConsole("-------------------------",4)
                }
                printToConsole("-------------------------",4)
                printToConsole("Enjoy ㄟ( ▔, ▔ )ㄏ",6, Typeface.BOLD_ITALIC)
            }
            2 -> {
                val cmd = args[1].trim().lowercase()
                for (i in helpDescriptionArray.indices) {
                    if (helpDescriptionArray[i][0].split(" ")[0] == cmd) {
                        printToConsole(helpDescriptionArray[i][0] ,7, Typeface.BOLD)
                        printToConsole(helpDescriptionArray[i][1] ,4)
                        printToConsole("-------------------------",4)
                        return
                    }
                }
                printToConsole("Command not found. Use 'help' to get list of available commands.", 5)
            }
            else -> {
                printToConsole("Invalid command usage. See 'help' for usage info", 5)
            }
        }
    }

    private fun showSystemInfo() {
        val actManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val availableMem = memInfo.availMem.toDouble() / (1024*1024) // Megabytes
        val totalMem = memInfo.totalMem.toDouble() / (1024*1024) // Megabytes

        val uptimeHours = SystemClock.uptimeMillis() / (1000*60*60) // Hours
        val uptimeMinutes = SystemClock.uptimeMillis() / (1000*60*60*60) // Minutes

        val widthRes = windowManager.defaultDisplay.width
        val heightRes = windowManager.defaultDisplay.height

        printToConsole("${getUserName(preferenceObject)}@YantraLauncher", 7)
        printToConsole("-------------------------", 7)
        printToConsole("--> OS: Android ${Build.VERSION.RELEASE}", 4)
        printToConsole("--> Host: ${Build.MANUFACTURER} ${Build.MODEL}", 4)
        printToConsole("--> Kernel: ${System.getProperty("os.version")}", 4)
        printToConsole("--> Uptime: ${uptimeHours}h ${uptimeMinutes}m", 4)
        printToConsole("--> Apps: ${appList.size + 1}", 4)  // +1 as Yantra Launcher not included in appList
        printToConsole("--> Terminal: Yantra Launcher ${BuildConfig.VERSION_NAME}", 4)
        printToConsole("--> Terminal Font: ${preferenceObject.getString("font", Constants().defaultFontName) ?: Constants().defaultFontName}", 4)
        printToConsole("--> Resolution: ${widthRes}x${heightRes}", 4)
        printToConsole("--> Theme: ${Constants().themeList[preferenceObject.getInt("theme",0)]}", 4)
        printToConsole("--> CPU: ${Build.SUPPORTED_ABIS[0]} (${Runtime.getRuntime().availableProcessors()}) @ ${getCPUSpeed()}", 4)
        printToConsole("--> Memory: ${availableMem.toInt()}MiB / ${totalMem.toInt()}MiB", 4)
        printToConsole("-------------------------", 7)
    }
    private fun alias(cmd: String) {
        if (cmd.trim() == "alias") {
            printToConsole("Aliases:", 7, Typeface.BOLD_ITALIC)
            printToConsole("-------------------------", 7)
            for (i in aliasList.indices) {
                printToConsole(aliasList[i][0] + " = " + aliasList[i][1], 4)
            }
            if (aliasList.size == 0) {
                printToConsole("No alias found.", 4)
            }
            printToConsole("-------------------------", 7)
            return
        }
        val cmdArray = cmd.trim().split(" ")
        if (cmdArray.size == 1) {
            printToConsole("Invalid command. Use 'alias' to get list of aliases.", 5)
            return
        }
        if (cmdArray.size >= 2) {
            if (cmdArray[1].trim() == "-1") {
                if (cmdArray.size > 2) {
                    printToConsole("Invalid command. See 'help' for usage info", 5)
                    return
                }
                // set aliasList to default
                aliasList = arrayListOf(arrayListOf("h", "help"),arrayListOf("o", "open"), arrayListOf("i", "info"), arrayListOf("u", "uninstall"), arrayListOf("bt", "bluetooth"), arrayListOf("w", "weather"), arrayListOf("tx", "termux"), arrayListOf("cls", "clear"))
                updateAliasList(aliasList, preferenceEditObject)
                printToConsole("Alias list set to default", 6)
                return
            }
            val aliasComponents = cmdArray.drop(1).joinToString(" ").split("=")
            if (aliasComponents.size == 1) {
                if (aliasComponents[0].split(" ").size > 1) {
                    printToConsole("Invalid command. See 'help' for usage info", 5)
                    return
                }
                val aliasName = aliasComponents[0].split(" ")[0].trim()
                for (i in aliasList.indices) {
                    if (aliasList[i][0] == aliasName) {
                        printToConsole("alias " + aliasName + " = " + aliasList[i][1], 4)
                        return
                    }
                }
                printToConsole("No alias found for '$aliasName'", 5)
                return
            }
            if (aliasComponents.size == 2) {
                val aliasName = aliasComponents[0].trim()
                if (aliasName.trim() in Constants().primarySuggestions) {
                    printToConsole("Alias name cannot be an existing command name.", 5)
                    return
                }
                // check if aliasName contains only alphanumeric characters and starts with a letter
                else if (!aliasName.matches(Regex("^[a-zA-Z][a-zA-Z0-9]*\$")) || aliasName.contains(" ")) {
                    printToConsole("Alias name must contain only alphanumeric characters and must start with a letter", 5)
                    return
                }
                val aliasCmd = aliasComponents[1].trim()
                for (i in aliasList.indices) {
                    if (aliasList[i][0] == aliasName) {
                        aliasList[i] = listOf(aliasName, aliasCmd)
                        updateAliasList(aliasList, preferenceEditObject)
                        printToConsole("Alias '$aliasName' updated.", 6)
                        return
                    }
                }
                aliasList.add(listOf(aliasName, aliasCmd))
                updateAliasList(aliasList, preferenceEditObject)
                printToConsole("Alias '$aliasName' added.", 6)
                return
            }
        }
        printToConsole("Invalid command. See 'help' to get usage info.", 5)
    }
    private fun unalias(cmd: String) {
        if (cmd.trim() == "unalias") {
            printToConsole("Invalid command. Use 'unalias alias_name' to remove alias", 5)
            return
        }
        val cmdArray = cmd.trim().split(" ")
        if (cmdArray.size == 1) {
            printToConsole("Invalid command. Use 'unalias alias_name' to remove alias", 5)
            return
        }
        if (cmdArray.size >= 2) {
            if (cmdArray[1].trim() == "-1") {
                // clear aliasList
                aliasList.clear()
                updateAliasList(aliasList, preferenceEditObject)
                printToConsole("Alias list cleared", 6)
                return
            }
            val aliasName = cmdArray[1].trim()
            for (i in aliasList.indices) {
                if (aliasList[i][0] == aliasName) {
                    aliasList.removeAt(i)
                    updateAliasList(aliasList, preferenceEditObject)
                    printToConsole("Alias '$aliasName' removed.", 6)
                    return
                }
            }
            printToConsole("No alias found for '$aliasName'", 5)
            return
        }
        printToConsole("Invalid command. See 'help' to get usage info.", 5)
    }

    private fun getAliasList(): MutableList<List<String>> {
        //get alias list from shared preferences
        val defaultAliasList = arrayListOf(arrayListOf("h", "help"),arrayListOf("o", "open"), arrayListOf("i", "info"), arrayListOf("u", "uninstall"), arrayListOf("bt", "bluetooth"), arrayListOf("w", "weather"), arrayListOf("tx", "termux"), arrayListOf("cls", "clear"))
        val defaultStringSet = mutableSetOf<String>()
        for (i in defaultAliasList.indices) {
            defaultStringSet.add(defaultAliasList[i][0] + "=" + defaultAliasList[i][1])
        }
        val aliasList = preferenceObject.getStringSet("aliasList", defaultStringSet)?.toMutableList()
        val aliasList2 = mutableListOf<List<String>>() //convert to list of list
        for (i in aliasList!!.indices) {
            aliasList2.add(listOf(aliasList[i].split("=")[0],aliasList[i].split("=")[1]))
        }
        return aliasList2
    }

    private fun runInTermux(cmd: String) {
        if (cmd == "") {
            printToConsole("Invalid command. See 'help termux' for usage info", 5)
            return
        }
        // check if termux is installed
        for (app in appList) {
            if (app.packageName == "com.termux") {
                // termux is installed
                val cmdName = cmd.split(" ")[0].trim()
                var cmdArgs = arrayOf<String>()
                if (cmd.split(" ").size > 1) {
                    cmdArgs = cmd.split(" ").drop(1).toTypedArray()
                }
                val cmdPath = preferenceObject.getString("termuxCmdPath", "/data/data/com.termux/files/usr/bin/")
                val cmdWorkDir = preferenceObject.getString("termuxCmdWorkDir", "/data/data/com.termux/files/home/")
                val cmdBackground = preferenceObject.getBoolean("termuxCmdBackground", false)
                val cmdSessionAction = preferenceObject.getInt("termuxCmdSessionAction", 0)
                val intent = Intent("com.termux.RUN_COMMAND").apply {
                    setClassName("com.termux", "com.termux.app.RunCommandService")
                    putExtra("com.termux.RUN_COMMAND_PATH", cmdPath + cmdName)
                    putExtra("com.termux.RUN_COMMAND_ARGUMENTS", cmdArgs)
                    putExtra("com.termux.RUN_COMMAND_WORKDIR", cmdWorkDir)
                    putExtra("com.termux.RUN_COMMAND_BACKGROUND", cmdBackground)
                    putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", cmdSessionAction)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    printToConsole("Running command in Termux...",6)
                    try {
                        startService(intent)
                    }
                    catch (e: Exception) {
                        printToConsole("Could not run command in Termux. Error: ${e.message}",5)
                    }
                }
                else {
                    printToConsole("Could not run command in Termux.",5)
                }
                return
            }
        }
        printToConsole("Termux is not installed.",5)
    }

    private fun email(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email.trim()))
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            printToConsole("Opened email app...",6)
        }
        else {
            toast(baseContext, "Could not open an email app.")
        }
    }
    private fun gupt(cmd: String) {
        printToConsole("Initializing G.U.P.T...",7)
        if (!preferenceObject.getBoolean("gupt___purchased",false)) {
            printToConsole("[-] G.U.P.T is a paid add-on feature. Consider buying it to enable it.",5)
            printToConsole("Salient features of G.U.P.T:",7, Typeface.BOLD)
            printToConsole("--------------------------",7)
            printToConsole("1. Open a private browsing tab inside Yantra Launcher.",4)
            printToConsole("2. All the data is cleared after closing the tab.",4)
            printToConsole("3. You can also open a specific url in the private tab.",4)
            printToConsole("4. Hidden from the recent apps list.",4)
            printToConsole("5. No history is saved.",4)
            printToConsole("6. No cookies are saved.",4)
            printToConsole("7. No more going through the hassle of opening an incognito tab in your browser.",4)
            printToConsole("--------------------------",7)
            initializeProductPurchase("gupt")
            return
        }
        printToConsole("Getting Undercover Private Tab...",4, Typeface.ITALIC)
        val cmdArray = cmd.split(" ")
        var url = "https://www.google.com"
        if (cmdArray.size > 1) {
            if (cmdArray.size > 2) {
                printToConsole("[-] Too many parameters! Usage: 'gupt' or 'gupt [url-here]'", 5)
                return
            }
            url = cmdArray[1]
        }
        startActivity(Intent(this, WebViewActivity::class.java).putExtra("url", url))
        printToConsole("[+] Opened G.U.P.T...",6)
    }
    private fun showBattery(args: List<String>) {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            baseContext.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct: Float = level / scale.toFloat()
        if (args.isEmpty()) {
            val charging: Boolean = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
            val health: String = when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                else -> "Unknown"
            }
            val temperature: Float = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.div(10f) ?: -1f
            val temperatureFahrenheit = String.format("%.1f", (temperature * 1.8f) + 32)
            printToConsole("Battery Status:", 6)
            printToConsole("-------------------------", 4)
            printToConsole("-> Level: ${(batteryPct*100).toInt()}%", 4)
            printToConsole("-> Charging: $charging", 4)
            printToConsole("-> Health: $health", 4)
            printToConsole("-> Temperature: $temperature°C ($temperatureFahrenheit°F)", 4)
            printToConsole("-------------------------", 4)
        }
        else if (args.size == 1 && args.first() == "-bar") {
            // show battery bar
            val totalBars = 10
            val filledBars = (batteryPct * totalBars).roundToInt()
            val emptyBars = totalBars - filledBars
            val filledBarSymbol = "|"
            val emptyBarSymbol = "x"

            val barString = "[" + filledBarSymbol.repeat(filledBars) + "${(batteryPct * 100).toInt()}%" + emptyBarSymbol.repeat(emptyBars) + "]"
            printToConsole(barString, 4)
        }
        else {
            printToConsole("Invalid args provided. See \"help battery\" for usage info",5)
        }
    }
    private fun getWeather(cityName: String) {
        val city = cityName.trim()
        printToConsole("Fetching weather report of $city...", 4, Typeface.ITALIC)
        val apiKey = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData["OPEN_WEATHER_API_KEY"]
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                printToConsole("-------------------------", 4)
                printToConsole("Weather report of $city", 6, Typeface.BOLD)
                val json = JSONObject(response)
                val weather = json.getJSONArray("weather").getJSONObject(0).getString("main")
                val temp = json.getJSONObject("main").getString("temp")
                val minTemp = json.getJSONObject("main").getString("temp_min")
                val maxTemp = json.getJSONObject("main").getString("temp_max")
                val humidity = json.getJSONObject("main").getString("humidity")
                val windSpeed = json.getJSONObject("wind").getString("speed").toFloat()
                val tempC = temp.toFloat().roundToInt() - 273
                printToConsole("=> $weather", 4)
                printToConsole("=> Temperature: $tempC°C (${tempC*9/5 +32}°F)", 4)
                printToConsole("=> Min: ${minTemp.toFloat().roundToInt() - 273}°C (${minTemp.toFloat().roundToInt().minus(273) * 9/5 +32}°F)", 4)
                printToConsole("=> Max: ${maxTemp.toFloat().roundToInt() - 273}°C (${maxTemp.toFloat().roundToInt().minus(273 ) * 9/5 +32}°F)", 4)
                printToConsole("=> Humidity: $humidity%", 4)
                printToConsole("=> Wind: ${(windSpeed * 3.6).roundToInt()} kmph", 4)
                printToConsole("-------------------------", 4)
            },
            { error ->
                if (error is NoConnectionError) {
                    printToConsole("No internet connection", 5)
                }
                //handle 404 error
                else if (error.networkResponse.statusCode == 404) {
                    printToConsole("Location not found", 7)
                }
                else {
                    printToConsole("An error occurred.",5)
                }
            })
        queue.add(stringRequest)
    }
    private fun getRandomActivity() {
        val url = "https://www.boredapi.com/api/activity/"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val json = JSONObject(response)
                val activity = json.getString("activity")
                val type = json.getString("type")
                val participants = json.getString("participants")
                printToConsole("-------------------------", 4)
                printToConsole("Random Activity", 6)
                printToConsole("=> $activity", 4)
                printToConsole("=> Type: $type", 4)
                printToConsole("=> Participants: $participants", 4)
                printToConsole("-------------------------", 4)
            },
            { error ->
                if (error is NoConnectionError) {
                    printToConsole("No internet connection", 5)
                }
                else {
                    printToConsole("An error occurred.",5)
                }
            })
        queue.add(stringRequest)
        printToConsole("Fetching random activity...", 4, Typeface.ITALIC)
    }

    private fun chatWithAi(message: String) {
        val url = "https://api.nova-oss.com/v1/chat/completions"
        val apiKey = preferenceObject.getString("aiApiKey", "") ?: ""
        val systemPrompt = preferenceObject.getString("aiSystemPrompt",Constants().aiSystemPrompt) ?: Constants().aiSystemPrompt
        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            })
        }

        // Create a Volley request
        val request = object: JsonObjectRequest(
            Method.POST,
            url,
            requestBody,
            { response ->
                // Handle the response here
                // You can access the response JSON using the 'response' parameter
                val jsonResponse = response.toString()
                val jsonObject = JSONObject(jsonResponse)

                if (jsonObject.has("choices")) {
                    // Extract the reply content
                    val choicesArray = jsonObject.getJSONArray("choices")
                    if (choicesArray.length() > 0) {
                        val firstChoice = choicesArray.getJSONObject(0)
                        val replyContent = firstChoice.getJSONObject("message").getString("content")

                        printToConsole(replyContent, 4, Typeface.ITALIC)
                    } else {
                        printToConsole("No reply found in the response", 5)
                    }
                }
                else {
                    printToConsole("The server did not send a chat reply! Try again.", 5)
                }
            },
            { error ->
                when (error) {
                    is NoConnectionError -> {
                        printToConsole("No internet connection", 5)
                    }

                    is TimeoutError -> {
                        printToConsole("Request Timed out. Try again or try a request with a shorter expected output.", 5)
                    }

                    is AuthFailureError -> {
                        printToConsole("Authentication Failed. Make sure you used the correct API key in 'settings'", 5)
                    }

                    else -> {
                        printToConsole("An error occurred: $error",5)
                    }
                }
            }
        )

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $apiKey"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        request.retryPolicy = DefaultRetryPolicy(1000 * 60 * 5, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        // Add the request to the Volley queue for execution
        val requestQueue = Volley.newRequestQueue(this) // 'this' is your activity or context
        requestQueue.add(request)
        printToConsole("Communicating with AI...", 4, Typeface.ITALIC)
    }

    private fun showTime() {
        val time = SimpleDateFormat("HH:mm:ss E d/M/y", Locale.getDefault()).format(Date())
        printToConsole(time, 4)
    }
    private fun openApp(appName: String) {
        val name = appName.trim().lowercase()
        val candidates = mutableListOf<AppBlock>()
        //wait till appList has been initialized
        for (app in appList) {
            if (app.appName.lowercase() == name) {
                candidates.add(app)
            }
        }
        if (candidates.size == 1) {
            applicationContext.startActivity(applicationContext.packageManager.getLaunchIntentForPackage(candidates[0].packageName))
            printToConsole("Opened ${candidates[0].appName}", 6)
            if (preferenceObject.getInt("appSortMode", Constants().appSortMode_alphabetically) == Constants().appSortMode_recency) {
                appList.remove(candidates[0])
                appList.add(0, candidates[0])
            }
        }
        else if (candidates.size > 1) {
            MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                .setTitle("Multiple apps found")
                .setMessage("Multiple apps found with name '$name'. Please select one.")
                .setPositiveButton("OK") { _, _ ->
                    val items = mutableListOf<String>()
                    for (app in candidates) {
                        items.add(app.packageName)
                    }
                    MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                        .setTitle("Select Package Name")
                        .setItems(items.toTypedArray()) { _, which ->
                            applicationContext.startActivity(applicationContext.packageManager.getLaunchIntentForPackage(candidates[which].packageName))
                            printToConsole("Opened ${candidates[which].appName}", 6)
                            if (preferenceObject.getInt("appSortMode", Constants().appSortMode_alphabetically) == Constants().appSortMode_recency) {
                                appList.remove(candidates[which])
                                appList.add(0, candidates[which])
                            }
                        }
                        .show()
                }
                .show()
        }
        else {
            printToConsole("'$name' app not found. Try using 'list apps' to get list of all app names.", 7)
        }
    }
    private fun openAppFuzzy(appName: String) {
        // function to open app by fuzzy search
        val name = appName.trim().lowercase()
        val candidates = mutableListOf<Double>()
        for (app in appList) {
            val score = findSimilarity(app.appName.lowercase(), name)
            candidates.add(score)
            //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
        }
        val maxIndex = candidates.indexOf(candidates.max())
        val appBlock = appList[maxIndex]
        applicationContext.startActivity(applicationContext.packageManager.getLaunchIntentForPackage(appBlock.packageName))
        printToConsole("Opened ${appList[maxIndex].appName}",6)
        if (preferenceObject.getInt("appSortMode", Constants().appSortMode_alphabetically) == Constants().appSortMode_recency) {
            appList.remove(appBlock)
            appList.add(0, appBlock)
        }
    }
    private fun openAppSettingsFuzzy(appName: String) {
        // function to open app by fuzzy search
        val name = appName.trim().lowercase()
        val candidates = mutableListOf<Double>()
        for (app in appList) {
            val score = findSimilarity(app.appName.lowercase(), name)
            candidates.add(score)
            //addToPrevTxt(app.appName+" ---> "+score.toString(),4)
        }
        val maxIndex = candidates.indexOf(candidates.max())
        val appBlock = appList[maxIndex]
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:"+appBlock.packageName)
        startActivity(intent)
        printToConsole("Opened settings for ${appList[maxIndex].appName}", 6)
    }



    private fun sendText(msg: String) {
        val  intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, msg.trim())
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, "Send via"))
        printToConsole("Text broadcasted",6)
    }

    private fun listApps() {
        printToConsole("Found ${appList.size} apps", 4)
        printToConsole("-------------------------", 4)
        for (app in appList) {
            printToConsole("""- ${app.appName}""", 4)
        }
    }

    private fun echo(inputString: String) {
        if (inputString.isEmpty()) {
            printToConsole("Invalid Syntax. See 'help echo' for usage info.", 5)
            return
        }
        else if (inputString.length <= 2) {
            // no mode specified. output in normal text
            binding.terminalOutput.removeViewAt(binding.terminalOutput.childCount - 1)  // remove command
            printToConsole(inputString, 4)
            return
        }
        else if (inputString[0] == '-' && inputString[1].isLetter() && inputString[2] == ' ') {
            // mode is specified. output in checked mode
            val mode = if (inputString[1] == 'e') {
                5
            } else if (inputString[1] == 'w') {
                7
            } else if (inputString[1] == 's') {
                6
            } else {
                printToConsole("Invalid mode Provided. Use 'e', 'w' or 's'.", 5)
                return
            }
            binding.terminalOutput.removeViewAt(binding.terminalOutput.childCount - 1)  // remove command
            printToConsole(inputString.removePrefix(inputString.substring(0,2)).trim(), mode)
        }
        else {
            // mode is not specified. output in normal text
            binding.terminalOutput.removeViewAt(binding.terminalOutput.childCount - 1)  // remove command
            printToConsole(inputString, 4)
            return
        }
    }

    private fun notify(message: String) {
        createNotificationChannel(this)
        val builder = NotificationCompat.Builder(this, Constants().userNotificationChannelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Yantra Launcher")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            printToConsole("Notifications permission missing!", 7)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), Constants().notificationsPermission)
            return
        }
        NotificationManagerCompat.from(this).notify(Constants().userNotificationId, builder.build())
        binding.terminalOutput.removeViewAt(binding.terminalOutput.childCount - 1)  // remove command
        printToConsole("Notification Fired!",6)
    }

    private fun calculate(expression: String) {
        try {
            val result = eval(expression)
            printToConsole(result.toString(), 4)
        }
        catch (e: RuntimeException) {
            printToConsole(e.message.toString(), 5)
        }
    }

    private fun searchGoogle(query: String) {
        //search in google app if installed
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, query.trim())
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
        //else search in browser
        else {
            val url = "https://www.google.com/search?q=${query.trim()}"
            openURL(url, this@MainActivity)
        }
        printToConsole("Searching '${query.trim()}' in Google...", 4, Typeface.ITALIC)
    }
    private fun searchDDG(query: String) {
        val url = "https://duckduckgo.com/?q=${query.trim()}"
        openURL(url, this@MainActivity)
        printToConsole("Searching '${query.trim()}' in Duck Duck Go...", 4, Typeface.ITALIC)
    }
    private fun searchBrave(query: String) {
        val url = "https://search.brave.com/search?q=${query.trim()}"
        openURL(url, this@MainActivity)
        printToConsole("Searching '${query.trim()}' in Brave...", 4, Typeface.ITALIC)
    }
    private fun toggleFlash(stateInput: String) {
        val state: Boolean = when (stateInput.lowercase()) {
            "on", "1" -> {
                true
            }
            "off", "0" -> {
                false
            }
            else -> {
                printToConsole("Toggle state not recognized. Try using 'on' | 'off' or 0 | 1.", 7)
                return
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraM = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraListId = cameraM.cameraIdList[0]
            if (state) {
                cameraM.setTorchMode(cameraListId, true)
                printToConsole("Flashlight turned on", 6)
            }
            else {
                cameraM.setTorchMode(cameraListId, false)
                printToConsole("Flashlight turned off", 6)
            }
        }
        else {
            printToConsole("Flashlight not supported on this device", 7)
        }
    }

    private fun uninstallApp(appName: String) {
        val name = appName.trim().lowercase()
        val candidates = mutableListOf<AppBlock>()
        //wait till appList has been initialized
        for (app in appList) {
            if (app.appName.lowercase() == name) {
                candidates.add(app)
            }
        }
        if (candidates.size == 1) {
            printToConsole("Requested to uninstall '${candidates[0].appName}'", 4)
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:"+candidates[0].packageName)
            startActivity(intent)
            uninstallCmdActive = true
        }
        else if (candidates.size > 1) {
            MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                .setTitle("Multiple apps found")
                .setMessage("Multiple apps found with name '$name'. Please select one.")
                .setPositiveButton("OK") { _, _ ->
                    val items = mutableListOf<String>()
                    for (app in candidates) {
                        items.add(app.packageName)
                    }
                    MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                        .setTitle("Select Package Name")
                        .setItems(items.toTypedArray()) { _, which ->
                            printToConsole("Requested to uninstall '${candidates[which].appName}'", 4)
                            val intent = Intent(Intent.ACTION_DELETE)
                            intent.data = Uri.parse("package:"+candidates[which].packageName)
                            startActivity(intent)
                            uninstallCmdActive = true
                        }
                        .show()
                }
                .show()
        }
        else {
            printToConsole("'$name' app not found. Try using 'list apps' to get list of all app names.", 7)
        }
    }
    private fun setBG(cmd: String) {
        if (cmd.trim() == "bg") {
            setupPermissions(this@MainActivity)
            // select image
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        else if (cmd.trim().split(" ")[1] == "-1") {
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            val colorDrawable = ColorDrawable(Color.parseColor(curTheme[0]))
            wallpaperManager.setBitmap(colorDrawable.toBitmap(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels))
            preferenceEditObject.putBoolean("defaultWallpaper",true).apply()
            printToConsole("Removed Wallpaper", 6)
        }
        else if (cmd.trim().split(" ")[1] == "random") {
            var query = "wallpaper"
            if (cmd.trim().split(" ").size == 3) {
                query = cmd.trim().split(" ")[2]
            }
            else if (cmd.trim().split(" ").size > 3) {
                printToConsole("Too many arguments. Use ',' to separate topics like 'bg random city,night-life'", 7)
                return
            }
            getRandomWallpaper(query)
        }
        else {
            printToConsole("Invalid argument passed for bg",5)
        }
    }
    private fun getRandomWallpaper(query: String = "") {
            val dimensions = "${resources.displayMetrics.widthPixels}x${resources.displayMetrics.heightPixels}"
            val url = "https://source.unsplash.com/random/$dimensions/?$query"
            val queue = Volley.newRequestQueue(this@MainActivity)
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    //get bitmap asynchronously and set it as wallpaper
                    Thread {
                        try {
                            val bitmap = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
                            val tintedBitmap = tintBitMap(bitmap, Color.parseColor("#33${curTheme[0].removePrefix("#")}"))  //33 = 20%
                            // set wallpaper from ui thread
                            runOnUiThread {
                                wallpaperManager.setBitmap(tintedBitmap)
                                preferenceEditObject.putBoolean("defaultWallpaper",false).apply()
                                printToConsole("Random Wallpaper applied!", 6)
                            }
                        }
                        catch (e: Exception) {
                            if (e is IOException) {
                                runOnUiThread { printToConsole("No internet connection", 5) }
                            }
                            else {
                                runOnUiThread { printToConsole("An error occurred.",5) }
                            }
                        }
                    }.start()
                },
                { error ->
                    if (error is NoConnectionError) {
                        printToConsole("No internet connection", 5)
                    } else {
                        printToConsole("An error occurred.", 5)
                    }
                })
            queue.add(stringRequest)
            printToConsole("Fetching random wallpaper...", 4)
    }
    private fun openYantraSettings() {
        yantraSettingsLauncher.launch(Intent(this, SettingsActivity::class.java))
    }
    private var yantraSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val settingsChanged = data.getBooleanExtra("settingsChanged", false)
                if (settingsChanged) {
                    recreate()
                }
            }
        }
    }

    private fun todo(cmd: String) {
        val args = cmd.split("\\s+".toRegex())
        val todolist = getToDo(preferenceObject)
        val todoProgressList = getToDoProgressList(todolist.size, preferenceObject)

        if (args.size == 1) {
            if (todolist.size == 0) {
                printToConsole("Enjoy! Nothing to do. ¯\\_(ツ)_/¯",7)
                printToConsole("Try adding an item like: todo Finish Game server config",4)
            }
            else {
                printToConsole("-------------------------", 4)
                printToConsole("TODO List:", 7, Typeface.BOLD)
                printToConsole("-------------------------", 4)
                for ((i, item) in todolist.withIndex()) {
                    val progress = todoProgressList[i]
                    var todoString = "$i:   $item"
                    if (progress > 0) {
                        todoString += " [${todoProgressList[i]}%]"
                    }
                    printToConsole(todoString, 4)
                }
            }
        }
        else if (args[1].trim() == "-1") {
            setToDo(mutableSetOf(), preferenceEditObject)
            setToDoProgress(arrayListOf(), preferenceEditObject)
            printToConsole("Todo list cleared", 6)
        }
        else if (args[1].trim().toIntOrNull() !== null && args.size == 2) {
            val index = args[1].trim().toInt()
            if (index >= todolist.size) {
                printToConsole("TODO List index out of range. Try a number less than ${todolist.size}.",5)
                return
            }
            else if (index < -1) {
                printToConsole("Invalid index provided. Use index of -1 to clear the list.",5)
                return
            }
            val toRemove = todolist.elementAt(index)
            todolist.remove(toRemove)
            todoProgressList.removeAt(index)
            setToDo(todolist, preferenceEditObject)
            setToDoProgress(todoProgressList, preferenceEditObject)
            printToConsole("Marked '$toRemove' as completed!",6)
        }
        else if (args.size == 3 && args[1].trim() == "-p" && args[2].trim() == "-1") {
            // set all % to 0
            for (i in 0 until todolist.size) {
                todoProgressList[i] = 0
            }
            setToDoProgress(todoProgressList, preferenceEditObject)
            printToConsole("Progress reset for all tasks!", 4)
            return
        }
        else if (args.size == 4 && args[1].trim() == "-p" && args[2].trim().toIntOrNull() != null && args[3].trim().toIntOrNull() != null) {
            val index = args[2].trim().toInt()
            val percent = args[3].trim().toInt()
            if (index >= todolist.size) {
                printToConsole("TODO List index out of range. Try a number less than ${todolist.size}.",5)
                return
            }
            if (index < 0) {
                printToConsole("Invalid command usage. Use 'todo -p -1' to reset progress for all tasks.", 5)
                return
            }
            if (percent < 0 || percent > 100) {
                printToConsole("Invalid progress value. Use a value from 0 to 100.", 5)
                return
            }
            if (percent == 100) {
                val toRemove = todolist.elementAt(index)
                todolist.remove(toRemove)
                todoProgressList.removeAt(index)
                setToDo(todolist, preferenceEditObject)
                setToDoProgress(todoProgressList, preferenceEditObject)
                printToConsole("Marked '$toRemove' as completed!",6)
                return
            }
            val toUpdate = todolist.elementAt(index)
            todoProgressList[index] = percent
            setToDoProgress(todoProgressList, preferenceEditObject)
            printToConsole("Marked '$toUpdate' as $percent% done!", 4)
            return
        }
        else {
            val toAdd = cmd.removePrefix(args[0]).trim()
            if (todolist.add(toAdd)) {
                todoProgressList.add(0)
                setToDo(todolist, preferenceEditObject)
                setToDoProgress(todoProgressList, preferenceEditObject)
                printToConsole("Added '$toAdd' to TODO List", 4)
            }
            else {
                printToConsole("Item already present in todo list", 7)
            }
        }
    }

    private fun init() {
        printToConsole("Opening Initialization Tasks for Yantra Launcher",4)
        val initListString = getInit(preferenceObject, preferenceEditObject)
        val initDialog = MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
            .setTitle("Initialization Tasks")
            .setMessage("Enter commands one-per-line to execute when Yantra Launcher gets in focus (opened or navigated-back to).")
            .setView(R.layout.dialog_multiline_input)
            .setCancelable(false)
            .setPositiveButton("Save") { dialog, _ ->
                val initTextBody = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
                val initListBody = initTextBody.trim()
                preferenceEditObject.putString("initList",initListBody).apply()
                printToConsole("Init List saved Successfully",6)
            }
            .setNegativeButton("Clear") { _, _ ->
                preferenceEditObject.putString("initList","").apply()
                printToConsole("Init List cleared",6)
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        initDialog.findViewById<EditText>(R.id.bodyText)?.setText(initListString)
    }

    @SuppressLint("MissingPermission")
    private fun toggleBT(stateInput: String) {
        val state: Boolean = when (stateInput.lowercase()) {
            "on", "1" -> {
                true
            }
            "off", "0" -> {
                false
            }
            else -> {
                printToConsole("Toggle state not recognized. Try using 'on' | 'off' or 0 | 1.", 7)
                return
            }
        }
        // code for android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                toast(baseContext, "The device doesn't support Bluetooth")
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                printToConsole("Bluetooth permission missing!", 7)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), Constants().bluetoothPermission)
                return
            }
            else {
                if (state) {
                    bluetoothAdapter?.enable()
                    printToConsole("Bluetooth turned on", 6)
                }
                else {
                    bluetoothAdapter?.disable()
                    printToConsole("Bluetooth turned off", 6)
                }
            }
        }
         else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                toast(baseContext, "The device doesn't support Bluetooth")
            }
            else {

                if (state) {
                    bluetoothAdapter.enable()
                    printToConsole("Bluetooth turned on", 6)
                }
                else {
                    bluetoothAdapter.disable()
                    printToConsole("Bluetooth turned off", 6)
                }
            }
        } else {
            toast(baseContext, "This feature requires Android 6 or higher")
        }
    }

    private fun backup() {
        printToConsole("Preparing Backup...", 4, Typeface.ITALIC)
        val username = getUserName(preferenceObject)
        val themeId = preferenceObject.getInt("theme", 0)
        val customThemeClrs = preferenceObject.getString("customThemeClrs", "#121212,#A0A0A0,#E1BEE7,#FAEBD7,#EBEBEB,#F00000,#00C853,#FFD600")
        // while restoring, check if plugin really purchased.
        val todo = getToDo(preferenceObject)
        val todoProgress = getToDoProgressList(todo.size, preferenceObject)
        val init = getInit(preferenceObject, preferenceEditObject)
        val alias = aliasList
        val scriptNames = getScripts(preferenceObject)
        val scriptBodies = ArrayList<String>()
        scriptNames.forEach {
            val scriptBody = preferenceObject.getString("script_$it","") ?: ""
            scriptBodies.add(scriptBody)
        }
        val usernamePrefix = preferenceObject.getString("usernamePrefix","$")?:"$"
        val getPrimarySuggestions = preferenceObject.getBoolean("getPrimarySuggestions",true)
        val getSecondarySuggestions = preferenceObject.getBoolean("getSecondarySuggestions",true)
        val fullscreenLauncher = preferenceObject.getBoolean("fullScreen",false)
        val vibrationPermission = preferenceObject.getBoolean("vibrationPermission",true)
        val showArrowKeys = preferenceObject.getBoolean("showArrowKeys",true)
        val oneTapKeyboardActivation = preferenceObject.getBoolean("oneTapKeyboardActivation",true)
        val hideKeyboardOnEnter = preferenceObject.getBoolean("hideKeyboardOnEnter", true)
        val actOnSuggestionTap = preferenceObject.getBoolean("actOnSuggestionTap", false)
        val doubleTapCommand = preferenceObject.getString("doubleTapCommand","lock")
        val newsWebsite = preferenceObject.getString("newsWebsite","https://news.google.com/")
        val fontSize = preferenceObject.getInt("fontSize",16)
        val orientation = preferenceObject.getInt("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        val appSugOrderingMode = preferenceObject.getInt("appSortMode", Constants().appSortMode_alphabetically)
        val fontName = preferenceObject.getString("font","Source Code Pro") ?: "Source Code Pro"
        // check if fontpack is purchased when restore command used.
        val termuxCmdPath = preferenceObject.getString("termuxCmdPath","/data/data/com.termux/files/usr/bin/")!!
        val termuxCmdWorkDir = preferenceObject.getString("termuxCmdWorkDir","/data/data/com.termux/files/home/")!!
        val termuxCmdSessionAction = preferenceObject.getInt("termuxCmdSessionAction",0)
        //val aiApiKey = preferenceObject.getString("aiApiKey","")!!
        val aiSystemPrompt = preferenceObject.getString("aiSystemPrompt",Constants().aiSystemPrompt)!!
    }

    private fun openAppSettings(appName: String) {
        val name = appName.trim().lowercase()
        val candidates = mutableListOf<AppBlock>()
        //wait till appList has been initialized
        for (app in appList) {
            if (app.appName.lowercase() == name) {
                candidates.add(app)
            }
        }
        if (candidates.size == 1) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:"+candidates[0].packageName)
            startActivity(intent)
            printToConsole("Opened settings for ${candidates[0].appName}", 6)
        }
        else if (candidates.size > 1) {
            MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                .setTitle("Multiple apps found")
                .setMessage("Multiple apps found with name '$name'. Please select one.")
                .setPositiveButton("OK") { _, _ ->
                    val items = mutableListOf<String>()
                    for (app in candidates) {
                        items.add(app.packageName)
                    }
                    MaterialAlertDialogBuilder(this, R.style.Theme_AlertDialog)
                        .setTitle("Select Package Name")
                        .setItems(items.toTypedArray()) { _, which ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:"+candidates[which].packageName)
                            startActivity(intent)
                            printToConsole("Opened settings for ${candidates[which].appName}", 6)
                        }
                        .show()
                }
                .show()
        }
        else {
            printToConsole("'$name' app not found. Try using 'list apps' to get list of all app names.", 7)
        }
    }
    private fun showQuote() {
        //read quotes from file
        val random = Random()
        val inputStream: InputStream = assets.open("quotes.txt")
        inputStream.bufferedReader().useLines { lines ->
            val quoteLine = lines.toList()[random.nextInt(1643)]  //1643 is the number of lines in the file
            val quote = quoteLine.split("%-%")[0]
            val author = quoteLine.split("%-%")[1]
            printToConsole(quote, 4, Typeface.ITALIC)
            printToConsole("      ~$author", 4, Typeface.ITALIC)
        }
    }
    private fun getContacts() {
        if (ContextCompat.checkSelfPermission(baseContext,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            printToConsole("Contacts permission missing!",7)
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                Constants().contactsPermission)
        }
        else {
            Thread {
                val contacts = fetchContacts()
                val len = contacts.count()
                runOnUiThread {
                    for (item in contacts) {
                        val name = item.name
                        val number = item.number
                        printToConsole(name,4)
                        printToConsole(number, 1)
                        printToConsole("-------------",4)
                    }
                    printToConsole("-------------",1)
                    printToConsole("Found $len Contacts",1)
                }
            }.start()
        }
    }

    private fun call(conName: String) {
        val name = conName.trim()
        if (ContextCompat.checkSelfPermission(baseContext,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            printToConsole("Contacts Permission missing!",7)
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                Constants().contactsPermission)
        }
        if (ContextCompat.checkSelfPermission(baseContext,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            printToConsole("Call Permission missing!",7)
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(Manifest.permission.CALL_PHONE),
                Constants().callPermission)
        }
        else {
            printToConsole("Resolving name...",4)
            Thread {
                fetchContacts(true, name.lowercase())
            }.start()
        }
    }

    private fun speakText(txt: String) {
        printToConsole("Initializing TTS engine", 4)
        ttsTxt = txt.trim()
        tts = TextToSpeech(baseContext,this)
    }

    private fun openCommunity() {
        openURL(Constants().discordCommunityURL, this@MainActivity)
        printToConsole("Opened Community Discord Server",6)
    }
    private fun fontPack() {
        if (preferenceObject.getBoolean("fontpack___purchased",false)) {
            printToConsole("Font Pack is already purchased. You can change the terminal font from settings.",6)
        }
        else {
            printToConsole("'fontpack' is not purchased!",5)
            printToConsole("--------------------------",7)
            printToConsole("Font Pack is an add-on for Yantra Launcher that lets you use any font from the entire collection of Google Fonts of more than 1550 fonts for your Yantra Launcher Terminal",4, Typeface.BOLD)
            printToConsole("--------------------------",7)
            initializeProductPurchase("fontpack")
        }
    }

    private fun exitApp() {
        printToConsole("Exiting app...", 4)
        finish()
    }

    @SuppressLint("Range")
    fun fetchContacts(callingIntent: Boolean = false, callTo: String = ""): List<Contacts> {
        contactsFetched = false
        var builder = ArrayList<Contacts>()
        // keep a list of contact names and their phone numbers whose name matches for calling
        val callingCandidates = ArrayList<String>()

        val resolver: ContentResolver = contentResolver
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
            null)

        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = (cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

                if (phoneNumber > 0) {
                    val cursorPhone = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone!!.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(
                                cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            builder.add(Contacts(name,phoneNumValue))
                            contactNames.add(name)
                            if (callingIntent && callTo == name.lowercase() && !callingCandidates.contains(phoneNumValue)) {
                                callingCandidates.add(phoneNumValue)
                            }
                        }
                    }
                    cursorPhone.close()
                }
            }
        } else {
            runOnUiThread { printToConsole("No contacts found!", 5) }
        }
        cursor.close()
        if (callingIntent) {
            if (callingCandidates.isEmpty()) {
                printToConsole("Contact name not found! Attempting to parse as phone number...", 4)
                runOnUiThread { printToConsole("Calling $callTo...", 6) }
                val intent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:${Uri.encode(callTo)}")
                )
                startActivity(intent)
            }
            else if (callingCandidates.size == 1) {
                    runOnUiThread { printToConsole("Calling $callTo...", 6) }
                    val intent = Intent(
                        Intent.ACTION_CALL,
                        Uri.parse("tel:${Uri.encode(callingCandidates.first())}")
                    )
                    startActivity(intent)
            }
            else {
                val dialog = MaterialAlertDialogBuilder(this@MainActivity,
                    R.style.Theme_AlertDialog
                )
                    .setTitle("Multiple Phone Numbers found")
                    .setMessage("Multiple Phone numbers with the name `$callTo` were found. Which one do you want to call?")
                    .setCancelable(false)
                    .setPositiveButton("Select") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        val dialog2 = MaterialAlertDialogBuilder(this@MainActivity,
                            R.style.Theme_AlertDialog
                        )
                            .setTitle("Select Phone Number")
                            .setCancelable(false)
                            .setItems(callingCandidates.toTypedArray()) { dialogInterface2, i ->
                                    runOnUiThread { printToConsole("Calling $callTo...", 6) }
                                    val intent = Intent(
                                        Intent.ACTION_CALL,
                                        Uri.parse("tel:${Uri.encode(callingCandidates[i])}")
                                    )
                                    startActivity(intent)
                                dialogInterface2.dismiss()
                            }
                            .setNegativeButton("Cancel") { dialogInterface2, _ ->
                                runOnUiThread { printToConsole("Cancelled...", 5) }
                                dialogInterface2.dismiss()
                            }
                        runOnUiThread { dialog2.show() }
                    }
                    .setNegativeButton("Cancel") { dialogInterface, _ ->
                        runOnUiThread { printToConsole("Cancelled...", 5) }
                        dialogInterface.dismiss()
                    }
                    runOnUiThread { dialog.show() }
            }
        }
        contactsFetched = true
        return builder.distinctBy { it.number }
    }

    private fun lockDevice() {
        printToConsole("Attempting to lock device...", 4)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // lock by Accessibility service (for Android 9 and above)
            lockDeviceByAccessibilityService(this@MainActivity, binding)
        }
        else {
            // lock by admin (for Android 8 and below)
            lockDeviceByAdmin(this@MainActivity)
        }
    }

    private fun initializeProductPurchase(skuId: String) {
        printToConsole("Initializing purchase...Please wait.",4)
        // check internet connection
        if (!isNetworkAvailable(this@MainActivity)) {
            printToConsole("No internet connection. Please connect to the internet and try again.",5)
            return
        }
        try {
            if (!billingClient.isReady) {
                billingClient.endConnection()
                billingClient = BillingClient.newBuilder(this)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build()
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready, query SKU details and launch purchase flow
                    val skuList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(skuId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build())
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(skuList)
                        .build()

                    billingClient.queryProductDetailsAsync(params) { billingResult1, skuDetailsList ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            // Handle retrieved SkuDetails objects here
                            val skuDetails = listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(skuDetailsList[0])
                                    .build()
                            )
                            val flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(skuDetails)
                                .build()

                            billingClient.launchBillingFlow(this@MainActivity, flowParams)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to reconnect to the billing service
                billingClient.startConnection(this)
            }
        })
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                toast(baseContext, "Purchase cancelled")
                printToConsole("[-] Purchase cancelled", 7)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                restoreAllPurchases()
                toast(baseContext, "Already purchased")
                printToConsole("[+] This item is already purchased. Please try again.", 7)
            } else {
                toast(baseContext, "Purchase failed")
                printToConsole("[-] Purchase failed. Please try again.", 5)
            }
            billingClient.endConnection()
        }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val isVerified = verifyValidSignature(purchase.originalJson, purchase.signature, baseContext, packageManager)
            if (!isVerified) {
                toast(baseContext, "Error : Invalid Purchase")
                return
            }
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                        if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                            preferenceObject.edit().putBoolean(JSONObject(purchase.originalJson).optString("productId")+"___purchased", true).apply()
                            runOnUiThread {
                                toast(baseContext, "Purchase successful")
                                printToConsole("[+] Purchase successful. Thank you for your support!", 6)
                            }
                        }
                    }
            }
            // else already purchased and acknowledged
            else {
                // grant entitlement to the user
                preferenceObject.edit().putBoolean(JSONObject(purchase.originalJson).optString("productId")+"___purchased", true).apply()
            }
        }
        else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            toast(baseContext, "Purchase is Pending. Please complete Transaction")
            printToConsole("Purchase is Pending. Please complete Transaction", 7)
        }
        else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            //if purchase is refunded or unknown
            preferenceObject.edit().putBoolean(JSONObject(purchase.originalJson).optString("productId")+"___purchased", false).apply()
            toast(baseContext, "Purchase failed")
            printToConsole("[-] Purchase failed. Please try again.", 5)
        }
    }
    private fun restoreAllPurchases() {
        val qpm = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        billingClient.queryPurchasesAsync(qpm) { _, p1 ->
            if (p1.isNotEmpty()) {
                for (purchase in p1) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    private fun createWakeButton() {
        wakeBtn = TextView(this@MainActivity)
        val spannable = SpannableString("Break")
        spannable.setSpan(UnderlineSpan(), 0, spannable.length, 0)
        wakeBtn.text = spannable
        wakeBtn.textSize = fontSize.toFloat()
        wakeBtn.setTextColor(Color.parseColor(curTheme[5]))
        wakeBtn.setOnClickListener {
            sleepTimer?.cancel()
            isSleeping = false
            binding.terminalOutput.removeView(wakeBtn)
            printToConsole("Yantra Launcher awakened mid-sleep (~_^)",5)
            binding.cmdInput.isEnabled = true
            executeCommandsInQueue()
        }
    }
}
