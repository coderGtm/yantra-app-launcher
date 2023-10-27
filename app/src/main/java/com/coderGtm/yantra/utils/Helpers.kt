package com.coderGtm.yantra.utils

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.WallpaperManager
import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.coderGtm.yantra.Constants
import com.coderGtm.yantra.R
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.databinding.ActivitySettingsBinding
import com.coderGtm.yantra.receivers.AdminReceiver
import com.coderGtm.yantra.services.YantraAccessibilityService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONObject
import java.io.IOException
import java.util.Timer
import kotlin.concurrent.schedule

fun eval(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0
        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number
        //        | functionName `(` expression `)` | functionName factor
        //        | factor `^` factor
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) x /= parseFactor() // division
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus
            var x: Double
            val startPos = pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                if (!eat(')'.code)) throw RuntimeException("Missing ')'")
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) { // functions
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, pos)
                if (eat('('.code)) {
                    x = parseExpression()
                    if (!eat(')'.code)) throw RuntimeException("Missing ')' after argument to $func")
                } else {
                    x = parseFactor()
                }
                x =
                    if (func == "sqrt") Math.sqrt(x) else if (func == "sin") Math.sin(
                        Math.toRadians(
                            x
                        )
                    ) else if (func == "cos") Math.cos(
                        Math.toRadians(x)
                    ) else if (func == "tan") Math.tan(Math.toRadians(x)) else throw RuntimeException(
                        "Unknown function: $func"
                    )
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            if (eat('^'.code)) x = Math.pow(x, parseFactor()) // exponentiation
            return x
        }
    }.parse()
}

fun setArrowKeysVisibility(preferenceObject: SharedPreferences, binding: ActivityMainBinding) {
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

fun defaultWallpaperManager(preferenceObject: SharedPreferences, applicationContext: Context, curTheme: ArrayList<String>, ) {
    if (preferenceObject.getBoolean("defaultWallpaper",true)) {
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        val colorDrawable = ColorDrawable(Color.parseColor(curTheme[0]))
        setSystemWallpaper(wallpaperManager, colorDrawable.toBitmap(applicationContext.resources.displayMetrics.widthPixels, applicationContext.resources.displayMetrics.heightPixels))
    }
}

fun goFullScreen(preferenceObject: SharedPreferences, activity: Activity) {
    if (preferenceObject.getBoolean("fullScreen",false)) {
        val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
        // Hide the system bars.
        windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
    }
}

fun getScripts(preferenceObject: SharedPreferences): java.util.ArrayList<String> {
    val scriptsFromSP = preferenceObject.getString("scripts", "").toString()
    val scripts = scriptsFromSP.split(";").toMutableList() as java.util.ArrayList<String>
    if (scripts.size == 1 && scripts[0].trim() == "") {
        scripts.clear()
    }

    return scripts
}

fun clearConsole(binding: ActivityMainBinding) {
    binding.terminalOutput.removeAllViews()
}

fun getUserName(preferenceObject: SharedPreferences): String {
    return preferenceObject.getString("username","root") ?: "root"
}

fun getUserNamePrefix(preferenceObject: SharedPreferences): String {
    return preferenceObject.getString("usernamePrefix","$")?:"$"
}

fun setUserNamePrefix(pre: String, preferenceEditObject: Editor) {
    preferenceEditObject.putString("usernamePrefix",pre).apply()
}

fun toast(baseContext: Context, msg: String) {
    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
}

fun updateAliasList(aliasList: MutableList<List<String>>, preferenceEditObject: SharedPreferences.Editor) {
    val aliasList2 = mutableListOf<String>()
    for (i in aliasList.indices) {
        aliasList2.add(aliasList[i][0] + "=" + aliasList[i][1])
    }
    preferenceEditObject.putStringSet("aliasList", aliasList2.toSet()).apply()
}

fun getLevenshteinDistance(x: String, y: String): Int {
    val m = x.length
    val n = y.length
    val t = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) {
        t[i][0] = i
    }
    for (j in 1..n) {
        t[0][j] = j
    }
    var cost: Int
    for (i in 1..m) {
        for (j in 1..n) {
            cost = if (x[i - 1] == y[j - 1]) 0 else 1
            t[i][j] = Integer.min(
                Integer.min(t[i - 1][j] + 1, t[i][j - 1] + 1),
                t[i - 1][j - 1] + cost
            )
        }
    }
    return t[m][n]
}

