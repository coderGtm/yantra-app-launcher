package com.coderGtm.yantra.commands.weather

import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatDelegate
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

private var weatherJob: Job? = null

/**
 * Fetches weather data from the WeatherAPI for the specified location.
 *
 * @param args The [WeatherCommandArgs] containing the location for which to fetch weather data.
 * @param command The [BaseCommand] instance.
 */
fun fetchWeatherData(args: WeatherCommandArgs, command: BaseCommand) {
    val location = args.location

    val langCode = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    command.output(
        command.terminal.activity.getString(R.string.fetching_weather_report_of, location),
        command.terminal.theme.resultTextColor,
        Typeface.ITALIC
    )

    val apiKey = command.terminal.activity.packageManager.getApplicationInfo(
        command.terminal.activity.packageName,
        PackageManager.GET_META_DATA
    ).metaData.getString("WEATHER_API_KEY")

    val url =
        "https://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$location&lang=$langCode&aqi=yes"

    weatherJob?.cancel()
    weatherJob = CoroutineScope(Dispatchers.Main).launch {
        try {
            ensureActive()
            val weather = withContext(Dispatchers.IO) {
                HttpClientProvider.client.get(url).body<WeatherResponse>()
            }
            handleResponse(weather, args, command)
        } catch (e: Exception) {
            if (e is CancellationException) return@launch
            handleKtorError(e, command)
        }
    }
}

/**
 * Handles the error response from the WeatherAPI.
 */
internal suspend fun handleKtorError(error: Exception, command: BaseCommand) {
    when (error) {
        is ClientRequestException -> {
            val apiError = parseErrorResponse(error)
            val stringRes = getWeatherApiErrorStringRes(apiError, error.response.status.value)
            command.output(
                command.terminal.activity.getString(stringRes),
                command.terminal.theme.errorTextColor
            )
        }

        is ConnectException, is UnknownHostException -> {
            command.output(
                command.terminal.activity.getString(R.string.no_internet_connection),
                command.terminal.theme.errorTextColor
            )
        }

        else -> {
            command.output(
                command.terminal.activity.getString(R.string.an_error_occurred_no_reason),
                command.terminal.theme.errorTextColor
            )
        }
    }
}

/**
 * Convenience function to parse the error response from the WeatherAPI.
 */
internal suspend fun parseErrorResponse(
    exception: ClientRequestException
): WeatherApiError? = withContext(Dispatchers.IO) {
    try {
        exception.response.body<WeatherErrorResponse>().error
    } catch (_: Exception) {
        null
    }
}

/**
 * Convenience function to get the appropriate error string resource based on the API error code.
 */
internal fun getWeatherApiErrorStringRes(
    apiError: WeatherApiError?,
    statusCode: Int
): Int = when (apiError?.code) {
        1002 -> R.string.weather_api_key_not_provided
        1003 -> R.string.weather_location_parameter_missing
        1005 -> R.string.weather_api_request_invalid
        1006 -> R.string.weather_location_not_found
        2006 -> R.string.weather_api_key_invalid
        2007 -> R.string.weather_quota_exceeded
        2008 -> R.string.weather_api_disabled
        2009 -> R.string.weather_api_access_restricted
        9000 -> R.string.weather_bulk_request_invalid
        9001 -> R.string.weather_bulk_too_many_locations
        9999 -> R.string.weather_internal_error
        else -> getGenericErrorForStatus(statusCode)
    }

/**
 * Convenience function to get the appropriate error string resource based on the HTTP status code.
 */
private fun getGenericErrorForStatus(statusCode: Int): Int {
    return when (statusCode) {
        400 -> R.string.weather_location_not_found
        401 -> R.string.weather_api_key_invalid
        403 -> R.string.weather_quota_exceeded
        else -> R.string.weather_unknown_error
    }
}

/**
 * Handles the successful response from the WeatherAPI.
 */
private fun handleResponse(
    weather: WeatherResponse,
    args: WeatherCommandArgs,
    command: BaseCommand,
) {
    command.output("-------------------------")
    with(command.terminal.activity) {
        try {
            val location = "${weather.location.name}, ${weather.location.country}"
            command.output(
                getString(R.string.weather_report_of, location),
                command.terminal.theme.successTextColor,
                Typeface.BOLD
            )

            val fieldsToShow = if (args.showDefaultFields) {
                DEFAULT_WEATHER_FIELDS
            } else {
                args.requestedFields
            }

            fieldsToShow.forEach { fieldKey ->
                WEATHER_FIELD_MAP[fieldKey]?.renderer?.invoke(weather, command)
            }
        } catch (e: Exception) {
            command.output(
                getString(
                    R.string.an_error_occurred,
                    e.message.toString()
                )
            )
        }
    }

    command.output("-------------------------")
}