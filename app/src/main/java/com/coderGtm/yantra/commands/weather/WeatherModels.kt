/**
 * Complete data models for WeatherAPI.com response.
 *
 * Note that these models map the full API response, but only a subset of fields are currently exposed
 * to users. See [WeatherField].kt for more info and instructions on adding new user-accessible
 * fields.
 *
 * [https://github.com/Kotlin/kotlinx.serialization/issues/2844](More info) about why the
 * opt-in annotation is needed.
 */
@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.coderGtm.yantra.commands.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

@Serializable
data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    @SerialName("tz_id") val tzId: String,
    @SerialName("localtime_epoch") val localtimeEpoch: Long,
    val localtime: String
)

@Serializable
data class Current(
    @SerialName("last_updated_epoch") val lastUpdatedEpoch: Long? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("temp_f") val tempF: Double,
    @SerialName("is_day") val isDay: Int? = null,
    val condition: Condition,
    @SerialName("wind_mph") val windMph: Double,
    @SerialName("wind_kph") val windKph: Double,
    @SerialName("wind_degree") val windDegree: Int? = null,
    @SerialName("wind_dir") val windDir: String,
    @SerialName("pressure_mb") val pressureMb: Double? = null,
    @SerialName("pressure_in") val pressureIn: Double? = null,
    @SerialName("precip_mm") val precipMm: Double? = null,
    @SerialName("precip_in") val precipIn: Double? = null,
    val humidity: Int,
    val cloud: Int? = null,
    @SerialName("feelslike_c") val feelslikeC: Double,
    @SerialName("feelslike_f") val feelslikeF: Double,
    @SerialName("windchill_c") val windchillC: Double? = null,
    @SerialName("windchill_f") val windchillF: Double? = null,
    @SerialName("heatindex_c") val heatindexC: Double? = null,
    @SerialName("heatindex_f") val heatindexF: Double? = null,
    @SerialName("dewpoint_c") val dewpointC: Double? = null,
    @SerialName("dewpoint_f") val dewpointF: Double? = null,
    @SerialName("vis_km") val visKm: Double? = null,
    @SerialName("vis_miles") val visMiles: Double? = null,
    val uv: Double? = null,
    @SerialName("gust_mph") val gustMph: Double? = null,
    @SerialName("gust_kph") val gustKph: Double? = null,
    @SerialName("air_quality") val airQuality: AirQuality,
    @SerialName("short_rad") val shortRad: Double? = null,
    @SerialName("diff_rad") val diffRad: Double? = null,
    val dni: Double? = null,
    val gti: Double? = null
)

@Serializable
data class Condition(
    val text: String,
    val icon: String? = null,
    val code: Int? = null
)

@Serializable
data class AirQuality(
    val co: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    @SerialName("pm2_5") val pm25: Double,
    val pm10: Double,
    @SerialName("us-epa-index") val usEpaIndex: Int,
    @SerialName("gb-defra-index") val gbDefraIndex: Int
)

@Serializable
data class Forecast(
    val forecastday: List<TodayForecast>
)

@Serializable
data class TodayForecast(
    val day: DayForecast,
    val astro: Astro? = null,
)

@Serializable
data class DayForecast(
    @SerialName("maxtemp_c") val maxtempC: Double,
    @SerialName("maxtemp_f") val maxtempF: Double,
    @SerialName("mintemp_c") val mintempC: Double,
    @SerialName("mintemp_f") val mintempF: Double,
    @SerialName("avgtemp_c") val avgtempC: Double? = null,
    @SerialName("avgtemp_f") val avgtempF: Double? = null,
    @SerialName("maxwind_mph") val maxwindMph: Double? = null,
    @SerialName("maxwind_kph") val maxwindKph: Double? = null,
    @SerialName("totalprecip_mm") val totalprecipMm: Double? = null,
    @SerialName("totalprecip_in") val totalprecipIn: Double? = null,
    @SerialName("totalsnow_cm") val totalsnowCm: Double? = null,
    @SerialName("avgvis_km") val avgvisKm: Double? = null,
    @SerialName("avgvis_miles") val avgvisMiles: Double? = null,
    @SerialName("avghumidity") val avghumidity: Double? = null,
    @SerialName("daily_will_it_rain") val dailyWillItRain: Int,
    @SerialName("daily_will_it_snow") val dailyWillItSnow: Int,
    @SerialName("daily_chance_of_rain") val dailyChanceOfRain: Int,
    @SerialName("daily_chance_of_snow") val dailyChanceOfSnow: Int,
    val condition: Condition,
    val uv: Double? = null,
    @SerialName("air_quality") val airQuality: AirQuality? = null
)

@Serializable
data class Astro(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    @SerialName("moon_phase") val moonPhase: String,
    @SerialName("moon_illumination") val moonIllumination: Int,
    @SerialName("is_moon_up") val isMoonUp: Int,
    @SerialName("is_sun_up") val isSunUp: Int
)

@Serializable
data class WeatherErrorResponse(
    val error: WeatherApiError
)

@Serializable
data class WeatherApiError(
    val code: Int,
    val message: String
)