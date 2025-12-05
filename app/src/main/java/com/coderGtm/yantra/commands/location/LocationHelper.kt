package com.coderGtm.yantra.commands.location

import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import com.coderGtm.yantra.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Represents the age of a location with its numeric value and corresponding string resource.
 *
 * @property value The numeric age value (e.g., 30 for "30 seconds ago").
 * @property stringResId The string resource ID to format this age (e.g., R.string.cmd_location_age_seconds).
 */
data class LocationAge(
    val value: Long,
    val stringResId: Int
)

/**
 * Calculates the age of a location and returns it with the appropriate string resource.
 *
 * This is a pure function with no Android dependencies, making it easily testable.
 *
 * @param time The timestamp when the location was obtained (milliseconds since epoch).
 * @param currentTime The current time in milliseconds since epoch (defaults to system time).
 * @return [LocationAge] containing the numeric value and string resource ID to use for formatting.
 */
fun calculateLocationAge(time: Long, currentTime: Long = System.currentTimeMillis()): LocationAge {
    val ageSeconds = (currentTime - time) / 1000
    return when {
        ageSeconds < 60 -> LocationAge(ageSeconds, R.string.cmd_location_age_seconds)
        ageSeconds < 3600 -> LocationAge(ageSeconds / 60, R.string.cmd_location_age_minutes)
        ageSeconds < 86400 -> LocationAge(ageSeconds / 3600, R.string.cmd_location_age_hours)
        else -> LocationAge(ageSeconds / 86400, R.string.cmd_location_age_days)
    }
}


/**
 * Suspend extension for [Geocoder.getFromLocation] that handles API level differences.
 *
 * On Android 13+ (API 33+), uses the callback-based API with [Geocoder.GeocodeListener] while
 * on older versions, uses the deprecated blocking API.
 *
 * **Note on timeout behavior:**
 * - API 33+: Timeout properly cancels the geocoding request via coroutine cancellation.
 * - Pre-API 33: The blocking [Geocoder.getFromLocation] call cannot be cancelled. The timeout
 *   will return `null` after the timeout for better UX, but the underlying blocking call may
 *   continue executing in the background. This is an inherent limitation of the deprecated API.
 *
 * **Additional note:** Instead of returning `null`, we could propagate the `Exception`s, maybe
 * even create a custom `GeocoderException` class to handle these cases it seemed like it would
 * be an overly complex implementation here.
 *
 * @param latitude The latitude of the location.
 * @param longitude The longitude of the location.
 * @param maxResults Maximum number of results to return.
 * @param timeout Timeout in milliseconds.
 * @return List of [Address]es or null if geocoding fails or times out.
 */
suspend fun Geocoder.getAddressSuspend(
    latitude: Double,
    longitude: Double,
    maxResults: Int = 1,
    timeout: Long = 4_000L,
): List<Address>? {
    return withTimeoutOrNull(timeout) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                val geocodeListener = object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (continuation.isActive) {
                            continuation.resume(addresses)
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }
                getFromLocation(latitude, longitude, maxResults, geocodeListener)
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                getFromLocation(latitude, longitude, maxResults)
            } catch (_: Exception) {
                null
            }
        }
    }
}