fun findSimilarity(x: String?, y: String?): Double {
    require(!(x == null || y == null)) { "Strings must not be null" }
    val maxLength = java.lang.Double.max(x.length.toDouble(), y.length.toDouble())
    return if (maxLength > 0) {
        // optionally ignore case if needed
        (maxLength - getLevenshteinDistance(x, y)) / maxLength
    } else 1.0
}

fun createNotificationChannel(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = Constants().userNotificationChannelName
        val descriptionText = Constants().userNotificationChannelDescription
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Constants().userNotificationChannelId, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun tintBitMap(bm: Bitmap, targetColor: Int): Bitmap {
    val mutableBitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
    val canvas = Canvas(mutableBitmap)
    val paint = Paint()
    paint.colorFilter = PorterDuffColorFilter(targetColor, PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(mutableBitmap, Matrix(), paint)
    return mutableBitmap
}

fun getToDo(preferenceObject: SharedPreferences): MutableSet<String> {
    return preferenceObject.getStringSet("todoList", setOf())!!
        .map { it.split(":", limit = 2).let { (index, value) -> index.toInt() to value } }
        .sortedBy { it.first }
        .mapTo(mutableSetOf()) { it.second }
}

fun getToDoProgressList(sizeOfTodo: Int, preferenceObject: SharedPreferences): java.util.ArrayList<Int> {
    if (sizeOfTodo == 0) return arrayListOf(0)
    var progress = arrayListOf<Int>()
    val ps = preferenceObject.getString("todoProgressList", "") ?: ""
    if (ps.trim() == "") {
        for (i in 0 until sizeOfTodo) {
            progress.add(0)
        }
        return progress
    }
    else {
        progress = ps.split(";").map {
            it.toInt()
        } as java.util.ArrayList<Int>
        if (progress.size == sizeOfTodo) {
            return progress
        }
        else {
            progress = arrayListOf()
            for (i in 0 until sizeOfTodo) {
                progress.add(0)
            }
            return progress
        }
    }
}

fun setToDoProgress(todoProgressList: java.util.ArrayList<Int>, preferenceEditObject: Editor) {
    val ps = todoProgressList.joinToString(";")
    preferenceEditObject.putString("todoProgressList", ps).apply()
}

fun setToDo(list: MutableSet<String>?, preferenceEditObject: Editor) {
    preferenceEditObject.putStringSet("todoList", list?.mapIndexedTo(mutableSetOf()) { index, s -> "$index:$s" }).apply()
}

fun getInit(preferenceObject: SharedPreferences, preferenceEditObject: Editor): String {
    return try {
        preferenceObject.getString("initList", "") ?: ""
    } catch (e: ClassCastException) {
        // prev Set implementation present
        preferenceEditObject.remove("initList").apply()
        ""
    }
}
fun incrementNumOfCommandsEntered(preferenceObject: SharedPreferences, preferenceEditObject: Editor, activity: Activity) {
    val n = preferenceObject.getLong("numOfCmdsEntered",0)
    preferenceEditObject.putLong("numOfCmdsEntered",n+1).apply()
    if ((n+1)%40 == 0L) {
        //askRating() after 5 seconds
        Timer().schedule(4000) {
            activity.runOnUiThread {
                askRating(preferenceObject, preferenceEditObject, activity)
            }
        }
        return
    }
    if ((n+1)>10 && !preferenceObject.getBoolean("communityPopupShown",false)) {
        //show community popup
        Timer().schedule(4000) {
            activity.runOnUiThread {
                showCommunityPopup(activity, preferenceEditObject)
            }
        }
    }
}

fun askRating(preferenceObject: SharedPreferences, preferenceEditObject: Editor, activity: Activity) {
    if (activity.isFinishing || !preferenceObject.getBoolean("ratePrompt",true)) {
        return
    }
    MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog)
        .setTitle("Rate app")
        .setMessage("If you like this app, please consider rating it and giving a feedback. You can also request features or report bugs. It helps me in improving the app. Thanks :)")
        .setPositiveButton("Rate") { dialogInterface, _ ->
            dialogInterface.dismiss()
            openURL("https://play.google.com/store/apps/details?id=com.coderGtm.yantra", activity)
            preferenceEditObject.putBoolean("ratePrompt",false).apply()
        }
        .setNegativeButton("Maybe Later") {dialogInterface,_ ->
            dialogInterface.dismiss()
            toast(activity.baseContext, "Ok ⊙﹏⊙∥")
        }
        .setNeutralButton("Don't ask again") {dialogInterface,_ ->
            dialogInterface.dismiss()
            preferenceEditObject.putBoolean("ratePrompt",false).apply()
            toast(activity.baseContext, "Done (￣┰￣*) Will never ask again!!")
        }
        .setCancelable(false)
        .show()
}

fun openURL(url: String, activity: Activity) {
    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

fun showCommunityPopup(activity: Activity, preferenceEditObject: Editor) {
    MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle("Join the community!")
        .setMessage("Join the community to get the latest updates about Yantra Launcher, ask questions, get help, discuss new features, and more!\n\nEveryone out there are CLI enthusiasts\uD83D\uDE0E like you, so join the community and have fun!")
        .setPositiveButton("Take me there") { dialog, _ ->
            openURL("https://discord.gg/sRZUG8rPjk", activity)
            dialog.dismiss()
        }
        .setNegativeButton("No thanks") { dialog, _ ->
            dialog.dismiss()
            toast(activity.baseContext, "We'd miss you!\n༼☯﹏☯༽")
        }
        .setCancelable(false)
        .show()
    preferenceEditObject.putBoolean("communityPopupShown",true).apply()
}

fun requestCommand(activity: Activity, packageManager: PackageManager) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:") // only email apps should handle this
        putExtra(Intent.EXTRA_EMAIL, arrayOf("coderGtm@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "Command request for Yantra Launcher")
        putExtra(Intent.EXTRA_TEXT, "I would like to request the following command for Yantra Launcher:\n\n[COMMAND]\n\n[DESCRIPTION]\n\n[EXAMPLE]\n\n[ANYTHING ELSE]")
    }
    if (intent.resolveActivity(packageManager) != null) {
        activity.startActivity(intent)
    } else {
        MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle("Oops!")
            .setMessage("Could not open an email app. Please send the mail to coderGtm@gmail.com with title 'Command request for Yantra Launcher'")
            .setPositiveButton("OK") { dialog, _ ->
                //copy title to clipboard
                val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Command request for Yantra Launcher", "Command request for Yantra Launcher")
                clipboard.setPrimaryClip(clip)
                toast(activity.baseContext, "Copied title to clipboard")
                dialog.dismiss()
            }
            .show()
    }
}

fun feedback(activity: Activity, packageManager: PackageManager) {
    MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog)
        .setTitle("Feedback")
        .setMessage("Thank you for choosing to give feedback! Your feedback fuels my motivation and helps me improve the app.\n\nYou can send feedback my mailing me at coderGtm@gmail.com or by giving a review on the Play Store.")
        .setPositiveButton("Email") { _, _ ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf("coderGtm@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Yantra Launcher")
            }
            if (intent.resolveActivity(packageManager) != null) {
                activity.startActivity(intent)
            } else {
                MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle("Oops!")
                    .setMessage("Could not open an email app. Please send the mail to coderGtm@gmail.com")
                    .setPositiveButton("OK", null).show()
            }
        }
        .setNegativeButton("Play Store") { _, _ ->
            openURL("https://play.google.com/store/apps/details?id=com.coderGtm.yantra", activity)
        }
        .setNeutralButton("Cancel", null)
        .show()
}

