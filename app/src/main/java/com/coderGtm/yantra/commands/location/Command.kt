package com.coderGtm.yantra.commands.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.coderGtm.yantra.PermissionRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Holds the current location job to allow cancellation when a new location request is made.
 * This prevents multiple concurrent location requests from running simultaneously.
 */
private var locationJob: Job? = null

/**
 * Result of a location request through [LocationManager].
 *
 * @property location The [Location] data from Android's location services.
 * @property source The [LocationSource] indicating where and how the location was obtained.
 */
data class LocationResult(
    val location: Location,
    val source: LocationSource,
)

/**
 * Source of a [Location], indicating the provider and freshness of the data.
 *
 * @property stringResId The string resource ID for this location source.
 *
 * - [GPS_CACHED]: Previously obtained GPS location from cache
 * - [GPS_FRESH]: Newly requested GPS location
 * - [NETWORK_CACHED]: Previously obtained network-based location from cache
 * - [NETWORK_FRESH]: Newly requested network-based location
 * - [UNKNOWN]: Location source cannot be determined
 */
enum class LocationSource(val stringResId: Int) {
    GPS_CACHED(R.string.cmd_location_source_gps_cached),
    GPS_FRESH(R.string.cmd_location_source_gps_fresh),
    NETWORK_CACHED(R.string.cmd_location_source_network_cached),
    NETWORK_FRESH(R.string.cmd_location_source_network_fresh),
    UNKNOWN(R.string.cmd_location_source_unknown)
}

