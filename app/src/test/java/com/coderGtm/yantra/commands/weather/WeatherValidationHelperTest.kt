package com.coderGtm.yantra.commands.weather

import android.content.Context
import com.coderGtm.yantra.R
import org.junit.Test
import org.junit.Assert.*
import org.mockito.kotlin.*

class WeatherValidationHelperTest {

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

    // Tests for validateWeatherFields function
    @Test
    fun `validateWeatherFields correctly categorizes valid fields`() {
        val mockContext = createMockContext()
        val result = validateWeatherFields(listOf("-temp", "-humidity", "-wind"), mockContext)

        assertEquals(listOf("temp", "humidity", "wind"), result.validFields)
        assertTrue("Should have no invalid fields", result.invalidFields.isEmpty())
        assertTrue("Should have no format errors", result.formatErrors.isEmpty())
        assertTrue("Should be valid", result.isValid())
    }

    @Test
    fun `validateWeatherFields detects invalid field names`() {
        val mockContext = createMockContext()
        val result = validateWeatherFields(listOf("-temp", "-invalidfield", "-humidity"), mockContext)

        assertEquals(listOf("temp", "humidity"), result.validFields)
        assertEquals(listOf("invalidfield"), result.invalidFields)
        assertTrue("Should have no format errors", result.formatErrors.isEmpty())
        assertFalse("Should not be valid", result.isValid())
    }

    @Test
    fun `validateWeatherFields detects single hyphen format error`() {
        val mockContext = createMockContext()
        val result = validateWeatherFields(listOf("-temp", "-", "-humidity"), mockContext)

        assertEquals(listOf("temp", "humidity"), result.validFields)
        assertTrue("Should have no invalid fields", result.invalidFields.isEmpty())
        assertEquals(1, result.formatErrors.size)
        assertTrue("Should contain single hyphen error",
            result.formatErrors.any { it.contains("Single hyphen") })
        assertFalse("Should not be valid", result.isValid())
    }

    @Test
    fun `validateWeatherFields detects multiple hyphens format error`() {
        val mockContext = createMockContext()
        val result = validateWeatherFields(listOf("-temp", "--humidity", "-wind"), mockContext)

        assertEquals(listOf("temp", "wind"), result.validFields)
        assertTrue("Should have no invalid fields", result.invalidFields.isEmpty())
        assertEquals(1, result.formatErrors.size)
        assertTrue("Should contain multiple hyphens error",
            result.formatErrors.any { it.contains("Multiple hyphens") })
        assertFalse("Should not be valid", result.isValid())
    }

    @Test
    fun `validateWeatherFields detects misplaced non-hyphenated arguments`() {
        val mockContext = createMockContext()
        val result = validateWeatherFields(listOf("-temp", "misplaced", "-humidity"), mockContext)

        assertEquals(listOf("temp", "humidity"), result.validFields)
        assertTrue("Should have no invalid fields", result.invalidFields.isEmpty())
        assertEquals(1, result.formatErrors.size)
        assertTrue("Should contain misplaced argument error",
            result.formatErrors.any { it.contains("Misplaced argument") })
        assertFalse("Should not be valid", result.isValid())
    }

    @Test
    fun `validateWeatherFields handles mixed validation errors`() {
        val mockContext = createMockContext()
        val result = validateWeatherFields(
            listOf("-temp", "--badformat", "-", "misplaced", "-invalidfield"),
            mockContext
        )

        assertEquals(listOf("temp"), result.validFields)
        assertEquals(listOf("invalidfield"), result.invalidFields)
        assertEquals(3, result.formatErrors.size) // --badformat, -, misplaced
        assertFalse("Should not be valid", result.isValid())
    }

    @Test
    fun `validateWeatherFields handles empty input`() {
        val mockContext = createMockContext()
        val result = validateWeatherFields(emptyList(), mockContext)

        assertTrue("Should have no valid fields", result.validFields.isEmpty())
        assertTrue("Should have no invalid fields", result.invalidFields.isEmpty())
        assertTrue("Should have no format errors", result.formatErrors.isEmpty())
        assertTrue("Should be valid", result.isValid())
    }

