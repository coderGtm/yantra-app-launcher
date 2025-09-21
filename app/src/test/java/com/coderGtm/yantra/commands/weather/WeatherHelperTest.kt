package com.coderGtm.yantra.commands.weather

import com.coderGtm.yantra.R
import org.junit.Test
import org.junit.Assert.*

class WeatherHelperTest {

    @Test
    fun `getWeatherApiErrorStringRes with known error code 1006 returns location not found`() {
        val apiError = WeatherApiError(1006, "Location not found")
        val statusCode = 400
        val result = getWeatherApiErrorStringRes(apiError, statusCode)
        assertEquals(R.string.weather_location_not_found, result)
    }

    @Test
    fun `getWeatherApiErrorStringRes with known error code 2007 returns quota exceeded`() {
        val apiError = WeatherApiError(2007, "Quota exceeded")
        val statusCode = 403
        val result = getWeatherApiErrorStringRes(apiError, statusCode)
        assertEquals(R.string.weather_quota_exceeded, result)
    }

    @Test
    fun `getWeatherApiErrorStringRes with all known error codes returns correct string resources`() {
        val testCases = mapOf(
            1002 to R.string.weather_api_key_not_provided,
            1003 to R.string.weather_location_parameter_missing,
            1005 to R.string.weather_api_request_invalid,
            1006 to R.string.weather_location_not_found,
            2006 to R.string.weather_api_key_invalid,
            2007 to R.string.weather_quota_exceeded,
            2008 to R.string.weather_api_disabled,
            2009 to R.string.weather_api_access_restricted,
            9000 to R.string.weather_bulk_request_invalid,
            9001 to R.string.weather_bulk_too_many_locations,
            9999 to R.string.weather_internal_error
        )

        testCases.forEach { (errorCode, expectedStringRes) ->
            val apiError = WeatherApiError(errorCode, "Test error")
            val statusCode = 400
            val result = getWeatherApiErrorStringRes(apiError, statusCode)
            assertEquals(
                "Error code $errorCode should map to correct string resource",
                expectedStringRes,
                result
            )
        }
    }

    @Test
    fun `getWeatherApiErrorStringRes with unknown error code falls back to status code mapping`() {
        val testCases = mapOf(
            400 to R.string.weather_location_not_found,
            401 to R.string.weather_api_key_invalid,
            403 to R.string.weather_quota_exceeded,
            500 to R.string.weather_unknown_error // Unknown status code
        )

        testCases.forEach { (statusCode, expectedStringRes) ->
            val apiError = WeatherApiError(9998, "Unknown error")
            val result = getWeatherApiErrorStringRes(apiError, statusCode)
            assertEquals(
                "Status code $statusCode should map to correct string resource",
                expectedStringRes,
                result
            )
        }
    }

    @Test
    fun `getWeatherApiErrorStringRes with null apiError falls back to status code mapping`() {
        val statusCode = 401
        val result = getWeatherApiErrorStringRes(null, statusCode)
        assertEquals(R.string.weather_api_key_invalid, result)
    }

    @Test
    fun `getWeatherApiErrorStringRes with null apiError and unknown status code returns unknown error`() {
        val statusCode = 502
        val result = getWeatherApiErrorStringRes(null, statusCode)
        assertEquals(R.string.weather_unknown_error, result)
    }

    @Test
    fun `getWeatherApiErrorStringRes with zero error code falls back to status code mapping`() {
        val zeroCodeError = WeatherApiError(0, "Zero code")
        val statusCode = 400
        val result = getWeatherApiErrorStringRes(zeroCodeError, statusCode)
        assertEquals(R.string.weather_location_not_found, result)
    }

    @Test
    fun `getWeatherApiErrorStringRes with negative error code falls back to status code mapping`() {
        val negativeCodeError = WeatherApiError(-1, "Negative code")
        val statusCode = 401
        val result = getWeatherApiErrorStringRes(negativeCodeError, statusCode)
        assertEquals(R.string.weather_api_key_invalid, result)
    }

    @Test
    fun `getWeatherApiErrorStringRes with large error code falls back to status code mapping`() {
        val largeCodeError = WeatherApiError(99999, "Large code")
        val statusCode = 403
        val result = getWeatherApiErrorStringRes(largeCodeError, statusCode)
        assertEquals(R.string.weather_quota_exceeded, result)
    }

    //-------------------------- WRITE OTHER TESTS BELOW THIS LINE -----------------------//


}