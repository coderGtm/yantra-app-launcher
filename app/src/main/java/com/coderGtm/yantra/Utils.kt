package com.coderGtm.yantra

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.models.Contacts
import com.coderGtm.yantra.models.Theme
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import java.io.FileNotFoundException
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
                        if (callingIntent && callTo == name.lowercase() && !callingCandidates.contains(phoneNumValue)) {
                            callingCandidates.add(phoneNumValue)
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
            val dialog = MaterialAlertDialogBuilder(terminal.activity,
                R.style.Theme_AlertDialog
            )
                .setTitle(terminal.activity.getString(R.string.multiple_phone_numbers_found))
                .setMessage(
                    terminal.activity.getString(
                        R.string.multiple_contacts_description,
                        callTo
                    ))
                .setCancelable(false)
                .setPositiveButton(terminal.activity.getString(R.string.select)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    val dialog2 = MaterialAlertDialogBuilder(terminal.activity,
                        R.style.Theme_AlertDialog
                    )
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
                    terminal.activity.runOnUiThread { dialog2.show() }
                }
                .setNegativeButton(terminal.activity.getString(R.string.cancel)) { dialogInterface, _ ->
                    terminal.output(terminal.activity.getString(R.string.cancelled), terminal.theme.errorTextColor, null)
                    dialogInterface.dismiss()
                }
            terminal.activity.runOnUiThread { dialog.show() }
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
            val builder = MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog)
                .setCancelable(false)
                .setTitle(activity.getString(R.string.update_available))
                .setMessage(activity.getString(R.string.update_available_description))
                .setPositiveButton(activity.getString(R.string.update)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URL)))
                }
                .setNegativeButton(activity.getString(R.string.not_now)) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
            activity.runOnUiThread { builder.show() }
        }
        preferenceObject.edit().putLong("lastUpdateCheck", System.currentTimeMillis()/60000).apply()
    }
}
private fun askRating(preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor, activity: Activity) {
    if (activity.isFinishing || !preferenceObject.getBoolean("ratePrompt",true)) {
        return
    }
    MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog)
        .setTitle(activity.getString(R.string.rate_app))
        .setMessage(activity.getString(R.string.rate_app_description))
        .setPositiveButton("Rate") { dialogInterface, _ ->
            dialogInterface.dismiss()
            openURL(PLAY_STORE_URL, activity)
            preferenceEditObject.putBoolean("ratePrompt",false).apply()
        }
        .setNegativeButton(activity.getString(R.string.maybe_later)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            toast(activity.baseContext, activity.getString(R.string.ok_with_face))
        }
        .setNeutralButton(activity.getString(R.string.don_t_ask_again)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            preferenceEditObject.putBoolean("ratePrompt",false).apply()
            toast(activity.baseContext, activity.getString(R.string.done_will_never_ask_again))
        }
        .setCancelable(false)
        .show()
}
private fun showCommunityPopup(preferenceEditObject: SharedPreferences.Editor, activity: Activity) {
    MaterialAlertDialogBuilder(activity, R.style.Theme_AlertDialog).setTitle(activity.getString(R.string.join_the_community))
        .setMessage(activity.getString(R.string.community_description))
        .setPositiveButton(activity.getString(R.string.take_me_there)) { dialog, _ ->
            openURL(DISCORD_COMMUNITY_URL, activity)
            dialog.dismiss()
        }
        .setNegativeButton(activity.getString(R.string.no_thanks)) { dialog, _ ->
            dialog.dismiss()
            toast(activity.baseContext, activity.getString(R.string.we_d_miss_you))
        }
        .setCancelable(false)
        .show()
    preferenceEditObject.putBoolean("communityPopupShown",true).apply()
}
fun showRatingAndCommandPopups(preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor, activity: Activity) {
    val n = preferenceObject.getLong("numOfCmdsEntered",0)
    if ((n+1)%40 == 0L) {
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
    return preferenceObject.getString(
        "customThemeClrs",
        "#000000,#A0A0A0,#E1BEE7,#FAEBD7,#EBEBEB,#F00000,#00C853,#FFD600"
    )!!.split(",").toMutableList() as ArrayList<String>
}
fun getCurrentTheme(preferenceObject: SharedPreferences): Theme {
    val id = preferenceObject.getInt("theme", 0)
    if (id == -1) {
        val customThemeColors = getCustomThemeColors(preferenceObject)
        return Theme(
            bgColor = Color.parseColor(customThemeColors[0]),
            commandColor = Color.parseColor(customThemeColors[1]),
            suggestionTextColor = Color.parseColor(customThemeColors[2]),
            buttonColor = Color.parseColor(customThemeColors[3]),
            resultTextColor = Color.parseColor(customThemeColors[4]),
            errorTextColor = Color.parseColor(customThemeColors[5]),
            successTextColor = Color.parseColor(customThemeColors[6]),
            warningTextColor = Color.parseColor(customThemeColors[7])
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