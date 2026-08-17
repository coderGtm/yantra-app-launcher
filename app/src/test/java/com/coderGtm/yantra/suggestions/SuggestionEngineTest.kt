package com.coderGtm.yantra.suggestions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenizerTest {

    private fun tokenize(raw: String) =
        tokenize(CompletionInput(rawText = raw, cursor = raw.length))

    @Test
    fun `empty input has no tokens`() {
        val result = tokenize("")
        assertEquals(0, result.tokens.size)
        assertNull(result.activeToken)
        assertEquals(false, result.hasTrailingWhitespace)
    }

    @Test
    fun `single word`() {
        val result = tokenize("run")
        assertEquals(listOf("run"), result.tokens.map { it.text })
        assertEquals(0, result.tokens[0].start)
        assertEquals(3, result.tokens[0].end)
        assertEquals("run", result.activeToken?.text)
        assertEquals(false, result.hasTrailingWhitespace)
    }

    @Test
    fun `word followed by space has trailing whitespace`() {
        val result = tokenize("run ")
        assertEquals(listOf("run"), result.tokens.map { it.text })
        assertNull(result.activeToken)
        assertEquals(true, result.hasTrailingWhitespace)
    }

    @Test
    fun `two words active token is last`() {
        val result = tokenize("run -lua")
        assertEquals(listOf("run", "-lua"), result.tokens.map { it.text })
        assertEquals("-lua", result.activeToken?.text)
        assertEquals(4, result.activeToken?.start)
        assertEquals(8, result.activeToken?.end)
        assertEquals(false, result.hasTrailingWhitespace)
    }

    @Test
    fun `two words trailing space`() {
        val result = tokenize("run -lua ")
        assertEquals(listOf("run", "-lua"), result.tokens.map { it.text })
        assertNull(result.activeToken)
        assertEquals(true, result.hasTrailingWhitespace)
    }

    @Test
    fun `three words`() {
        val result = tokenize("run -lua scr")
        assertEquals(listOf("run", "-lua", "scr"), result.tokens.map { it.text })
        assertEquals("scr", result.activeToken?.text)
        assertEquals(9, result.activeToken?.start)
        assertEquals(12, result.activeToken?.end)
    }

    @Test
    fun `multi word value with spaces`() {
        val result = tokenize("launch Google M")
        assertEquals(listOf("launch", "Google", "M"), result.tokens.map { it.text })
        assertEquals("M", result.activeToken?.text)
    }

    @Test
    fun `repeated spaces preserved as boundaries`() {
        val result = tokenize("launch  Google")
        assertEquals(listOf("launch", "Google"), result.tokens.map { it.text })
        assertEquals(8, result.tokens[1].start)
        assertEquals("Google", result.activeToken?.text)
    }
}