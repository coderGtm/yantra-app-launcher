package com.coderGtm.yantra.commands.weather

import org.junit.Test
import org.junit.Assert.*

class WeatherFieldValidationTest {

    @Test
    fun `all field keys are unique across categories`() {
        val allFieldKeys = WEATHER_FIELD_CATEGORIES.flatMap { it.fields.map { field -> field.key } }
        val uniqueKeys = allFieldKeys.toSet()

        assertEquals(
            "All field keys should be unique",
            allFieldKeys.size,
            uniqueKeys.size
        )
    }

    @Test
    fun `VALID_WEATHER_FIELDS contains all expected fields`() {
        val allFieldKeys = WEATHER_FIELD_CATEGORIES.flatMap { it.fields.map { field -> field.key } }

        assertEquals(
            "VALID_WEATHER_FIELDS should contain all field keys",
            allFieldKeys.toSet(),
            VALID_WEATHER_FIELDS
        )
    }

    @Test
    fun `DEFAULT_WEATHER_FIELDS matches isDefault flag`() {
        val expectedDefaults = WEATHER_FIELD_CATEGORIES
            .flatMap { it.fields }
            .filter { it.isDefault }
            .map { it.key }
            .toSet()

        assertEquals(
            "DEFAULT_WEATHER_FIELDS should match fields with isDefault=true",
            expectedDefaults,
            DEFAULT_WEATHER_FIELDS.toSet()
        )
    }

    @Test
    fun `WEATHER_FIELD_MAP maps all fields correctly`() {
        val allFields = WEATHER_FIELD_CATEGORIES.flatMap { it.fields }

        // Check all fields are in the map
        allFields.forEach { field ->
            assertTrue(
                "Field '${field.key}' should be in WEATHER_FIELD_MAP",
                WEATHER_FIELD_MAP.containsKey(field.key)
            )
            assertEquals(
                "Field object should match in WEATHER_FIELD_MAP",
                field,
                WEATHER_FIELD_MAP[field.key]
            )
        }

        // Check map doesn't contain extra fields
        assertEquals(
            "WEATHER_FIELD_MAP should contain exactly the same fields",
            allFields.size,
            WEATHER_FIELD_MAP.size
        )
    }

    @Test
    fun `renderer functions are not null`() {
        val allFields = WEATHER_FIELD_CATEGORIES.flatMap { it.fields }

        allFields.forEach { field ->
            assertNotNull(
                "Field '${field.key}' should have a non-null renderer",
                field.renderer
            )
        }
    }

}