    // Tests for validateRawArgsForFieldLikeWords function
    @Test
    fun `validateRawArgsForFieldLikeWords detects field-like words in location`() {
        val mockContext = createMockContext()
        val result = validateRawArgsForFieldLikeWords(
            listOf("london", "temp", "humidity", "-pressure"),
            mockContext
        )

        assertEquals(2, result.size) // Should detect "temp" and "humidity"
        assertTrue("Should contain field in location errors",
            result.all { it.contains("Field in location") })
    }

    @Test
    fun `validateRawArgsForFieldLikeWords suggests hyphens when no fields present`() {
        val mockContext = createMockContext()
        val result = validateRawArgsForFieldLikeWords(
            listOf("london", "temp", "humidity"),
            mockContext
        )

        assertEquals(2, result.size) // Should detect "temp" and "humidity"
        assertTrue("Should suggest adding hyphens",
            result.all { it.contains("without hyphens") })
    }

    @Test
    fun `validateRawArgsForFieldLikeWords ignores first word as location`() {
        val mockContext = createMockContext()
        val result = validateRawArgsForFieldLikeWords(
            listOf("temp", "humidity", "-pressure"), // "temp" is first word (location)
            mockContext
        )

        assertEquals(1, result.size) // Should only detect "humidity", not "temp"
    }

    @Test
    fun `validateRawArgsForFieldLikeWords ignores non-field words`() {
        val mockContext = createMockContext()
        val result = validateRawArgsForFieldLikeWords(
            listOf("new", "york", "city", "-temp"),
            mockContext
        )

        assertEquals(0, result.size) // "york" and "city" are not weather fields
    }

    @Test
    fun `validateRawArgsForFieldLikeWords handles case insensitive detection`() {
        val mockContext = createMockContext()
        val result = validateRawArgsForFieldLikeWords(
            listOf("london", "TEMP", "Humidity", "-pressure"),
            mockContext
        )

        assertEquals(2, result.size) // Should detect "TEMP" and "Humidity"
    }

    @Test
    fun `validateRawArgsForFieldLikeWords handles no hyphenated fields`() {
        val mockContext = createMockContext()
        val result = validateRawArgsForFieldLikeWords(
            listOf("london", "temp"),
            mockContext
        )

        assertEquals(1, result.size)
        assertTrue("Should suggest adding hyphens when no fields present",
            result[0].contains("without hyphens"))
    }

    @Test
    fun `validateRawArgsForFieldLikeWords handles empty input`() {
        val mockContext = createMockContext()
        val result = validateRawArgsForFieldLikeWords(emptyList(), mockContext)

        assertEquals(0, result.size)
    }

    // Tests for validateLocationForFieldLikeWords function
    @Test
    fun `validateLocationForFieldLikeWords detects field names in location string`() {
        val mockContext = createMockContext()
        val result = validateLocationForFieldLikeWords("london temp humidity", mockContext)

        assertEquals(2, result.size) // Should detect "temp" and "humidity"
        assertTrue("Should contain field in location errors",
            result.all { it.contains("Field in location") })
    }

    @Test
    fun `validateLocationForFieldLikeWords handles case insensitive detection`() {
        val mockContext = createMockContext()
        val result = validateLocationForFieldLikeWords("london TEMP Humidity", mockContext)

        assertEquals(2, result.size) // Should detect "TEMP" and "Humidity"
    }

    @Test
    fun `validateLocationForFieldLikeWords ignores non-field words`() {
        val mockContext = createMockContext()
        val result = validateLocationForFieldLikeWords("new york city", mockContext)

        assertEquals(0, result.size) // None of these are weather fields
    }

    @Test
    fun `validateLocationForFieldLikeWords handles empty location`() {
        val mockContext = createMockContext()
        val result = validateLocationForFieldLikeWords("", mockContext)

        assertEquals(0, result.size)
    }

    @Test
    fun `validateLocationForFieldLikeWords handles single word location`() {
        val mockContext = createMockContext()
        val result = validateLocationForFieldLikeWords("temp", mockContext)

        assertEquals(1, result.size) // Should detect "temp" as field-like
    }

    @Test
    fun `validateLocationForFieldLikeWords handles whitespace normalization`() {
        val mockContext = createMockContext()
        val result = validateLocationForFieldLikeWords("  london   temp   humidity  ", mockContext)

        assertEquals(2, result.size) // Should handle extra whitespace properly
    }
}