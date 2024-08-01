package com.ntt.skyway.libskywaytest.test

import android.Manifest
import android.util.Log
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking
import androidx.test.platform.app.InstrumentationRegistry
import com.ntt.skyway.libskywaytest.SkywayTest
import org.junit.*
import org.junit.Assert.*


class LibSkyWayTest {
    val TAG = this.javaClass.simpleName

    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    @Before
    fun setup() {
        Log.d(TAG, "LibSkyWayTest setup()")
    }

    @After
    fun tearDown() {
        Log.d(TAG, "LibSkyWayTest tearDown()")
    }


//    -----------------TEST-----------------

    @Test
    fun libSkywayAllTest() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testResult = SkywayTest.startTest(appContext)
        Log.d(TAG, "LibSkyWayTest testResult: " + testResult)
        assertNotNull(testResult)
        assertEquals(testResult, 0) // 0 for passing all tests and 1 for failing test
    }

}
