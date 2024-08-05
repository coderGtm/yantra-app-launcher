package com.coderGtm.yantra

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import com.coderGtm.yantra.blueprints.YantraLauncherDialog
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.models.Contacts
import com.coderGtm.yantra.models.Theme
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONArray
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Timer
import kotlin.concurrent.timerTask

fun openURL(url: String, activity: Activity) {
    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
fun toast(baseContext: Context, msg: String) {
    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
}
fun getUserNamePrefix(preferenceObject: SharedPreferences): String {
    return preferenceObject.getString("usernamePrefix","$")?:"$"
}
fun setUserNamePrefix(pre: String, preferenceEditObject: SharedPreferences.Editor) {
    preferenceEditObject.putString("usernamePrefix",pre).apply()
}
fun getUserName(preferenceObject: SharedPreferences): String {
    return preferenceObject.getString("username","root") ?: "root"
}
fun setSystemWallpaper(wallpaperManager: WallpaperManager, bitmap: Bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
    }
    else {
        wallpaperManager.setBitmap(bitmap)
    }
}
fun setWallpaperFromUri(uri: Uri?, activity: Activity, fallbackColor: Int, preferenceObject: SharedPreferences) {
    val bg: Drawable = try {
        val inputStream = activity.contentResolver.openInputStream(uri!!)
        Drawable.createFromStream(inputStream, uri.toString())!!
    } catch (e: FileNotFoundException) {
        fallbackColor.toDrawable()
    }
    val wallpaperManager = WallpaperManager.getInstance(activity)
    setSystemWallpaper(wallpaperManager, (bg as BitmapDrawable).bitmap)
    preferenceObject.edit().putBoolean("defaultWallpaper",false).apply()
}
fun requestCmdInputFocusAndShowKeyboard(activity: Activity, binding: ActivityMainBinding) {
    binding.cmdInput.requestFocus()
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(binding.cmdInput, InputMethodManager.SHOW_IMPLICIT)
}
@SuppressLint("Range")
fun contactsManager(terminal: Terminal, callingIntent: Boolean = false, callTo: String = ""): List<Contacts> {
    terminal.contactsFetched = false
    var builder = ArrayList<Contacts>()
    // keep a list of contact names and their phone numbers whose name matches for calling
    val callingCandidates = ArrayList<String>()

    val resolver: ContentResolver = terminal.activity.contentResolver
    val cursor = resolver.query(
        ContactsContract.Contacts.CONTENT_URI, null, null, null,
        null)

    if (cursor!!.count > 0) {
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val phoneNumber = (cursor.getString(
                cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

            if (phoneNumber > 0) {
                val cursorPhone = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                if(cursorPhone!!.count > 0) {
                    while (cursorPhone.moveToNext()) {
                        val phoneNumValue = cursorPhone.getString(
                            cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        builder.add(Contacts(name,phoneNumValue))
                        terminal.contactNames.add(name)
                        val phoneNumValueStandardized  = phoneNumValue.filterNot { it.isWhitespace() }
                        if (callingIntent && callTo == name.lowercase() && !callingCandidates.contains(phoneNumValueStandardized)) {
                            callingCandidates.add(phoneNumValueStandardized)
                        }
                    }
                }
                cursorPhone.close()
            }
        }
    } else {
        terminal.output(terminal.activity.getString(R.string.no_contacts_found), terminal.theme.errorTextColor, null)
    }
    cursor.close()
    if (callingIntent) {
        if (callingCandidates.isEmpty()) {
            terminal.output(terminal.activity.getString(R.string.contact_not_found_attempt_number), terminal.theme.resultTextColor, null)
            terminal.output(terminal.activity.getString(R.string.calling, callTo), terminal.theme.successTextColor, null)
            val intent = Intent(
                Intent.ACTION_CALL,
                Uri.parse("tel:${Uri.encode(callTo)}")
            )
            terminal.activity.startActivity(intent)
        }
        else if (callingCandidates.size == 1) {
            terminal.output(terminal.activity.getString(R.string.calling, callTo), terminal.theme.successTextColor, null)
            val intent = Intent(
                Intent.ACTION_CALL,
                Uri.parse("tel:${Uri.encode(callingCandidates.first())}")
            )
            terminal.activity.startActivity(intent)
        }
        else {
            if (!terminal.activity.isFinishing) {
                terminal.activity.runOnUiThread {
                    YantraLauncherDialog(terminal.activity).showInfo(
                        title = terminal.activity.getString(R.string.multiple_phone_numbers_found),
                        message = terminal.activity.getString(R.string.multiple_contacts_description, callTo),
                        cancellable = false,
                        positiveButton = terminal.activity.getString(R.string.select),
                        negativeButton = terminal.activity.getString(R.string.cancel),
                        positiveAction = {
                            val dialog2 = MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                                .setTitle(terminal.activity.getString(R.string.select_phone_number))
                                .setCancelable(false)
                                .setItems(callingCandidates.toTypedArray()) { dialogInterface2, i ->
                                    terminal.output(terminal.activity.getString(R.string.calling, callTo), terminal.theme.successTextColor, null)
                                    val intent = Intent(
                                        Intent.ACTION_CALL,
                                        Uri.parse("tel:${Uri.encode(callingCandidates[i])}")
                                    )
                                    terminal.activity.startActivity(intent)
                                    dialogInterface2.dismiss()
                                }
                                .setNegativeButton(terminal.activity.getString(R.string.cancel)) { dialogInterface2, _ ->
                                    terminal.output(terminal.activity.getString(R.string.cancelled), terminal.theme.errorTextColor, null)
                                    dialogInterface2.dismiss()
                                }
                            if (!terminal.activity.isFinishing) {
                                terminal.activity.runOnUiThread { dialog2.show() }
                            }
                        },
                        negativeAction = {
                            terminal.output(terminal.activity.getString(R.string.cancelled), terminal.theme.errorTextColor, null)
                        }
                    )
                }
            }
        }
    }
    terminal.contactsFetched = true
    return builder.distinctBy { it.number }
}
fun requestUpdateIfAvailable(preferenceObject: SharedPreferences, activity: Activity) {
    val lastUpdateCheck = preferenceObject.getLong("lastUpdateCheck", 0)
    if (System.currentTimeMillis()/60000 - lastUpdateCheck < 1440) {
        return
    }
    val appUpdateManager = AppUpdateManagerFactory.create(activity.baseContext)
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            if (!activity.isFinishing) {
                activity.runOnUiThread {
                    YantraLauncherDialog(activity).showInfo(
                        title = activity.getString(R.string.update_available),
                        message = activity.getString(R.string.update_available_description),
                        cancellable = false,
                        positiveButton = activity.getString(R.string.update),
                        negativeButton = activity.getString(R.string.not_now),
                        positiveAction = {
                            openURL(getStoreUrl(activity), activity)
                        }
                    )
                }
            }
        }
        preferenceObject.edit().putLong("lastUpdateCheck", System.currentTimeMillis()/60000).apply()
    }
}
private fun askRating(preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor, activity: Activity) {
    if (activity.isFinishing || !preferenceObject.getBoolean("ratePrompt",true)) {
        return
    }
    YantraLauncherDialog(activity).showInfo(
        title = activity.getString(R.string.rate_app),
        message = activity.getString(R.string.rate_app_description),
        cancellable = false,
        positiveButton = "Rate",
        negativeButton = activity.getString(R.string.don_t_ask_again),
        positiveAction = {
            openURL(getStoreUrl(activity), activity)
            preferenceEditObject.putBoolean("ratePrompt",false).apply()
        },
        negativeAction = {
            preferenceEditObject.putBoolean("ratePrompt",false).apply()
            toast(activity.baseContext, activity.getString(R.string.done_will_never_ask_again))
        },
        dismissAction = {
            toast(activity.baseContext, activity.getString(R.string.ok_with_face))
        }
    )
}
private fun showCommunityPopup(preferenceEditObject: SharedPreferences.Editor, activity: Activity) {
    val communityPopup = MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle(activity.getString(R.string.join_the_community))
        .setMessage(activity.getString(R.string.community_description))
        .setPositiveButton(activity.getString(R.string.take_me_there)) { dialog, _ ->
            openURL(DISCORD_COMMUNITY_URL, activity)
            dialog.dismiss()
        }
        .setNegativeButton("Reddit") { dialog, _ ->
            openURL(REDDIT_COMMUNITY_URL, activity)
            dialog.dismiss()
        }
        .setNeutralButton(activity.getString(R.string.no_thanks)) { dialog, _ ->
            dialog.dismiss()
            toast(activity.baseContext, activity.getString(R.string.we_d_miss_you))
        }
        .setCancelable(false)
        .create()
    communityPopup.setOnShowListener {
        val positiveButton = communityPopup.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = communityPopup.getButton(AlertDialog.BUTTON_NEGATIVE)
        val textSize = positiveButton.textSize.toInt()
        val drawable1 = AppCompatResources.getDrawable(activity, R.drawable.ic_discord)
        val drawable2 = AppCompatResources.getDrawable(activity, R.drawable.ic_reddit)
        val positiveDrawableResized = drawable1?.apply { setBounds(0, 0, textSize, textSize) }
        val negativeDrawableResized = drawable2?.apply { setBounds(0, 0, textSize, textSize) }
        // add drawable to button
        positiveButton.setCompoundDrawables(positiveDrawableResized, null, null, null)
        negativeButton.setCompoundDrawables(negativeDrawableResized, null, null, null)
    }

    if (!activity.isFinishing) {
        communityPopup.show()
    }

    preferenceEditObject.putBoolean("communityPopupShown",true).apply()
}
fun showRatingAndCommunityPopups(preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor, activity: Activity) {
    val n = preferenceObject.getLong("numOfCmdsEntered",0)
    if ((n+1)%40 == 0L && preferenceObject.getBoolean("ratePrompt",true)) {
        //askRating() after 5 seconds
        Timer().schedule(timerTask {
            activity.runOnUiThread {
                askRating(preferenceObject, preferenceEditObject, activity)
            }
        }, 4000)
        return
    }
    if ((n+1)>10 && !preferenceObject.getBoolean("communityPopupShown",false)) {
        //show community popup
        Timer().schedule(timerTask {
            activity.runOnUiThread {
                showCommunityPopup(preferenceEditObject, activity)
            }
        }, 4000)
        return
    }
}

