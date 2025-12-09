package com.coderGtm.yantra.commands.weather

import android.content.Context
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand

/**
 * Weather field definitions for user-accessible weather data.
 *
 * Note that only 38 fields exposed below out of 100+ available from API)
 *
 * To add new fields:
 *  1. Add WeatherField entry to appropriate category below
 *  2. Add string resource to values/strings.xml (and translate to all languages)
 *  3. Implement renderer function accessing WeatherResponse properties
 */

data class WeatherField(
    val key: String,
    val nameRes: Int,
    val isDefault: Boolean = false,
    val renderer: (WeatherResponse, BaseCommand) -> Unit,
) {
    fun getDescription(context: Context): String =
        context.getString(nameRes)
}

data class WeatherCategory(
    val nameRes: Int,
    val fields: List<WeatherField>,
) {
    fun displayFields(command: BaseCommand) {
        command.output("${command.terminal.activity.getString(nameRes)}:")
        fields.forEach { field ->
            command.output("  -${field.key.padEnd(12)} ${field.getDescription(command.terminal.activity)}")
        }
        command.output("")
    }
}

val WEATHER_FIELD_CATEGORIES = listOf(
    WeatherCategory(
        R.string.weather_category_current, listOf(
            WeatherField(
                "temp",
                R.string.weather_field_temp,
                isDefault = true
            ) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_temperature_c_f,
                        weather.current.tempC,
                        weather.current.tempF
                    )
                )
            },
            WeatherField(
                "feels",
                R.string.weather_field_feels,
                isDefault = true
            ) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_feels_like_c_f,
                        weather.current.feelslikeC,
                        weather.current.feelslikeF
                    )
                )
            },
            WeatherField("windchill", R.string.weather_field_windchill) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_windchill_c_f,
                        weather.current.windchillC,
                        weather.current.windchillF
                    )
                )
            },
            WeatherField("heatindex", R.string.weather_field_heatindex) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_heatindex_c_f,
                        weather.current.heatindexC,
                        weather.current.heatindexF
                    )
                )
            },
            WeatherField("dewpoint", R.string.weather_field_dewpoint) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_dewpoint_c_f,
                        weather.current.dewpointC,
                        weather.current.dewpointF
                    )
                )
            },
            WeatherField(
                "humidity",
                R.string.weather_field_humidity,
                isDefault = true
            ) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_humidity,
                        weather.current.humidity
                    )
                )
            },
            WeatherField(
                "wind",
                R.string.weather_field_wind,
                isDefault = true
            ) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_wind,
                        weather.current.windKph,
                        weather.current.windMph,
                        weather.current.windDir
                    )
                )
            },
            WeatherField("gust", R.string.weather_field_gust) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_gust,
                        weather.current.gustKph,
                        weather.current.gustMph
                    )
                )
            },
            WeatherField("pressure", R.string.weather_field_pressure) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_pressure,
                        weather.current.pressureMb
                    )
                )
            },
            WeatherField("uv", R.string.weather_field_uv) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_uv_index,
                        weather.current.uv
                    )
                )
            },
            WeatherField("visibility", R.string.weather_field_visibility) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_visibility,
                        weather.current.visKm
                    )
                )
            },
            WeatherField(
                "condition",
                R.string.weather_field_condition,
                isDefault = true
            ) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_condition,
                        weather.current.condition.text
                    )
                )
            },
            WeatherField("cloud", R.string.weather_field_cloud) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_cloud_cover,
                        weather.current.cloud
                    )
                )
            },
            WeatherField("precip", R.string.weather_field_precip) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_precipitation,
                        weather.current.precipMm
                    )
                )
            },
            WeatherField("solarrad", R.string.weather_field_solarrad) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_solar_radiation,
                        weather.current.shortRad,
                        weather.current.diffRad,
                        weather.current.dni,
                        weather.current.gti
                    )
                )
            }
        )
    ),
    WeatherCategory(
        R.string.weather_category_forecast, listOf(
            WeatherField("min", R.string.weather_field_min, isDefault = true) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_min_c_f,
                        weather.forecast.forecastday[0].day.mintempC,
                        weather.forecast.forecastday[0].day.mintempF
                    )
                )
            },
            WeatherField("max", R.string.weather_field_max, isDefault = true) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_max_c_f,
                        weather.forecast.forecastday[0].day.maxtempC,
                        weather.forecast.forecastday[0].day.maxtempF
                    )
                )
            },
            WeatherField("avgtemp", R.string.weather_field_avgtemp) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_avgtemp_c_f,
                        weather.forecast.forecastday[0].day.avgtempC,
                        weather.forecast.forecastday[0].day.avgtempF
                    )
                )
            },
            WeatherField("maxwind", R.string.weather_field_maxwind) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_maxwind,
                        weather.forecast.forecastday[0].day.maxwindKph,
                        weather.forecast.forecastday[0].day.maxwindMph
                    )
                )
            },
            WeatherField("totalprecip", R.string.weather_field_totalprecip) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_total_precipitation,
                        weather.forecast.forecastday[0].day.totalprecipMm
                    )
                )
            },
            WeatherField("totalsnow", R.string.weather_field_totalsnow) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_total_snow,
                        weather.forecast.forecastday[0].day.totalsnowCm
                    )
                )
            },
            WeatherField("avghumidity", R.string.weather_field_avghumidity) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_avg_humidity,
                        weather.forecast.forecastday[0].day.avghumidity
                    )
                )
            },
            WeatherField(
                "rain",
                R.string.weather_field_rain,
                isDefault = true
            ) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.precipitation_chance,
                        weather.forecast.forecastday[0].day.dailyChanceOfRain
                    )
                )
            },
            WeatherField(
                "snow",
                R.string.weather_field_snow,
                isDefault = true
            ) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.snow_chance,
                        weather.forecast.forecastday[0].day.dailyChanceOfSnow
                    )
                )
            }
        )
    ),
    WeatherCategory(
        R.string.weather_category_air_quality, listOf(
            WeatherField("aqi", R.string.weather_field_aqi, isDefault = true) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_air_quality,
                        getAqiText(weather.current.airQuality.usEpaIndex, command.terminal.activity)
                    )
                )
            },
            WeatherField("co", R.string.weather_field_co) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_co,
                        weather.current.airQuality.co
                    )
                )
            },
            WeatherField("no2", R.string.weather_field_no2) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_no2,
                        weather.current.airQuality.no2
                    )
                )
            },
            WeatherField("o3", R.string.weather_field_o3) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_o3,
                        weather.current.airQuality.o3
                    )
                )
            },
            WeatherField("so2", R.string.weather_field_so2) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_so2,
                        weather.current.airQuality.so2
                    )
                )
            },
            WeatherField("pm25", R.string.weather_field_pm25) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_pm25,
                        weather.current.airQuality.pm25
                    )
                )
            },
            WeatherField("pm10", R.string.weather_field_pm10) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_pm10,
                        weather.current.airQuality.pm10
                    )
                )
            }
        )
    ),
    WeatherCategory(
        R.string.weather_category_astronomy, listOf(
            WeatherField("sunrise", R.string.weather_field_sunrise) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_sunrise,
                        weather.forecast.forecastday[0].astro?.sunrise
                    )
                )
            },
            WeatherField("sunset", R.string.weather_field_sunset) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_sunset,
                        weather.forecast.forecastday[0].astro?.sunset
                    )
                )
            },
            WeatherField("moonrise", R.string.weather_field_moonrise) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_moonrise,
                        weather.forecast.forecastday[0].astro?.moonrise
                    )
                )
            },
            WeatherField("moonset", R.string.weather_field_moonset) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_moonset,
                        weather.forecast.forecastday[0].astro?.moonset
                    )
                )
            },
            WeatherField("moonphase", R.string.weather_field_moonphase) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_moon_phase,
                        weather.forecast.forecastday[0].astro?.moonPhase
                    )
                )
            },
            WeatherField("moonlight", R.string.weather_field_moonlight) { weather, command ->
                command.output(
                    command.terminal.activity.getString(
                        R.string.weather_moon_illumination,
                        weather.forecast.forecastday[0].astro?.moonIllumination
                    )
                )
            }
        )
    )
)

val VALID_WEATHER_FIELDS =
    WEATHER_FIELD_CATEGORIES.flatMap { it.fields.map { field -> field.key } }.toSet()

val DEFAULT_WEATHER_FIELDS = WEATHER_FIELD_CATEGORIES
    .flatMap { it.fields }
    .filter { it.isDefault }
    .map { it.key }

val WEATHER_FIELD_MAP = WEATHER_FIELD_CATEGORIES
    .flatMap { it.fields }
    .associateBy { it.key }

fun getAqiText(index: Int, context: Context): String {
    return with(context) {
        when (index) {
            1 -> getString(R.string.aqi_good)
            2 -> getString(R.string.aqi_moderate)
            3 -> getString(R.string.aqi_unhealthy_sensitive)
            4 -> getString(R.string.aqi_unhealthy)
            5 -> getString(R.string.aqi_very_unhealthy)
            6 -> getString(R.string.aqi_hazardous)
            else -> getString(R.string.aqi_unknown)
        }
    }
}