fun requestUpdateIfAvailable(preferenceObject: SharedPreferences, preferenceEditObject: Editor, activity: Activity) {
    val lastUpdateCheck = preferenceObject.getLong("lastUpdateCheck", 0)
    if (System.currentTimeMillis()/60000 - lastUpdateCheck < 1440) {
        return
    }
    val appUpdateManager = AppUpdateManagerFactory.create(activity.baseContext)
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog)
                .setCancelable(false)
                .setTitle("Update Available")
                .setMessage("A new version of Yantra Launcher is available on the Play Store.")
                .setPositiveButton("Update") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.coderGtm.yantra")))
                }
                .setNegativeButton("Not now") {dialogInterface,_ ->
                    dialogInterface.dismiss()
                }
                .show()
        }
        preferenceEditObject.putLong("lastUpdateCheck", System.currentTimeMillis()/60000).apply()
    }
}

fun setupPermissions(activity: Activity) {
    val permission = ContextCompat.checkSelfPermission(activity,
        Manifest.permission.READ_EXTERNAL_STORAGE)
    if (permission != PackageManager.PERMISSION_GRANTED) {
        makePermissionRequest(activity)
    }
}

fun makePermissionRequest(activity: Activity) {
    ActivityCompat.requestPermissions(activity,
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        Constants().storagePermission)
}

