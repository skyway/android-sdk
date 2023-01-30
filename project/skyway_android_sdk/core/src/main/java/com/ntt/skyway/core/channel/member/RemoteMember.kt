/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

/**
 * RemoteMemberの操作を行うクラス。
 */
abstract class RemoteMember constructor(dto: Dto) : Member(dto) {
    /**
     *  常に[Member.Side.REMOTE]を返します。
     */
    override val side = Side.REMOTE

    init {
        super.addEventListener()
    }

//    fun getStats(publication: Publication): String {
//        return nativeGetStatsOfPublication(nativePointer, publication.nativePointer)
//    }
//
//    fun getStats(subscription: Subscription): String {
//        return nativeGetStatsOfSubscription(nativePointer, subscription.nativePointer)
//    }

    private external fun nativeGetStatsOfPublication(ptr: Long, publicationPtr: Long): String
    private external fun nativeGetStatsOfSubscription(ptr: Long, subscriptionPtr: Long): String
}
