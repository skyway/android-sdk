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
    val mimeType: String,
    /**
     *  コーデックのパラメータ
     */
    val parameters: Parameters
) {
    data class Parameters(
        /**
         *  dtxオプション。指定しない場合は有効になります。
         */
        val useDtx: Boolean? = null
    )

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

    constructor(mimeType: MimeType, parameters: Parameters = Parameters()) : this(
        mimeType.literal,
        parameters
    )

    companion object {
        /**
         *  @suppress
         */
        fun fromJsonArray(jsonArr: JsonArray): List<Codec> {
            return jsonArr.map {
                val jsonObject = it.asJsonObject
                val mimeType = MimeType.fromString(jsonObject.get("mimeType").asString)
                val parametersJson = jsonObject.get("parameters").asJsonObject
                val useDtx = if (parametersJson.has("useDtx")) {
                    parametersJson.get("useDtx").asBoolean
                } else {
                    null
                }
                val parameters = Parameters(useDtx = useDtx)
                Codec(mimeType, parameters)
            }
        }
    }
}