// function to check if Accessibility service is enabled (source: https://stackoverflow.com/a/56970606)
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val prefString = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return prefString != null && prefString.contains(context.packageName.toString() + "/" + YantraAccessibilityService::class.java.name)
}

fun lockDeviceByAccessibilityService(activity: Activity, binding: ActivityMainBinding) {
    if (isAccessibilityServiceEnabled(activity)) {
        binding.lockView.performClick()
    }
    else {
        MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle("Enable Locking Device")
            .setMessage("Please turn on Accessibility Service for Yantra Launcher for 'lock' command to work.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                activity.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

fun lockDeviceByAdmin(activity: Activity) {
    val pm = activity.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
    if (pm.isScreenOn) {
        val policy = activity.getSystemService(AppCompatActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        try {
            policy.lockNow()
        } catch (ex: SecurityException) {
            MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle("Enable Locking Device")
                .setMessage("Please enable device administrator for Yantra Launcher for Lock command to work.\n\nNote that for Android 8 and below, using lock from Yantra Launcher may prevent Biometric Authentication and use only PIN or Password for the next time you unlock your Device, due to Android API limitations.")
                .setPositiveButton("Open Settings") { dialog, _ ->
                    val admin = ComponentName(activity.baseContext, AdminReceiver::class.java)
                    val intent: Intent = Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN
                    ).putExtra(
                        DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin
                    )
                    activity.startActivity(intent)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}

fun verifyValidSignature(signedData: String, signature: String, context: Context, packageManager: PackageManager): Boolean {
    return try {
        // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
        val base64Key = packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA).metaData["LICENSE_KEY"] as String
        Security.verifyPurchase(base64Key, signedData, signature)
    } catch (e: IOException) {
        false
    }
}

fun isNetworkAvailable(activity: Activity): Boolean {
    val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun requestCmdInputFocusAndShowKeyboard(activity: Activity, binding: ActivityMainBinding) {
    binding.cmdInput.requestFocus()
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(binding.cmdInput, InputMethodManager.SHOW_IMPLICIT)
}

fun setOrientationTvText(binding: ActivitySettingsBinding, orientation: Int) {
    binding.tvOrientation.text = when (orientation) {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
            "Portrait"
        }
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
            "Landscape"
        }
        ActivityInfo.SCREEN_ORIENTATION_USER -> {
            "System"
        }
        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR -> {
            "Full Sensor"
        }
        else -> {
            "Unspecified"
        }
    }
}

fun setAppSugOrderTvText(binding: ActivitySettingsBinding, appSugOrderingMode: Int) {
    binding.tvAppSugOrder.text = when (appSugOrderingMode) {
        Constants().appSortModeAlphabetically -> {
            "Alphabetically"
        }
        Constants().appSortModeRecency -> {
            "Recency"
        }
        else -> {
            "Alphabetically"
        }
    }
}

fun changedSettingsCallback(activity: Activity) {
    val finishIntent = Intent()
    finishIntent.putExtra("settingsChanged",true)
    activity.setResult(AppCompatActivity.RESULT_OK,finishIntent)
}

fun openUsernamePrefixSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val usernamePrefixBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Username Prefix")
        .setMessage("Enter a prefix for your username. This will be displayed before your username in the terminal.")
        .setView(R.layout.username_prefix_dialog)
        .setPositiveButton("Save") { dialog, _ ->
            val prefix = (dialog as AlertDialog).findViewById<EditText>(R.id.usernameET)?.text.toString()
            setUserNamePrefix(prefix, preferenceEditObject)
            binding.usernamePrefix.text = getUserNamePrefix(preferenceObject)
            Toast.makeText(activity, "Username Prefix updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    usernamePrefixBuilder.findViewById<EditText>(R.id.usernameET)?.setText(getUserNamePrefix(preferenceObject))
}

fun openDoubleTapActionSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val doubleTapActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Double Tap Command")
        .setMessage("Enter a command to be executed when you double tap the terminal.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val command = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("doubleTapCommand",command.trim()).apply()
            Toast.makeText(activity, "Double-tap command updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    doubleTapActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("doubleTapCommand","lock"))
}

fun openNewsWebsiteSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val newsWebsiteBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Change News Website")
        .setMessage("Enter a website URL to be opened when you enter the \"news\" command.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val website = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("newsWebsite",website.trim()).apply()
            Toast.makeText(activity, "News website changed!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    newsWebsiteBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("newsWebsite","https://news.google.com/"))
}

fun openFontSizeSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val fontSizeBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Font Size")
        .setMessage("Enter a font size for the terminal.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val size = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            if (size.toIntOrNull() == null) {
                Toast.makeText(activity, "Invalid font size!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            preferenceEditObject.putInt("fontSize",size.toInt()).apply()
            binding.fontSizeBtn.text = size
            Toast.makeText(activity, "Font size updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    fontSizeBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getInt("fontSize",16).toString())
}

fun openOrientationSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceEditObject: Editor) {
    MaterialAlertDialogBuilder(activity)
        .setTitle("Set Terminal Orientation")
        .setItems(arrayOf("Portrait", "Landscape", "System", "Full Sensor")) { dialog, which ->
            when (which) {
                0 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    ).apply()
                    binding.tvOrientation.text = "Portrait"
                }

                1 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    ).apply()
                    binding.tvOrientation.text = "Landscape"
                }

                2 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_USER
                    ).apply()
                    binding.tvOrientation.text = "System"
                }

                3 -> {
                    preferenceEditObject.putInt(
                        "orientation",
                        ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
                    ).apply()
                    binding.tvOrientation.text = "Full Sensor"
                }
            }
            Toast.makeText(activity, "Orientation updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .show()
}

fun openAppSugOrderingSetter(activity: Activity, binding: ActivitySettingsBinding, preferenceEditObject: Editor) {
    MaterialAlertDialogBuilder(activity)
        .setTitle("App Suggestions Ordering")
        .setItems(arrayOf("Alphabetically", "Recency")) { dialog, which ->
            when (which) {
                0 -> {
                    preferenceEditObject.putInt("appSortMode", Constants().appSortModeAlphabetically).apply()
                    binding.tvAppSugOrder.text = "Alphabetically"
                }
                1 -> {
                    preferenceEditObject.putInt("appSortMode", Constants().appSortModeRecency).apply()
                    binding.tvAppSugOrder.text = "Recency"
                }
            }
            Toast.makeText(activity, "App Suggestions Ordering updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .show()
}

fun openTermuxCmdPathSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val termuxCmdPathBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Termux Command Path")
        .setMessage("Enter the path to the directory where the Termux command is located.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val path = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("termuxCmdPath",path.trim()).apply()
            Toast.makeText(activity, "Termux command path updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    termuxCmdPathBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("termuxCmdPath","/data/data/com.termux/files/usr/bin/")!!)
}

fun openTermuxCmdWorkingDirSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val termuxCmdWorkDirBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Termux Command Working Directory")
        .setMessage("Enter the path to the directory where the Termux command will be executed.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val path = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("termuxCmdWorkDir",path.trim()).apply()
            Toast.makeText(activity, "Termux command working directory updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    termuxCmdWorkDirBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("termuxCmdWorkDir","/data/data/com.termux/files/home/")!!)
}

fun openTermuxCmdSessionActionSelector(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val termuxCmdSessionActionBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("Termux Command Session Action")
        .setMessage("Enter integer for Termux Session Action. Available : 0,1,2,3. See TermuxConstants.java on GitHub for more info.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val action = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString().toIntOrNull()
            if (action == null || action !in 0..3) {
                Toast.makeText(activity, "Invalid action!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            preferenceEditObject.putInt("termuxCmdSessionAction",action).apply()
            Toast.makeText(activity, "Termux command session action updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .setNeutralButton("TermuxConstants.java") { _, _ ->
            openURL("https://github.com/termux/termux-app/blob/master/termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java#L1052-L1083", activity)
        }
        .show()
    termuxCmdSessionActionBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getInt("termuxCmdSessionAction",0).toString())
}

fun openAiApiKeySetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val aiApiKeyBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("AI API Key")
        .setMessage("Enter your API Key (NovaAI) for use in the 'ai' command.")
        .setView(R.layout.dialog_singleline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val key = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiApiKey",key.trim()).apply()
            Toast.makeText(activity, "AI API Key updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiApiKeyBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiApiKey","")!!)
}

fun openAiSystemPromptSetter(activity: Activity, preferenceObject: SharedPreferences, preferenceEditObject: Editor) {
    val aiSystemPromptBuilder = MaterialAlertDialogBuilder(activity)
        .setTitle("System Prompt")
        .setMessage("Enter The 'system' prompt for use in the 'ai' command. It dictates the behaviour of the model.")
        .setView(R.layout.dialog_multiline_input)
        .setPositiveButton("Save") { dialog, _ ->
            val prompt = (dialog as AlertDialog).findViewById<EditText>(R.id.bodyText)?.text.toString()
            preferenceEditObject.putString("aiSystemPrompt",prompt.trim()).apply()
            Toast.makeText(activity, "AI System Prompt updated!", Toast.LENGTH_SHORT).show()
            changedSettingsCallback(activity)
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
    aiSystemPromptBuilder.findViewById<EditText>(R.id.bodyText)?.setText(preferenceObject.getString("aiSystemPrompt",Constants().aiSystemPrompt)!!)
}

fun getCPUSpeed(): String {
    try {
        val process = ProcessBuilder("/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().use { reader ->
            val cpuSpeed = reader.readLine()
            if (!cpuSpeed.isNullOrBlank()) {
                val speedInKHz = cpuSpeed.trim().toInt()
                val speedInGHz = speedInKHz / 1000000.0
                return String.format("%.2f GHz", speedInGHz)
            }
        }
    } catch (e: Exception) {
        return "-- GHz"
    }
    return "-- GHz"
}

fun getBackupJSON(preferenceObject: SharedPreferences, timestamp: Long): JSONObject {
    val backup = JSONObject()
    backup.put("string", JSONObject())
    backup.put("int", JSONObject())
    backup.put("float", JSONObject())
    backup.put("boolean", JSONObject())
    backup.put("long", JSONObject())
    backup.put("set", JSONObject())
    backup.put("list", JSONObject())
    backup.put("map", JSONObject())

    // store all shared pref values in backup
    for (key in preferenceObject.all.keys) {
        when (val value = preferenceObject.all[key]) {
            is String -> {
                backup.getJSONObject("string").put(key, value)
            }
            is Int -> {
                backup.getJSONObject("int").put(key, value)
            }
            is Float -> {
                backup.getJSONObject("float").put(key, value)
            }
            is Boolean -> {
                backup.getJSONObject("boolean").put(key, value)
            }
            is Long -> {
                backup.getJSONObject("long").put(key, value)
            }
            is MutableSet<*> -> {
                backup.getJSONObject("set").put(key, value)
            }
            is MutableList<*> -> {
                backup.getJSONObject("list").put(key, value)
            }
            is MutableMap<*, *> -> {
                backup.getJSONObject("map").put(key, value)
            }
        }
        backup.put("timestamp", timestamp.toString())
    }

    return backup
}

fun restoreBackupJSON(jsonObject: JSONObject, preferenceEditObject: Editor) {
    val keys = jsonObject.keys()
    // json has data types as keys in which shared pref key-value pairs are stored
    // iterate over all data types
    while (keys.hasNext()) {
        val dataType = keys.next()
        val dataObject = jsonObject.getJSONObject(dataType)
        val dataKeys = dataObject.keys()
        // iterate over all key-value pairs of a data type
        while (dataKeys.hasNext()) {
            val key = dataKeys.next()
            when (dataType) {
                "int" -> {
                    preferenceEditObject.putInt(key, dataObject.getInt(key)).apply()
                }
                "float" -> {
                    preferenceEditObject.putFloat(key, dataObject.getDouble(key).toFloat()).apply()
                }
                "long" -> {
                    preferenceEditObject.putLong(key, dataObject.getInt(key).toLong()).apply()
                }
                "boolean" -> {
                    preferenceEditObject.putBoolean(key, dataObject.getBoolean(key)).apply()
                }
                "string" -> {
                    preferenceEditObject.putString(key, dataObject.getString(key)).apply()
                }
                "set" -> {
                    preferenceEditObject.putStringSet(key, dataObject.getString(key).split(",").toSet()).apply()
                }
                "list" -> {
                    preferenceEditObject.putString(key, dataObject.getString(key)).apply()
                }
                "map" -> {
                    preferenceEditObject.putString(key, dataObject.getString(key)).apply()
                }
            }
        }
    }
}

fun setSystemWallpaper(wallpaperManager: WallpaperManager, bitmap: Bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
    }
    else {
        wallpaperManager.setBitmap(bitmap)
    }
}
fun removePremiumValuesFromBackupJson(backup: JSONObject): JSONObject {
    Constants().premiumPluginsId.forEach {
        val key = "${it}___purchased"
        backup.remove(key)
    }
    return backup
}

fun getFileNameFromUri(uri: Uri, contentResolver: ContentResolver): String {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return if (cursor != null) {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            val fileName = cursor.getString(nameIndex)
            cursor.close()
            fileName
        } else {
            // Handle the case where DISPLAY_NAME is not available
            "unknown_file"
        }
    } else {
        // Handle the case where cursor is null
        "unknown_file"
    }
}

fun getFileExtensionFromUri(uri: Uri, contentResolver: ContentResolver): String {
    val fileName = getFileNameFromUri(uri, contentResolver)
    val fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length)
    return if (fileExtension == fileName) {
        ""
    } else {
        fileExtension
    }
}