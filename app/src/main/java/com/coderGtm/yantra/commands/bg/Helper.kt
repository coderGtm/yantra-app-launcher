package com.coderGtm.yantra.commands.bg

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.core.graphics.ColorUtils
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.coderGtm.yantra.R
import com.coderGtm.yantra.setSystemWallpaper
import java.io.IOException
import java.net.URL

fun getRandomWallpaper(query: String = "", command: Command) {
    val dimensions = "${command.terminal.activity.resources.displayMetrics.widthPixels}x${command.terminal.activity.resources.displayMetrics.heightPixels}"
    val url = "https://source.unsplash.com/random/$dimensions/?$query"
    val queue = Volley.newRequestQueue(command.terminal.activity)
    val stringRequest = StringRequest(
        Request.Method.GET, url,
        { response ->
            val wallpaperManager = WallpaperManager.getInstance(command.terminal.activity.applicationContext)
            //get bitmap asynchronously and set it as wallpaper
            Thread {
                try {
                    val bitmap = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())
                    val tintedBitmap = tintBitMap(bitmap, ColorUtils.setAlphaComponent(command.terminal.theme.bgColor, 33)) //33 = 20%
                    // set wallpaper from ui thread
                    command.terminal.activity.runOnUiThread {
                        setSystemWallpaper(wallpaperManager, tintedBitmap)
                        command.terminal.preferenceObject.edit().putBoolean("defaultWallpaper",false).apply()
                        command.output(command.terminal.activity.getString(R.string.random_wallpaper_applied), command.terminal.theme.successTextColor)
                    }
                }
                catch (e: Exception) {
                    if (e is IOException) {
                        command.output(command.terminal.activity.getString(R.string.no_internet_connection), command.terminal.theme.errorTextColor)
                    }
                    else {
                        command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again),command.terminal.theme.errorTextColor)
                    }
                }
            }.start()
        },
        { error ->
            if (error is NoConnectionError) {
                command.output(command.terminal.activity.getString(R.string.no_internet_connection), command.terminal.theme.errorTextColor)
            } else {
                command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again), command.terminal.theme.errorTextColor)
            }
        })
    queue.add(stringRequest)
    command.output(command.terminal.activity.getString(R.string.fetching_random_wallpaper))
}
fun tintBitMap(bm: Bitmap, targetColor: Int): Bitmap {
    val mutableBitmap = bm.copy(Bitmap.Config.ARGB_8888, true);
    val canvas = Canvas(mutableBitmap)
    val paint = Paint()
    paint.colorFilter = PorterDuffColorFilter(targetColor, PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(mutableBitmap, Matrix(), paint)
    return mutableBitmap
}