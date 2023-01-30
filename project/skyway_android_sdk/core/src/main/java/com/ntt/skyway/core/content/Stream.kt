/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content

abstract class Stream(
    val id: String,
    internal val nativePointer: Long
) {
    enum class Side {
        LOCAL, REMOTE
    }

    enum class ContentType {
        AUDIO, VIDEO, DATA;

        companion object {
            internal fun fromString(string: String): ContentType {
                return when (string) {
                    "Audio" -> AUDIO
                    "Video" -> VIDEO
                    "Data" -> DATA
                    else -> throw IllegalArgumentException("Unknown content type")
                }
            }
        }
    }

    internal data class Dto(
        val id: String,
        val nativePointer: Long
    )

    internal constructor(dto: Dto) : this(
        dto.id,
        dto.nativePointer
    )

    abstract fun dispose()

    abstract val side: Side
    abstract val contentType: ContentType
}
