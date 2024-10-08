package com.ntt.skyway.examples.sfuroom

import com.ntt.skyway.room.sfu.LocalSFURoomMember
import com.ntt.skyway.room.sfu.SFURoom


object SFURoomManager {
    var sfuRoom: SFURoom? = null
    var localPerson: LocalSFURoomMember? = null
    var isSkywayContextSetup = false
}
