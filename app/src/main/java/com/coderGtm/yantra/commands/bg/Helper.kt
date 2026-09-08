package com.coderGtm.yantra.commands.bg

import android.graphics.BitmapFactory
import com.coderGtm.yantra.R
import com.coderGtm.yantra.applyLauncherBackground
import com.coderGtm.yantra.network.HttpClientProvider
import com.coderGtm.yantra.setLauncherBackgroundBitmap
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun isValidBlur(value: Int): Boolean = value in 1..10

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

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bytes = HttpClientProvider.client.get(url).bodyAsBytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            withContext(Dispatchers.Main) {
                if (bitmap != null && setLauncherBackgroundBitmap(command.terminal.activity, bitmap, command.terminal.preferenceObject)) {
                    applyLauncherBackground(command.terminal.activity, command.terminal.binding, command.terminal.preferenceObject, command.terminal.theme.bgColor)
                    command.output(command.terminal.activity.getString(R.string.random_wallpaper_applied), command.terminal.theme.successTextColor)
                } else {
                    command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again), command.terminal.theme.errorTextColor)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val errorText = e.message?.takeIf { it.isNotBlank() } ?: e::class.simpleName.orEmpty()
                val statusText = (e as? ResponseException)?.response?.status?.toString()
                if (statusText != null) {
                    command.output(command.terminal.activity.getString(R.string.an_error_occurred_please_try_again), command.terminal.theme.errorTextColor)
                    command.output("$statusText: $errorText", command.terminal.theme.errorTextColor)
                } else {
                    command.output(errorText.ifBlank { command.terminal.activity.getString(R.string.an_error_occurred_please_try_again) }, command.terminal.theme.errorTextColor)
                }
            }
        }
    }
}
