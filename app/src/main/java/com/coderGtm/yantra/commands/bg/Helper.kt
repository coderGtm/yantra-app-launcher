package com.coderGtm.yantra.commands.bg

import android.graphics.Bitmap
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.BitmapRequestListener
import com.coderGtm.yantra.R
import com.coderGtm.yantra.applyLauncherBackground
import com.coderGtm.yantra.setLauncherBackgroundBitmap

fun getRandomWallpaper(id: Int = -1, grayscale: Boolean = false, blur: Int = 0, command: Command) {
    val dimensions = "${command.terminal.activity.resources.displayMetrics.widthPixels}/${command.terminal.activity.resources.displayMetrics.heightPixels}"
    var url = "https://picsum.photos"
    if (id != -1) {
        url = url.plus("/id/$id")
    }
    url = url.plus("/$dimensions")
    if (grayscale) {
        url = url.plus("?grayscale")
    }
    if (blur > 0) {
        if (grayscale) {
            url = url.plus("&")
        }
        else {
            url = url.plus("?")
        }
        url = url.plus("blur=$blur")
    }

    command.output(command.terminal.activity.getString(R.string.fetching_random_wallpaper))

    AndroidNetworking.get(url)
        .build()
        .getAsBitmap(object : BitmapRequestListener {
            override fun onResponse(response: Bitmap?) {
                if (response != null) {
                    command.terminal.activity.runOnUiThread {
                        AndroidNetworking.evictAllBitmap()
                        if (setLauncherBackgroundBitmap(command.terminal.activity, response, command.terminal.preferenceObject)) {
                            applyLauncherBackground(command.terminal.activity, command.terminal.binding, command.terminal.preferenceObject, command.terminal.theme.bgColor)
                            command.output(command.terminal.activity.getString(R.string.random_wallpaper_applied), command.terminal.theme.successTextColor)
                        }
                        else {
                            command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again),command.terminal.theme.errorTextColor)
                        }
                    }
                }
                else {
                    AndroidNetworking.evictAllBitmap()
                    command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again),command.terminal.theme.errorTextColor)
                }
            }

            override fun onError(anError: ANError?) {
                AndroidNetworking.evictAllBitmap()
                if (anError?.errorCode != 0) {
                    command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again),command.terminal.theme.errorTextColor)
                    command.output("${anError?.errorCode}: ${anError?.errorBody} (${anError?.errorDetail})",command.terminal.theme.errorTextColor)
                }
                else {
                    command.output(anError.errorDetail ?: command.terminal.activity.getString(R.string.an_error_occurred_please_try_again),command.terminal.theme.errorTextColor)
                }
            }
        })
}