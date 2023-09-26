package com.ntt.skyway.plugin.sfuBot

import org.junit.Assert.*
import org.junit.Test


class SFUBotPluginTest {
    @Test
    fun name() {
        val result = SFUBotPlugin().name
        assertEquals(result, "sfu")
    }
}
