package com.coderGtm.yantra.commands.wiki

import org.junit.Assert.assertEquals
import org.junit.Test

class WikiCommandTest {
    @Test
    fun `wiki command uses the launcher wiki URL`() {
        assertEquals(
            "https://codergtm.github.io/yantra-app-launcher/docs/",
            WIKI_URL,
        )
    }
}
