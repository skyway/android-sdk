package com.ntt.skyway.examples.common.manager

object SampleManager {
    enum class Type(val displayName: String) {
        P2P_ROOM("P2P Room"),
        SFU_ROOM("SFU Room"),
        AUTO_SUBSCRIBE("Auto Subscribe")
    }
    var type : Type? = null
}
