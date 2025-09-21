package com.coderGtm.yantra.commands.weather

import android.content.Context
import com.coderGtm.yantra.R
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*

class WeatherCommandParsingTest {

    private fun createMockContext(): Context {
        val mockContext = mock<Context>()

        // Mock string resources with generic returns
        whenever(mockContext.getString(eq(R.string.weather_error_single_hyphen)))
            .thenReturn("Single hyphen error")
        whenever(mockContext.getString(eq(R.string.weather_error_field_in_location_no_hyphens), any(), any()))
            .thenReturn("Field in location without hyphens error")
        whenever(mockContext.getString(eq(R.string.weather_error_field_in_location), any(), any()))
            .thenReturn("Field in location error")
        whenever(mockContext.getString(eq(R.string.weather_error_multiple_hyphens), any(), any()))
            .thenReturn("Multiple hyphens error")
        whenever(mockContext.getString(eq(R.string.weather_error_misplaced_argument), any()))
            .thenReturn("Misplaced argument error")
        whenever(mockContext.getString(eq(R.string.weather_error_combined_fields), any(), any()))
            .thenReturn("Combined fields error")

        return mockContext
    }

    @Test
    fun `parseWeatherCommand returns Success for valid location and fields`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather london -temp -humidity", mockContext)

        assertTrue("Should return Success", result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals("london", success.args.location)
        assertEquals(listOf("temp", "humidity"), success.args.requestedFields)
        assertFalse("Should not show default fields", success.args.showDefaultFields)
    }

    @Test
    fun `parseWeatherCommand returns Success for location only`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather new york", mockContext)

        assertTrue("Should return Success", result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals("new york", success.args.location)
        assertTrue("Should be empty fields", success.args.requestedFields.isEmpty())
        assertTrue("Should show default fields when no fields specified", success.args.showDefaultFields)
    }

    @Test
    fun `parseWeatherCommand returns MissingLocation for too few arguments`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather", mockContext)

        assertTrue("Should return MissingLocation", result is ParseResult.MissingLocation)
    }

    @Test
    fun `parseWeatherCommand returns ListCommand for list argument`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather list", mockContext)

        assertTrue("Should return ListCommand", result is ParseResult.ListCommand)
    }

    @Test
    fun `parseWeatherCommand returns MissingLocation when only fields provided`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather -temp -humidity", mockContext)

        assertTrue("Should return MissingLocation", result is ParseResult.MissingLocation)
    }

    @Test
    fun `parseWeatherCommand returns ValidationError for field-like words in location`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather london temp -humidity", mockContext)

        assertTrue("Should return ValidationError", result is ParseResult.ValidationError)
        val error = result as ParseResult.ValidationError
        assertEquals(1, error.formatErrors.size)
        assertTrue("Should contain field in location error",
            error.formatErrors.any { it.contains("Field in location") })
    }

    @Test
    fun `parseWeatherCommand returns ValidationError for invalid field format`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather london --temp", mockContext)

        assertTrue("Should return ValidationError", result is ParseResult.ValidationError)
        val error = result as ParseResult.ValidationError
        assertEquals(1, error.formatErrors.size)
        assertTrue("Should contain multiple hyphens error",
            error.formatErrors.any { it.contains("Multiple hyphens") })
    }

    @Test
    fun `parseWeatherCommand returns ValidationError for single hyphen`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather london -", mockContext)

        assertTrue("Should return ValidationError", result is ParseResult.ValidationError)
        val error = result as ParseResult.ValidationError
        assertEquals(1, error.formatErrors.size)
        assertTrue("Should contain single hyphen error",
            error.formatErrors.any { it.contains("Single hyphen") })
    }

    @Test
    fun `parseWeatherCommand returns ValidationError for invalid field names`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather london -invalidfield", mockContext)

        assertTrue("Should return ValidationError", result is ParseResult.ValidationError)
        val error = result as ParseResult.ValidationError
        assertEquals(1, error.invalidFields.size)
        assertEquals("invalidfield", error.invalidFields[0])
    }

    @Test
    fun `parseWeatherCommand handles mixed validation errors`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather london -temp --humidity -invalidfield", mockContext)

        assertTrue("Should return ValidationError", result is ParseResult.ValidationError)
        val error = result as ParseResult.ValidationError
        assertEquals(1, error.formatErrors.size) // --humidity format error
        assertEquals(1, error.invalidFields.size) // invalidfield
        assertTrue("Should contain multiple hyphens error",
            error.formatErrors.any { it.contains("Multiple hyphens") })
        assertEquals("invalidfield", error.invalidFields[0])
    }

    @Test
    fun `parseWeatherCommand handles complex multi-word locations`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather los angeles california -temp -wind", mockContext)

        assertTrue("Should return Success", result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals("los angeles california", success.args.location)
        assertEquals(listOf("temp", "wind"), success.args.requestedFields)
    }

    @Test
    fun `parseWeatherCommand handles hyphenated location names`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather winston-salem -temp", mockContext)

        assertTrue("Should return Success", result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals("winston-salem", success.args.location)
        assertEquals(listOf("temp"), success.args.requestedFields)
    }

    @Test
    fun `parseWeatherCommand early validation catches field-like words without hyphens`() {
        val mockContext = createMockContext()
        val result = parseWeatherCommand("weather london temp humidity", mockContext)

        assertTrue("Should return ValidationError", result is ParseResult.ValidationError)
        val error = result as ParseResult.ValidationError
        assertEquals(2, error.formatErrors.size) // Should catch both "temp" and "humidity"
        assertTrue("Should suggest adding hyphens",
            error.formatErrors.all { it.contains("without hyphens") })
    }
}