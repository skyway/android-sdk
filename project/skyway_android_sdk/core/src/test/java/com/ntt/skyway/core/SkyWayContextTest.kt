package com.ntt.skyway.core

import com.ntt.skyway.plugin.Plugin
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

class SkyWayContextTest {

    @Test
    fun isSetup() {
        val result = SkyWayContext.isSetup
        assertFalse(result)
    }

    @Test
    fun registerPlugin() {
        val plugin: Plugin = mock(Plugin::class.java)
        SkyWayContext.registerPlugin(plugin)
        assertEquals(SkyWayContext.plugins.size, 2)
    }

    @Test
    fun findNoExistPlugin() {
        val result = SkyWayContext.findPlugin("invalid")
        assertNull(result)
    }

    @Test
    fun findExistPlugin() {
        val plugin: Plugin = mock(Plugin::class.java)
        doReturn("test").`when`(plugin).name
        SkyWayContext.registerPlugin(plugin)
        val result = SkyWayContext.findPlugin("test")
        assertEquals(result, plugin)
    }
}
