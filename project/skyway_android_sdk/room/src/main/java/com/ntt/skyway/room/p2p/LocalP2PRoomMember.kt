/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.room.p2p

import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.room.Room
import com.ntt.skyway.room.member.LocalRoomMember

/**
 * LocalP2PRoomMemberの操作を行うクラス。
 */
class LocalP2PRoomMember internal constructor(
    room: Room,
    override val localPerson: LocalPerson
) : LocalRoomMember(room, localPerson) {

}
