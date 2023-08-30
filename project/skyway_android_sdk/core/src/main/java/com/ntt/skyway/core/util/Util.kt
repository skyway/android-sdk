/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.util

import com.google.gson.Gson
import com.google.gson.JsonObject

internal class Util {
    companion object {
        val memberLock = java.util.concurrent.locks.ReentrantLock()
        val publicationLock = java.util.concurrent.locks.ReentrantLock()
        val subscriptionLock = java.util.concurrent.locks.ReentrantLock()

        fun getObjectId(jsonString: String): String {
            val dto = Gson().fromJson(jsonString, JsonObject::class.java)
            return dto.get("id").asString
        }
    }
}