fun promoteProVersion(terminal: Terminal, preferenceObject: SharedPreferences) {
    if (isPro(terminal.activity))   return
    val n = preferenceObject.getLong("numOfCmdsEntered",0)
    if ((n+1)%50 == 0L) {
        terminal.output(terminal.activity.getString(R.string.pro_prompt), terminal.theme.successTextColor, Typeface.BOLD_ITALIC)
        terminal.output(terminal.activity.getString(R.string.pro_cmd_info), terminal.theme.suggestionTextColor, Typeface.NORMAL)
    }
}
private fun getLevenshteinDistance(x: String, y: String): Int {
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
fun vibrate(millis: Long? = 100, activity: Activity) {
    if (millis == null) {
        return
    }
    val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        v.vibrate(millis)
    }
}
fun getCustomThemeColors(preferenceObject: SharedPreferences): ArrayList<String> {
    var colors =  preferenceObject.getString(
        "customThemeClrs",
        "#FF121212,#FFA0A0A0,#FF121212,#FFFAEBD7,#FFE1BEE7,#FFEBEBEB,#FFF00000,#FF00C853,#FFFFD600"
    )!!.split(",").toMutableList() as ArrayList<String>
    if (colors.size != 9) {
        colors = arrayListOf("#FF121212","#FFA0A0A0","#FF121212","#FFFAEBD7","#FFE1BEE7","#FFEBEBEB","#FFF00000","#FF00C853","#FFFFD600")
    }
    return colors
}
fun getCurrentTheme(activity: Activity, preferenceObject: SharedPreferences): Theme {
    if (!isPro(activity)) {
        preferenceObject.edit().putInt("theme", 0).apply()
    }
    val id = preferenceObject.getInt("theme", 0)
    if (id == -1) {
        val customThemeColors = getCustomThemeColors(preferenceObject)
        return Theme(
            bgColor = Color.parseColor(customThemeColors[0]),
            commandColor = Color.parseColor(customThemeColors[1]),
            suggestionBgColor = Color.parseColor(customThemeColors[2]),
            suggestionTextColor = Color.parseColor(customThemeColors[3]),
            inputLineTextColor = Color.parseColor(customThemeColors[4]),
            resultTextColor = Color.parseColor(customThemeColors[5]),
            errorTextColor = Color.parseColor(customThemeColors[6]),
            successTextColor = Color.parseColor(customThemeColors[7]),
            warningTextColor = Color.parseColor(customThemeColors[8])
        )
    }
    else if (Themes.entries.indices.contains(id)) {
        return Themes.entries[id].theme
    }
    else {
        return Themes.Default.theme
    }
}
fun isNetworkAvailable(activity: Activity): Boolean {
    val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}
