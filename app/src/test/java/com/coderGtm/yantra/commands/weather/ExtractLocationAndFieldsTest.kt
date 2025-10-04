package com.coderGtm.yantra.commands.weather

import org.junit.Test
import org.junit.Assert.*

class ExtractLocationAndFieldsTest {

    @Test
    fun `location only extracted correctly`() {
        val args = listOf("london")
        val result = extractLocationAndFields(args)

        assertEquals("london", result.location)
        assertTrue(result.fields.isEmpty())
    }

    @Test
    fun `location with fields extracted correctly`() {
        val args = listOf("london", "-temp", "-humidity")
        val result = extractLocationAndFields(args)

        assertEquals("london", result.location)
        assertEquals(listOf("temp", "humidity"), result.fields)
    }

    @Test
    fun `multi word location extracted correctly`() {
        val args = listOf("new", "york", "-temp")
        val result = extractLocationAndFields(args)

        assertEquals("new york", result.location)
        assertEquals(listOf("temp"), result.fields)
    }

    @Test
    fun `multi word location without fields`() {
        val args = listOf("san", "francisco")
        val result = extractLocationAndFields(args)

        assertEquals("san francisco", result.location)
        assertTrue(result.fields.isEmpty())
    }

    @Test
    fun `fields without hyphens ignored`() {
        val args = listOf("tokyo", "temp", "-humidity", "wind", "-pressure")
        val result = extractLocationAndFields(args)

        // non-hyphenated arguments before first "-" are considered part of location
        assertEquals("tokyo temp", result.location)
        assertEquals(listOf("humidity", "pressure"), result.fields)
    }

    @Test
    fun `empty args returns empty location`() {
        val args = emptyList<String>()
        val result = extractLocationAndFields(args)

        assertEquals("", result.location)
        assertTrue(result.fields.isEmpty())
    }

    @Test
    fun `only fields with no location`() {
        val args = listOf("-temp", "-humidity")
        val result = extractLocationAndFields(args)

        assertEquals("", result.location)
        assertEquals(listOf("temp", "humidity"), result.fields)
    }

    @Test
    fun `fields with empty names filtered out`() {
        val args = listOf("london", "-temp", "-", "-humidity")
        val result = extractLocationAndFields(args)

        assertEquals("london", result.location)
        assertEquals(listOf("temp", "humidity"), result.fields)
    }

    @Test
    fun `complex location with multiple fields`() {
        val args = listOf("los", "angeles", "california", "-temp", "-feels", "-wind", "-humidity")
        val result = extractLocationAndFields(args)

        assertEquals("los angeles california", result.location)
        assertEquals(listOf("temp", "feels", "wind", "humidity"), result.fields)
    }

    @Test
    fun `location with mixed field formats`() {
        val args = listOf("chicago", "-temp", "sometext", "-humidity", "-wind")
        val result = extractLocationAndFields(args)

        assertEquals("chicago", result.location)
        assertEquals(listOf("temp", "humidity", "wind"), result.fields)
    }

    @Test
    fun `hyphenated location parsed correctly`() {
        val args = listOf("winston-salem", "-temp", "-humidity")
        val result = extractLocationAndFields(args)
        assertEquals("winston-salem", result.location)
        assertEquals(listOf("temp", "humidity"), result.fields)
    }

    @Test
    fun `unicode location names handled correctly`() {
        val args = listOf("москва", "-temp") // Moscow in Russian
        val result = extractLocationAndFields(args)
        assertEquals("москва", result.location)
        assertEquals(listOf("temp"), result.fields)
    }

    @Test
    fun `location with special characters`() {
        val args = listOf("coeur", "d'alene", "-humidity")
        val result = extractLocationAndFields(args)
        assertEquals("coeur d'alene", result.location)
        assertEquals(listOf("humidity"), result.fields)
    }

    @Test
    fun `very long location name`() {
        val longLocation = listOf("lake", "chargoggagoggmanchauggagoggchaubunagungamaugg", "-temp")
        val result = extractLocationAndFields(longLocation)
        assertEquals("lake chargoggagoggmanchauggagoggchaubunagungamaugg", result.location)
        assertEquals(listOf("temp"), result.fields)
    }

    @Test
    fun `maximum number of fields`() {
        val manyFields = listOf("london") + VALID_WEATHER_FIELDS.map { "-$it" }
        val result = extractLocationAndFields(manyFields)
        assertEquals("london", result.location)
        assertEquals(VALID_WEATHER_FIELDS.toList(), result.fields)
    }

    @Test
    fun `duplicate fields are preserved`() {
        val args = listOf("london", "-temp", "-humidity", "-temp", "-wind")
        val result = extractLocationAndFields(args)
        assertEquals("london", result.location)
        assertEquals(listOf("temp", "humidity", "temp", "wind"), result.fields)
    }

    @Test
    fun `fields with numbers in location name`() {
        val args = listOf("highway", "101", "exit", "23", "-temp")
        val result = extractLocationAndFields(args)
        assertEquals("highway 101 exit 23", result.location)
        assertEquals(listOf("temp"), result.fields)
    }

    @Test
    fun `location with punctuation`() {
        val args = listOf("st.", "john's", "-wind", "-temp")
        val result = extractLocationAndFields(args)
        assertEquals("st. john's", result.location)
        assertEquals(listOf("wind", "temp"), result.fields)
    }

    @Test
    fun `single character location parts`() {
        val args = listOf("a", "b", "c", "-humidity")
        val result = extractLocationAndFields(args)
        assertEquals("a b c", result.location)
        assertEquals(listOf("humidity"), result.fields)
    }

    @Test
    fun `malformed input with multiple consecutive hyphens`() {
        val args = listOf("london", "---temp", "-humidity")
        val result = extractLocationAndFields(args)
        assertEquals("london", result.location)
        // Function extracts what it can - removePrefix only removes first hyphen
        assertEquals(listOf("--temp", "humidity"), result.fields)
    }

    @Test
    fun `fields at start followed by location`() {
        val args = listOf("-temp", "-humidity", "london", "england")
        val result = extractLocationAndFields(args)
        assertEquals("", result.location) // No location before first field
        assertEquals(listOf("temp", "humidity"), result.fields)
    }

}