/**
 * Represents the location information to be displayed to the user.
 *
 * @property latitude The latitude of the location in decimal degrees.
 * @property longitude The longitude of the location in decimal degrees.
 * @property accuracy The accuracy of the location in meters.
 * @property time The timestamp when the location was obtained (milliseconds since epoch).
 * @property source The [LocationSource] indicating how the location was obtained.
 * @property streetAddress The human-readable street address (null if geocoding unavailable or failed).
 */
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val time: Long,
    val source: LocationSource,
    val streetAddress: String?
)

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "location",
        helpTitle = "location",
        description = terminal.activity.getString(R.string.cmd_location_help)
    )

    /**
     * Executes the location command.
     *
     * Usage:
     * - `location`: Retrieves location (uses cached if available, otherwise requests fresh)
     * - `location -refresh`: Forces a fresh GPS location update, bypassing cache
     *
     * Displays latitude, longitude, accuracy, address (if available), source, and age.
     */
    override fun execute(command: String) {
        if (!checkLocationPermission()) {
            return
        }

        val args = command.split(" ").drop(1)
        val forceRefresh = args.contains("-refresh")

        if (forceRefresh) {
            output(terminal.activity.getString(R.string.cmd_location_forcing_refresh))
        } else {
            output(terminal.activity.getString(R.string.cmd_location_fetching))
        }

        locationJob?.cancel()
        locationJob = CoroutineScope(Dispatchers.Main).launch {
            try {
                ensureActive()
                val result = withContext(Dispatchers.IO) {
                    val locationManager =
                        terminal.activity.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
                    getBestLocation(locationManager, forceRefresh)
                }

                if (result == null) {
                    output(
                        terminal.activity.getString(R.string.cmd_location_unavailable),
                        terminal.theme.errorTextColor
                    )
                    return@launch
                }

                val processed = processLocationResult(result)
                displayLocationInfo(processed)

            } catch (_: SecurityException) {
                output(
                    terminal.activity.getString(R.string.cmd_location_permission_denied),
                    terminal.theme.errorTextColor
                )
            } catch (e: Exception) {
                output(
                    terminal.activity.getString(
                        R.string.cmd_location_error,
                        e.message ?: "Unknown error"
                    ), terminal.theme.errorTextColor
                )
            }
        }
    }

    /**
     * Check if location permission is granted, and request it if not.
     *
     * *Note: ideally, we would setup a re-usable global function or use case to 1. check the
     * permission status 2. request it. This works for now though since its usage is localized to
     * this command.*
     *
     * @return `true` if permission is granted, `false` otherwise.
     */
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                terminal.activity.baseContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            output(
                terminal.activity.getString(
                    R.string.feature_permission_missing,
                    terminal.activity.getString(R.string.cmd_location_location)
                ),
                terminal.theme.warningTextColor
            )
            ActivityCompat.requestPermissions(
                terminal.activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PermissionRequestCodes.LOCATION.code
            )
            return false
        }
        return true
    }

    /**
     * Retrieves the most recent cached location from [LocationManager].
     *
     * Checks both GPS and Network providers and returns the most recent location
     * if available. Does not request a new location update.
     *
     * @param locationManager The [LocationManager] instance.
     * @return The most recent cached [Location] or null if no cached location is available.
     */
    @SuppressLint("MissingPermission")
    private fun getCachedLocation(locationManager: LocationManager): Location? {
        val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        // If both exist, pick the more recent one
        return when {
            gpsLocation == null && networkLocation == null -> null
            gpsLocation == null -> networkLocation
            networkLocation == null -> gpsLocation
            gpsLocation.time > networkLocation.time -> gpsLocation
            else -> networkLocation
        }
    }

    /**
     * Get the best location from [LocationManager].
     *
     * We first attempt to retrieve a cached location. If a cached location is not available, or
     * if [forceRefresh] is true, we request a fresh location update from the best available
     * provider (GPS or network).
     *
     * @param locationManager The [LocationManager] instance.
     * @param forceRefresh If true, skip cached location and request fresh update.
     * @return A [LocationResult] or null if not available.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(locationManager: LocationManager, forceRefresh: Boolean = false): LocationResult? {
        if (!forceRefresh) {
            getCachedLocation(locationManager)?.let { location ->
                val source = when (location.provider) {
                    LocationManager.GPS_PROVIDER -> LocationSource.GPS_CACHED
                    LocationManager.NETWORK_PROVIDER -> LocationSource.NETWORK_CACHED
                    else -> LocationSource.UNKNOWN
                }
                return LocationResult(location, source)
            }
        }
        return requestFreshLocation(locationManager)?.let { location ->
            val source = when (location.provider) {
                LocationManager.GPS_PROVIDER -> LocationSource.GPS_FRESH
                LocationManager.NETWORK_PROVIDER -> LocationSource.NETWORK_FRESH
                else -> LocationSource.UNKNOWN
            }
            LocationResult(location, source)
        }
    }

    /**
     * Asynchronously requests a single fresh location update from the best available provider.
     *
     * **Note: the location permission is expected to be already checked before calling this
     * function.**
     *
     * @param locationManager The system's `LocationManager` service.
     * @param timeout Timeout in milliseconds for the location request.
     * @param preferredProvider The preferred location provider (GPS or Network). Falls back to
     *                          the other provider if preferred is not enabled.
     * @return The first received [Location] object, or `null` if no provider is enabled or the
     * request times out.
     */
    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(
        locationManager: LocationManager,
        timeout: Long = 10_000L,
        preferredProvider: String = LocationManager.GPS_PROVIDER
    ): Location? {
        val provider = when {
            locationManager.isProviderEnabled(preferredProvider) -> preferredProvider
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return null
        }

        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}

                    override fun onProviderEnabled(provider: String) {}

                    override fun onProviderDisabled(provider: String) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }

                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )

                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(listener)
                }
            }
        }
    }

    /**
     * Processes a [LocationResult] and converts it to [LocationInfo] for display.
     *
     * Extracts location data and attempts to reverse geocode the coordinates to get
     * a human-readable street address if the Geocoder service is available.
     *
     * @param result The [LocationResult] from the location request.
     * @return [LocationInfo] containing all display-ready location data.
     */
    private suspend fun processLocationResult(result: LocationResult): LocationInfo {
        val location = result.location
        val source = result.source

        val latitude = location.latitude
        val longitude = location.longitude
        val accuracy = location.accuracy
        val time = location.time

        val streetAddress = if (Geocoder.isPresent()) {
            withContext(Dispatchers.IO) {
                val geocoder = Geocoder(terminal.activity, Locale.getDefault())
                val addresses = geocoder.getAddressSuspend(latitude, longitude)
                addresses?.firstOrNull()?.getAddressLine(0)
            }
        } else {
            null
        }

        return LocationInfo(latitude, longitude, accuracy, time, source, streetAddress)
    }

    /**
     * Displays the location information to the terminal output.
     *
     * Shows address (if available), latitude, longitude, accuracy, source, and age
     * in a formatted display with separators.
     *
     * @param info The [LocationInfo] to display.
     */
    @SuppressLint("DefaultLocale")
    private fun displayLocationInfo(info: LocationInfo) {
        output("-------------------------")
        output(
            terminal.activity.getString(R.string.cmd_location_info),
            terminal.theme.successTextColor
        )

        info.streetAddress?.let {
            output(terminal.activity.getString(R.string.cmd_location_address, it))
        }

        output(
            terminal.activity.getString(
                R.string.cmd_location_latitude,
                String.format("%.6f", info.latitude)
            )
        )
        output(
            terminal.activity.getString(
                R.string.cmd_location_longitude,
                String.format("%.6f", info.longitude)
            )
        )
        output(
            terminal.activity.getString(
                R.string.cmd_location_accuracy,
                String.format("%.1f", info.accuracy)
            )
        )

        output(terminal.activity.getString(R.string.cmd_location_source, terminal.activity.getString(info.source.stringResId)))

        val age = calculateLocationAge(info.time)
        output(terminal.activity.getString(R.string.cmd_location_age, terminal.activity.getString(age.stringResId, age.value)))

        output("-------------------------")
    }

}
