package com.coderGtm.yantra

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.view.inputmethod.InputMethodManager
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.models.Contacts
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

fun getUserNamePrefix(preferenceObject: SharedPreferences): String {
    return preferenceObject.getString("usernamePrefix","$")?:"$"
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
        terminal.output("No contacts found!", terminal.theme.errorTextColor, null)
    }
    cursor.close()
    if (callingIntent) {
        if (callingCandidates.isEmpty()) {
            terminal.output("Contact name not found! Attempting to parse as phone number...", terminal.theme.resultTextColor, null)
            terminal.output("Calling $callTo...", terminal.theme.successTextColor, null)
            val intent = Intent(
                Intent.ACTION_CALL,
                Uri.parse("tel:${Uri.encode(callTo)}")
            )
            terminal.activity.startActivity(intent)
        }
        else if (callingCandidates.size == 1) {
            terminal.output("Calling $callTo...", terminal.theme.successTextColor, null)
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
                .setTitle("Multiple Phone Numbers found")
                .setMessage("Multiple Phone numbers with the name `$callTo` were found. Which one do you want to call?")
                .setCancelable(false)
                .setPositiveButton("Select") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    val dialog2 = MaterialAlertDialogBuilder(terminal.activity,
                        R.style.Theme_AlertDialog
                    )
                        .setTitle("Select Phone Number")
                        .setCancelable(false)
                        .setItems(callingCandidates.toTypedArray()) { dialogInterface2, i ->
                            terminal.output("Calling $callTo...", terminal.theme.successTextColor, null)
                            val intent = Intent(
                                Intent.ACTION_CALL,
                                Uri.parse("tel:${Uri.encode(callingCandidates[i])}")
                            )
                            terminal.activity.startActivity(intent)
                            dialogInterface2.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialogInterface2, _ ->
                            terminal.output("Cancelled...", terminal.theme.errorTextColor, null)
                            dialogInterface2.dismiss()
                        }
                    terminal.activity.runOnUiThread { dialog2.show() }
                }
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    terminal.output("Cancelled...", terminal.theme.errorTextColor, null)
                    dialogInterface.dismiss()
                }
            terminal.activity.runOnUiThread { dialog.show() }
        }
    }
    terminal.contactsFetched = true
    return builder.distinctBy { it.number }
}
fun requestUpdateIfAvailable(preferenceObject: SharedPreferences, preferenceEditObject: SharedPreferences.Editor, activity: Activity) {
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
                .setTitle("Update Available")
                .setMessage("A new version of Yantra Launcher is available on the Play Store.")
                .setPositiveButton("Update") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.coderGtm.yantra")))
                }
                .setNegativeButton("Not now") {dialogInterface,_ ->
                    dialogInterface.dismiss()
                }
            activity.runOnUiThread { builder.show() }
        }
        preferenceEditObject.putLong("lastUpdateCheck", System.currentTimeMillis()/60000).apply()
    }
}