fun getInit(preferenceObject: SharedPreferences): String {
    return try {
        preferenceObject.getString("initList", "") ?: ""
    } catch (e: ClassCastException) {
        // prev Set implementation present
        preferenceObject.edit().remove("initList").apply()
        ""
    }
}
fun runInitTasks(initList: String?, preferenceObject: SharedPreferences, terminal: Terminal) {
    if (initList?.trim() != "") {
        val initCmdLog = preferenceObject.getBoolean("initCmdLog", false)
        terminal.activity.runOnUiThread {
            initList?.lines()?.forEach {
                terminal.handleCommand(it.trim(), logCmd = initCmdLog)
            }
        }
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
fun setProStatus(activity: Activity, preferenceObject: SharedPreferences) {
    if (activity.packageName.endsWith(".pro") || activity.packageName.endsWith(".pro.debug") || activity.packageName.endsWith(".pro.beta")) {
        preferenceObject.edit().putBoolean("isPro", true).apply()
    }
    else {
        preferenceObject.edit().putBoolean("isPro", false).apply()
    }
}
fun isPro(activity: Activity): Boolean {
    return activity.getSharedPreferences(SHARED_PREFS_FILE_NAME,0).getBoolean("isPro", false)
}

fun informOfProVersionIfOldUser(activity: Activity) {
    if (isPro(activity))    return
    val prefObject = activity.getSharedPreferences(SHARED_PREFS_FILE_NAME, 0)

    // elementary vague check for old user
    val oldUser = (prefObject.getInt("theme", 0) != 0 || prefObject.getString("initList", "") != "" || prefObject.getString("scripts", "") != "" || prefObject.getStringSet("todoList", setOf())?.size != 0 || prefObject.getString("newsWebsite", "") != "") && !prefObject.getBoolean("minimalPromptShown", false)

    if (!activity.isFinishing && oldUser) {
        YantraLauncherDialog(activity).showInfo(
            title = activity.getString(R.string.yantra_launcher_has_been_trimmed),
            message = activity.getString(R.string.yantra_trim_description),
            cancellable = false,
            positiveButton = activity.getString(R.string.upgrade),
            negativeButton = activity.getString(R.string.later),
            positiveAction = {
                openURL(PLAY_STORE_URL_PRO, activity)
            }
        )
        prefObject.edit().putBoolean("minimalPromptShown", true).apply()
    }
}

fun getStoreUrl(activity: Activity): String {
    return if (isPro(activity)) {
        PLAY_STORE_URL_PRO
    }
    else {
        PLAY_STORE_URL
    }
}

fun copyFileToInternalStorage(activity: Activity, uri: Uri) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = activity.contentResolver.openInputStream(uri) ?: return
        val fileName = getFullName(uri, activity) ?: return
        val outputFile = File(activity.filesDir, fileName)
        outputStream = FileOutputStream(outputFile)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    } catch (ignored: IOException) {
    } finally {
        try {
            inputStream?.close()
            outputStream?.close()
        } catch (ignored: IOException) {
        }
    }
}

fun getFullName(uri: Uri, activity: Activity): String? {
    val contentResolver = activity.contentResolver
    val cursor: Cursor? = contentResolver.query(
        uri, null, null, null, null, null)

    cursor?.use {
        if (it.moveToFirst()) {
            val name = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (name >= 0) {
                val displayName: String =
                    it.getString(name)
                return displayName
            }
        }
    }

    return null
}

fun checkCroissantPermission(activity: Activity): Boolean {
    val contentResolver: ContentResolver = activity.contentResolver
    val uri = Uri.parse("content://com.anready.croissant.files")
        .buildUpon()
        .appendQueryParameter("command", "isPermissionsGranted") // Adding parameter command
        .build()

    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val dataIndex = cursor.getColumnIndex("response")
            if (dataIndex == -1) {
                println("Error while getting data!")
                return false
            }

            val jsonArray = JSONArray(cursor.getString(dataIndex))
            val fileInfo = jsonArray.getJSONObject(0)
            return fileInfo.getBoolean("result")
        } else {
            println("Error while getting data!")
        }
    } catch (e: Exception) {
        println("Error while getting data!\n" + e.message)
    } finally {
        cursor?.close()
    }
    return false
}