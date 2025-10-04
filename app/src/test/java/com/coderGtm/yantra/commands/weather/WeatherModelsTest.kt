package com.coderGtm.yantra.commands.weather

import org.junit.Test
import org.junit.Assert.*

class WeatherModelsTest {

    @Test
    fun `ValidationResult isValid returns true for valid result`() {
        val result = ValidationResult(
            validFields = listOf("temp", "humidity"),
            invalidFields = emptyList(),
            formatErrors = emptyList()
        )

        assertTrue("Should be valid when no errors", result.isValid())
    }

    @Test
    fun `ValidationResult isValid returns false when format errors exist`() {
        val result = ValidationResult(
            validFields = listOf("temp"),
            invalidFields = emptyList(),
            formatErrors = listOf("Invalid format")
        )

        assertFalse("Should be invalid when format errors exist", result.isValid())
    }

    @Test
    fun `ValidationResult isValid returns false when invalid fields exist`() {
        val result = ValidationResult(
            validFields = listOf("temp"),
            invalidFields = listOf("invalid"),
            formatErrors = emptyList()
        )

        assertFalse("Should be invalid when invalid fields exist", result.isValid())
    }

    @Test
    fun `ValidationResult isValid returns false when both errors exist`() {
        val result = ValidationResult(
            validFields = listOf("temp"),
            invalidFields = listOf("invalid"),
            formatErrors = listOf("Format error")
        )

        assertFalse("Should be invalid when both types of errors exist", result.isValid())
    }
}