/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.content

import com.google.gson.JsonElement

@Deprecated("This API is deprecated.", ReplaceWith("", ""))
data class WebRTCStatsReport(
    /**
     *  統計情報のID。
     */
    val id: String,
    /**
     *  統計情報のType。
     */
    val type: String,
    /**
     *  IDとType以外のパラメータ。
     */
    val params: Map<String, JsonElement>
)
