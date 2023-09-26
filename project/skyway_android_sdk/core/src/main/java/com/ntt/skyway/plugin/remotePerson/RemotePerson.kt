/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.plugin.remotePerson

import com.ntt.skyway.core.channel.Repository
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.channel.member.RemoteMemberImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemotePerson internal constructor(dto: Member.Dto, private val repository: Repository) :
    RemoteMemberImpl(dto) {
    override val type: Member.Type = Member.Type.PERSON
    override val subType: String = "person"

    /**
     *  subscribeします。
     */
    suspend fun subscribe(publicationId: String): Subscription? = withContext(Dispatchers.Default) {
        val subscriptionJson =
            nativeSubscribe(nativePointer, publicationId) ?: return@withContext null
        return@withContext repository.addLocalSubscription(subscriptionJson)
    }

    /**
     *  unsubscribeします。
     */
    suspend fun unsubscribe(subscriptionId: String): Boolean = withContext(Dispatchers.Default) {
        return@withContext nativeUnsubscribe(nativePointer, subscriptionId)
    }

    private external fun nativeSubscribe(ptr: Long, publicationId: String): String?
    private external fun nativeUnsubscribe(ptr: Long, subscriptionId: String): Boolean
}
