/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

/**
 * RemoteMemberの操作を行うクラス。
 */
abstract class RemoteMember : Member {
    /**
     *  常に[Member.Side.REMOTE]を返します。
     */
    override val side = Member.Side.REMOTE
}
