package com.coderGtm.yantra.commands.bg

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlurValidationTest {
    @Test
    fun `blur values must be between 1 and 10`() {
        assertFalse(isValidBlur(0))
        assertTrue(isValidBlur(1))
        assertTrue(isValidBlur(10))
        assertFalse(isValidBlur(11))
    }
}
