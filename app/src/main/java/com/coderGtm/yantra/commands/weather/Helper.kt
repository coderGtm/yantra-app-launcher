package com.coderGtm.yantra.commands.weather

import android.graphics.Typeface
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.coderGtm.yantra.R
import org.json.JSONObject
import kotlin.math.roundToInt

fun handleResponse(response: String, command: Command) {
    command.output("-------------------------")
    val json = JSONObject(response)
    try {
        val weather_location = json.getJSONObject("location").getString("name") + ", " + json.getJSONObject("location").getString("country")
        val current = json.getJSONObject("current")
        val condition = current.getJSONObject("condition").getString("text")
        val temp_c = current.getDouble("temp_c")
        val temp_f = current.getDouble("temp_f")
        val feelslike_c = current.getDouble("feelslike_c")
        val feelslike_f = current.getDouble("feelslike_f")
        val wind_kph = current.getDouble("wind_kph")
        val wind_mph = current.getDouble("wind_mph")
        val wind_dir = current.getString("wind_dir")
        val humidity = current.getDouble("humidity")
        val air_quality = current.getJSONObject("air_quality")
        val air_quality_index = air_quality.getInt("us-epa-index")
        val forecast = json.getJSONObject("forecast")
        val forecastDay = forecast.getJSONArray("forecastday").getJSONObject(0)
        val day = forecastDay.getJSONObject("day")
        val maxtemp_c = day.getDouble("maxtemp_c")
        val mintemp_c = day.getDouble("mintemp_c")
        val maxtemp_f = day.getDouble("maxtemp_f")
        val mintemp_f = day.getDouble("mintemp_f")
        val will_it_rain = day.getInt("daily_will_it_rain")
        val will_it_snow = day.getInt("daily_will_it_snow")
        val precipitation_chance = day.getInt("daily_chance_of_rain")
        val snow_chance = day.getInt("daily_chance_of_snow")
        command.output(command.terminal.activity.getString(R.string.weather_report_of, weather_location), command.terminal.theme.successTextColor, Typeface.BOLD)
        command.output("=> $condition")
        command.output(command.terminal.activity.getString(R.string.weather_temperature_c_f, temp_c, temp_f))
        command.output(command.terminal.activity.getString(R.string.weather_feels_like_c_f, feelslike_c, feelslike_f))
        command.output(command.terminal.activity.getString(R.string.weather_min_c_f, mintemp_c, mintemp_f))
        command.output(command.terminal.activity.getString(R.string.weather_max_c_f, maxtemp_c, maxtemp_f))
        if (command.terminal.preferenceObject.getBoolean("includeUvIndex", false)) {
            val uvi = current.getDouble("uv")
            command.output(command.terminal.activity.getString(R.string.weather_uv, uvi))
        }
        command.output(command.terminal.activity.getString(R.string.weather_humidity, humidity.roundToInt()))
        command.output(command.terminal.activity.getString(R.string.weather_wind, wind_kph, wind_mph, wind_dir))
        command.output(command.terminal.activity.getString(R.string.weather_air_quality, getAqiText(air_quality_index)))
        if (will_it_rain == 1) {
            command.output(command.terminal.activity.getString(R.string.precipitation_chance, precipitation_chance))
        }
        if (will_it_snow == 1) {
            command.output(command.terminal.activity.getString(R.string.snow_chance, snow_chance))
        }
    } catch (e: Exception) {
        command.output(command.terminal.activity.getString(R.string.an_error_occurred, e.message.toString()))
    }

    command.output("-------------------------")
}

fun handleError(error: VolleyError, command: Command) {
    if (error is NoConnectionError) {
        command.output(command.terminal.activity.getString(R.string.no_internet_connection), command.terminal.theme.errorTextColor)
    }
    else if (error.networkResponse.statusCode == 400) {
        command.output(command.terminal.activity.getString(R.string.location_not_found), command.terminal.theme.warningTextColor)
    }
    else {
        command.output(command.terminal.activity.getString(R.string.an_error_occurred_no_reason),command.terminal.theme.errorTextColor)
    }
}

fun getAqiText(index: Int): String {
    return when (index) {
        1 -> "Good"
        2 -> "Moderate"
        3 -> "Unhealthy for sensitive group"
        4 -> "Unhealthy"
        5 -> "Very Unhealthy"
        6 -> "Hazardous"
        else -> "Unknown"
    }
}