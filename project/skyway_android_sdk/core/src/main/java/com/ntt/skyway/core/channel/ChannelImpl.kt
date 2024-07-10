/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.newFixedThreadPoolContext

class ChannelImpl internal constructor(
    override val id: String, override val name: String?, val nativePointer: Long
) : Channel {
    companion object {
        @JvmStatic
        suspend fun find(name: String? = null, id: String? = null): Channel? =
            withContext(SkyWayContext.threadContext) {
                if (!SkyWayContext.isSetup) {
                    Logger.logE("Please setup SkyWayContext first")
                    return@withContext null
                }

                val initDto = nativeFind(name, id) ?: return@withContext null
                return@withContext instantiateChannel(initDto)
            }

        @JvmStatic
        suspend fun create(name: String? = null, metadata: String? = null): Channel? =
            withContext(SkyWayContext.threadContext) {
                if (!SkyWayContext.isSetup) {
                    Logger.logE("Please setup SkyWayContext first")
                    return@withContext null
                }

                val initDto = nativeCreate(name, metadata) ?: return@withContext null
                return@withContext instantiateChannel(initDto)
            }

        @JvmStatic
        suspend fun findOrCreate(name: String? = null, metadata: String? = null): Channel? =
            withContext(SkyWayContext.threadContext) {
                if (!SkyWayContext.isSetup) {
                    Logger.logE("Please setup SkyWayContext first")
                    return@withContext null
                }

                val initDto = nativeFindOrCreate(name, metadata) ?: return@withContext null
                return@withContext instantiateChannel(initDto)
            }

        private fun instantiateChannel(channelJson: String): Channel {
            return Factory.createChannel(channelJson)
        }

        @JvmStatic
        private external fun nativeCreate(channelName: String?, channelMetadata: String?): String?

        @JvmStatic
        private external fun nativeFind(channelName: String?, channelId: String?): String?

        @JvmStatic
        private external fun nativeFindOrCreate(
            channelName: String?, channelMetadata: String?
        ): String?
    }

    override val metadata: String?
        get() {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return null
            }
            return nativeMetadata(nativePointer).takeUnless { it.isBlank() }
        }

    override val state: Channel.State
        get() {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return Channel.State.CLOSED
            }
            return Channel.State.fromString(nativeState(nativePointer))
        }

    override val members: Set<Member>
        get() = repository.availableMembers

    override val localPerson: LocalPerson?
        get() = if (members.find { it.side == Member.Side.LOCAL } != null) {
            members.find { it.side == Member.Side.LOCAL } as LocalPerson
        } else {
            null
        }

    override val bots: Set<Member>
        get() = members.filter { it.type == Member.Type.BOT }.toSet()

    override val publications: Set<Publication>
        get() = repository.availablePublications

    override val subscriptions: Set<Subscription>
        get() = repository.availableSubscriptions

    override var onClosedHandler: (() -> Unit)? = null

    override var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    override var onMemberListChangedHandler: (() -> Unit)? = null

    override var onMemberJoinedHandler: ((member: Member) -> Unit)? = null

    override var onMemberLeftHandler: ((member: Member) -> Unit)? = null

    override var onMemberMetadataUpdatedHandler: ((member: Member, metadata: String) -> Unit)? =
        null

    override var onPublicationListChangedHandler: (() -> Unit)? = null

    override var onStreamPublishedHandler: ((publication: Publication) -> Unit)? = null

    override var onStreamUnpublishedHandler: ((publication: Publication) -> Unit)? = null

    override var onPublicationEnabledHandler: ((publication: Publication) -> Unit)? = null

    override var onPublicationDisabledHandler: ((publication: Publication) -> Unit)? = null

    override var onPublicationMetadataUpdatedHandler: ((publication: Publication, metadata: String) -> Unit)? =
        null

    override var onSubscriptionListChangedHandler: (() -> Unit)? = null

    override var onPublicationSubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    override var onPublicationUnsubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    override var onErrorHandler: ((e: Exception) -> Unit)? = null

    override val _threadContext = Dispatchers.Default

    val repository: Repository = Repository(this)
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        nativeAddEventListener(nativePointer)
    }

    override suspend fun updateMetadata(metadata: String): Boolean =
        withContext(_threadContext) {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return@withContext false
            }
            return@withContext nativeUpdateMetadata(nativePointer, metadata)
        }

    override suspend fun join(memberInit: Member.Init): LocalPerson? =
        withContext(_threadContext) {
            if (!SkyWayContext.isSetup) {
                Logger.logE("SkyWayContext is disposed.")
                return@withContext null
            }
            val localPersonJson = nativeJoin(
                nativePointer,
                memberInit.name,
                memberInit.metadata ?: "",
                memberInit.type.toString(),
                memberInit.subtype,
                memberInit.keepAliveIntervalSec
            ) ?: return@withContext null
            return@withContext repository.addMemberIfNeeded(localPersonJson) as LocalPerson
        }


    override suspend fun leave(member: Member): Boolean = withContext(_threadContext) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext false
        }
        return@withContext nativeLeave(nativePointer, member.nativePointer)
    }

    override suspend fun close(): Boolean = withContext(_threadContext) {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return@withContext false
        }
        return@withContext nativeClose(nativePointer)
    }

    override fun dispose() {
        if (!SkyWayContext.isSetup) {
            Logger.logE("SkyWayContext is disposed.")
            return
        }
        nativeDispose(nativePointer)
    }

    private fun onClosed() {
        Logger.logI("🔔onClosed")
        scope.launch {
            onClosedHandler?.invoke()
        }
    }

    private fun onMetadataUpdated(metadata: String) {
        Logger.logI("🔔onMetadataUpdated")
        scope.launch {
            onMetadataUpdatedHandler?.invoke(metadata)
        }
    }

    private fun onMemberListChanged() {
        Logger.logI("🔔onMemberListChanged")
        scope.launch {
            onMemberListChangedHandler?.invoke()
        }
    }

    private fun onMemberJoined(memberJson: String) {
        Logger.logI("🔔onMemberJoined")
        val member = repository.addMemberIfNeeded(memberJson)
        scope.launch {
            onMemberJoinedHandler?.invoke(member)
        }
    }

    private fun onMemberLeft(memberId: String) {
        Logger.logI("🔔onMemberLeft")
        val member = repository.findMember(memberId) ?: run {
            Logger.logW("onMemberLeft: The member is not found")
            return
        }
        scope.launch {
            onMemberLeftHandler?.invoke(member)
        }
    }

    private fun onMemberMetadataUpdated(memberId: String, metadata: String) {
        Logger.logI("🔔onMemberMetadataUpdated")
        val member = repository.findMember(memberId) ?: run {
            Logger.logW("onMemberMetadataUpdated: The member is not found")
            return
        }
        scope.launch {
            onMemberMetadataUpdatedHandler?.invoke(member, metadata)
        }
    }

    private fun onPublicationMetadataUpdated(publicationId: String, metadata: String) {
        Logger.logI("🔔onPublicationMetadataUpdated")
        val publication = repository.findPublication(publicationId) ?: run {
            Logger.logW("onPublicationMetadataUpdated: The publication is not found")
            return
        }
        scope.launch {
            onPublicationMetadataUpdatedHandler?.invoke(publication, metadata)
        }
    }

    private fun onPublicationListChanged() {
        Logger.logI("🔔onPublicationListChanged")
        scope.launch {
            onPublicationListChangedHandler?.invoke()
        }
    }

    private fun onStreamPublished(publicationJson: String) {
        Logger.logI("🔔onStreamPublished")
        val publication = repository.addPublicationIfNeeded(publicationJson, null)
        scope.launch {
            onStreamPublishedHandler?.invoke(publication)
        }
    }

    private fun onStreamUnpublished(publicationId: String) {
        Logger.logI("🔔onStreamUnpublished")
        val publication = repository.findPublication(publicationId) ?: run {
            Logger.logW("onStreamUnpublished: The publication is not found")
            return
        }
        scope.launch {
            onStreamUnpublishedHandler?.invoke(publication)
        }
    }

    private fun onPublicationEnabled(publicationId: String) {
        Logger.logI("🔔onPublicationEnabled")
        val publication = repository.findPublication(publicationId) ?: run {
            Logger.logW("onPublicationEnabled: The publication is not found")
            return
        }
        scope.launch {
            onPublicationEnabledHandler?.invoke(publication)
        }
    }

    private fun onPublicationDisabled(publicationId: String) {
        Logger.logI("🔔onPublicationDisabled")
        val publication = repository.findPublication(publicationId) ?: run {
            Logger.logW("onPublicationDisabled: The publication is not found")
            return
        }
        scope.launch {
            onPublicationDisabledHandler?.invoke(publication)
        }
    }

    private fun onSubscriptionListChanged() {
        Logger.logI("🔔onSubscriptionListChanged")
        scope.launch {
            onSubscriptionListChangedHandler?.invoke()
        }
    }

    private fun onPublicationSubscribed(subscriptionJson: String) {
        Logger.logI("🔔onPublicationSubscribed")
        val subscription = repository.addSubscriptionIfNeeded(subscriptionJson)
        scope.launch {
            onPublicationSubscribedHandler?.invoke(subscription)
        }
    }

    private fun onPublicationUnsubscribed(subscriptionId: String) {
        Logger.logI("🔔onPublicationUnsubscribed")
        val subscription = repository.findSubscription(subscriptionId) ?: run {
            Logger.logW("onPublicationUnsubscribed: The subscription is not found")
            return
        }
        scope.launch {
            onPublicationUnsubscribedHandler?.invoke(subscription)
        }
    }

    private fun onError(message: String, e: Exception) {
        Logger.logE(message)
        scope.launch {
            onErrorHandler?.invoke(e)
        }
    }

    private external fun nativeState(ptr: Long): String
    private external fun nativeMetadata(ptr: Long): String
    private external fun nativeAddEventListener(ptr: Long)
    private external fun nativeUpdateMetadata(ptr: Long, metadata: String): Boolean
    private external fun nativeJoin(
        ptr: Long,
        name: String?,
        metadata: String,
        type: String,
        subtype: String,
        keepAliveIntervalSec: Int
    ): String?

    private external fun nativeLeave(ptr: Long, memberPointer: Long): Boolean
    private external fun nativeClose(ptr: Long): Boolean
    private external fun nativeDispose(ptr: Long)
}
