package com.coderGtm.yantra.commands.location

import com.coderGtm.yantra.R
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure unit tests for location helper functions.
 * No Android dependencies or mocking required.
 */
class LocationCommandTest {

    companion object {
        private const val ONE_SECOND = 1_000L
        private const val ONE_MINUTE = 60 * ONE_SECOND
        private const val ONE_HOUR = 60 * ONE_MINUTE
        private const val ONE_DAY = 24 * ONE_HOUR

        private const val BASE_TIME = 1_000_000_000L
    }

    // ========== LocationSource Enum Tests ==========

    @Test
    fun `LocationSource GPS_CACHED has correct string resource`() {
        assertEquals(R.string.cmd_location_source_gps_cached, LocationSource.GPS_CACHED.stringResId)
    }

    @Test
    fun `LocationSource GPS_FRESH has correct string resource`() {
        assertEquals(R.string.cmd_location_source_gps_fresh, LocationSource.GPS_FRESH.stringResId)
    }

    @Test
    fun `LocationSource NETWORK_CACHED has correct string resource`() {
        assertEquals(R.string.cmd_location_source_network_cached, LocationSource.NETWORK_CACHED.stringResId)
    }

    @Test
    fun `LocationSource NETWORK_FRESH has correct string resource`() {
        assertEquals(R.string.cmd_location_source_network_fresh, LocationSource.NETWORK_FRESH.stringResId)
    }

    @Test
    fun `LocationSource UNKNOWN has correct string resource`() {
        assertEquals(R.string.cmd_location_source_unknown, LocationSource.UNKNOWN.stringResId)
    }

    // ========== calculateLocationAge Tests ==========

    @Test
    fun `calculateLocationAge returns seconds when age is less than 60 seconds`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (30 * ONE_SECOND)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(30L, result.value)
        assertEquals(R.string.cmd_location_age_seconds, result.stringResId)
    }

    @Test
    fun `calculateLocationAge returns minutes when age is between 60 seconds and 1 hour`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (5 * ONE_MINUTE)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(5L, result.value)
        assertEquals(R.string.cmd_location_age_minutes, result.stringResId)
    }

    @Test
    fun `calculateLocationAge returns hours when age is between 1 hour and 24 hours`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (2 * ONE_HOUR)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(2L, result.value)
        assertEquals(R.string.cmd_location_age_hours, result.stringResId)
    }

    @Test
    fun `calculateLocationAge returns days when age is more than 24 hours`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (3 * ONE_DAY)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(3L, result.value)
        assertEquals(R.string.cmd_location_age_days, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles boundary at 59 seconds`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (59 * ONE_SECOND)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(59L, result.value)
        assertEquals(R.string.cmd_location_age_seconds, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles boundary at 60 seconds (1 minute)`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - ONE_MINUTE

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(1L, result.value)
        assertEquals(R.string.cmd_location_age_minutes, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles boundary at 3599 seconds (59 minutes 59 seconds)`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (ONE_HOUR - ONE_SECOND)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(59L, result.value)
        assertEquals(R.string.cmd_location_age_minutes, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles boundary at 3600 seconds (1 hour)`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - ONE_HOUR

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(1L, result.value)
        assertEquals(R.string.cmd_location_age_hours, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles boundary at 86399 seconds (23 hours 59 minutes 59 seconds)`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (ONE_DAY - ONE_SECOND)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(23L, result.value)
        assertEquals(R.string.cmd_location_age_hours, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles boundary at 86400 seconds (1 day)`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - ONE_DAY

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(1L, result.value)
        assertEquals(R.string.cmd_location_age_days, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles 0 seconds (just now)`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(0L, result.value)
        assertEquals(R.string.cmd_location_age_seconds, result.stringResId)
    }

    @Test
    fun `calculateLocationAge handles multiple days`() {
        val currentTime = BASE_TIME
        val locationTime = currentTime - (7 * ONE_DAY)

        val result = calculateLocationAge(locationTime, currentTime)

        assertEquals(7L, result.value)
        assertEquals(R.string.cmd_location_age_days, result.stringResId)
    }
}
