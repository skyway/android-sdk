/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content

import com.google.gson.JsonArray

/**
 *  エンコード設定。
 */
data class Encoding(
    /**
     *  エンコード設定のID。任意の文字列を指定します。
     */
    val id: String?,
    /**
     *  最大ビットレート。
     */
    val maxBitrate: Int? = null,
    /**
     *  動画のサイズを縮小する倍率。
     */
    val scaleResolutionDownBy: Double? = null,
    /**
     *  最大フレームレート。
     */
    val maxFramerate: Double? = null
) {
    companion object {
        /**
         *  @suppress
         */
        fun fromJsonArray(jsonArr: JsonArray): List<Encoding> {
            return jsonArr.map {
                Encoding(
                    it.asJsonObject.get("id")?.asString,
                    it.asJsonObject.get("maxBitrate")?.asInt,
                    it.asJsonObject.get("scaleResolutionDownBy")?.asDouble,
                    it.asJsonObject.get("maxFramerate")?.asDouble,
                )
            }
        }
    }
}
