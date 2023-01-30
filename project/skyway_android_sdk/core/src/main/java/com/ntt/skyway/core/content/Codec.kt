/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content

import com.google.gson.JsonArray

/**
 *  コーデック。
 */
data class Codec(
    /**
     *  メディアタイプ。
     */
    val mimeType: String
) {
    /**
     *  メディアタイプの一覧。
     */
    enum class MimeType(val literal: String) {
        H264("video/h264"),
        VP8("video/VP8"),
        VP9("video/VP9"),
        AV1("video/AV1"),
        OPUS("audio/opus"),
        RED("audio/red");

        companion object {
            internal fun fromString(literal: String): MimeType {
                return values().find { it.literal == literal }
                    ?: throw IllegalArgumentException("Unknown mimeType")
            }
        }
    }

    constructor(mimeType: MimeType) : this(mimeType.literal)

    companion object {
        fun fromJsonArray(jsonArr: JsonArray): List<Codec> {
            return jsonArr.map {
                val mimeType = MimeType.fromString(it.asJsonObject.get("mimeType").asString)
                Codec(mimeType)
            }
        }
    }
}
