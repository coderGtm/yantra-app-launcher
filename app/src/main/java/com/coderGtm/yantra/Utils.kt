package com.coderGtm.yantra

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.view.inputmethod.InputMethodManager
import com.coderGtm.yantra.databinding.ActivityMainBinding

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