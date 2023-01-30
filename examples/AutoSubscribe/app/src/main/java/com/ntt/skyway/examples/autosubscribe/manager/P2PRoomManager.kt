package com.ntt.skyway.examples.autosubscribe.manager

import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.p2p.P2PRoom


object P2PRoomManager {
    var room: P2PRoom? = null
    var localPerson: LocalP2PRoomMember? = null
    var isSkywayContextSetup = false
}
