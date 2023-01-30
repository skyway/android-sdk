/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel

import com.ntt.skyway.core.SkyWayContext
import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Logger
import com.ntt.skyway.core.util.Util
import com.ntt.skyway.core.util.Util.Companion.lock
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import java.lang.Exception
import kotlin.concurrent.withLock

/**
 * Channelの操作を行うクラス。
 */
class Channel internal constructor(
    /**
     * このChannelのID。
     */
    val id: String,
    /**
     * このChannelの名前。
     */
    val name: String,
    val nativePointer: Long
) {
    /**
     * Channelの状態。
     */
    enum class State {
        OPENED, CLOSED;

        companion object {
            internal fun fromString(state: String): State {
                return when (state) {
                    "opened" -> OPENED
                    "closed" -> CLOSED
                    else -> throw IllegalStateException("Invalid state")
                }
            }
        }
    }

    companion object {
        /**
         *  Channelを探します。
         */
        @JvmStatic
        suspend fun find(name: String? = null, id: String? = null): Channel? =
            withContext(Dispatchers.IO) {
                check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }

                val initDto = nativeFind(name, id) ?: return@withContext null
                return@withContext instantiateChannel(initDto)
            }

        /**
         *  Channelを作成します。
         */
        @JvmStatic
        suspend fun create(name: String? = null, metadata: String? = null): Channel? =
            withContext(Dispatchers.IO) {
                check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }

                val initDto = nativeCreate(name, metadata) ?: return@withContext null
                return@withContext instantiateChannel(initDto)
            }

        /**
         *  Channelを探し、見つからなかった場合は作成します。
         */
        @JvmStatic
        suspend fun findOrCreate(name: String? = null, metadata: String? = null): Channel? =
            withContext(Dispatchers.IO) {
                check(SkyWayContext.isSetup) { "Please setup SkyWayContext first" }

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
            channelName: String?,
            channelMetadata: String?
        ): String?
    }

    /**
     *  このChannelのMetadata。
     */
    val metadata: String
        get() = nativeMetadata(nativePointer)

    /**
     *  このChannelの状態。
     */
    val state: State
        get() = State.fromString(nativeState(nativePointer))

    /**
     * このChannel内の[Member]一覧。
     */
    val members: Set<Member>
        get() = _members.map { it.value }.filter { it.state != Member.State.LEFT }.toSet()

    /**
     * このChannel内にこのSDKから参加しているLocalPerson。
     */
    val localPerson: LocalPerson?
        get() = if (members.find { it.side == Member.Side.LOCAL } != null) {
            members.find { it.side == Member.Side.LOCAL } as LocalPerson
        } else {
            null
        }

    /**
     * このChannel内のBotの一覧。
     */
    val bots: Set<Member>
        get() = members.filter { it.type == Member.Type.BOT }.toSet()

    /**
     * このChannel内の[Publication]の一覧。
     */
    val publications: Set<Publication>
        get() = _publications.map { it.value }.filter { it.state != Publication.State.CANCELED }
            .toSet()

    /**
     * このChannel内の[Subscription]の一覧。
     */
    val subscriptions: Set<Subscription>
        get() = _subscriptions.map { it.value }.filter { it.state != Subscription.State.CANCELED }
            .toSet()

    /**
     * このChannelが閉じられた時に発火するハンドラ。
     */
    var onClosedHandler: (() -> Unit)? = null

    /**
     * このChannelのMetadataが更新された時に発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)? = null

    /**
     * このChannel内のMemberの数が変更された時に発火するハンドラ。
     */
    var onMemberListChangedHandler: (() -> Unit)? = null

    /**
     * このChannelにMemberが入室した時に発火するハンドラ。
     */
    var onMemberJoinedHandler: ((member: Member) -> Unit)? = null

    /**
     * このChannelからMemberが退出した時に発火するハンドラ。
     */
    var onMemberLeftHandler: ((member: Member) -> Unit)? = null

    /**
     * このChannel内のMemberのMetadataが更新された時に発火するハンドラ。
     */
    var onMemberMetadataUpdatedHandler: ((member: Member, metadata: String) -> Unit)? = null

    /**
     * このChannel内のPublicationの数が変更された時に発火するハンドラ。
     */
    var onPublicationListChangedHandler: (() -> Unit)? = null

    /**
     * このChannel内にStreamがPublishされた時に発火するハンドラ。
     */
    var onStreamPublishedHandler: ((publication: Publication) -> Unit)? = null

    /**
     * このChannel内にStreamがUnpublishされた時に発火するハンドラ。
     */
    var onStreamUnpublishedHandler: ((publication: Publication) -> Unit)? = null

    /**
     * このChannel内のPublicationがEnableになった時に発火するハンドラ。
     */
    var onPublicationEnabledHandler: ((publication: Publication) -> Unit)? = null

    /**
     * このChannel内のPublicationがDisableになった時に発火するハンドラ。
     */
    var onPublicationDisabledHandler: ((publication: Publication) -> Unit)? = null

    /**
     * このChannel内のPublicationのMetadataが更新された時に発火するハンドラ。
     */
    var onPublicationMetadataUpdatedHandler: ((publication: Publication, metadata: String) -> Unit)? =
        null

    /**
     * このChannel内のSubscriptionのList変わった時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)? = null

    /**
     * このChannel内のPublicationがSubscribeされた時に発火するハンドラ。
     */
    var onPublicationSubscribedHandler: ((subscription: Subscription) -> Unit)? = null

    /**
     * このChannel内のPublicationがUnsubscribeされた時に発火するハンドラ。
     */
    var onPublicationUnsubscribedHandler: ((subscription: Subscription) -> Unit)? = null

//    /**
//     * このChannel内のSubscriptionがEnableになった時に発火するハンドラ。
//     */
//    var onSubscriptionEnabledHandler: ((subscription: Subscription) -> Unit)? = null
//
//    /**
//     * このChannel内のSubscriptionがDisableになった時に発火するハンドラ。
//     */
//    var onSubscriptionDisabledHandler: ((subscription: Subscription) -> Unit)? = null

    /**
     * このChannel内の各種イベントでエラーが起きた時に発火するハンドラ。
     */
    var onErrorHandler: ((e: Exception) -> Unit)? = null

    private val factory: Factory = Factory(this)
    private val _members = mutableMapOf<String, Member>()
    private val _publications = mutableMapOf<String, Publication>()
    private val _subscriptions = mutableMapOf<String, Subscription>()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        nativeAddEventListener(nativePointer)
    }

    /**
     *  Channelの[metadata]を更新します。
     *  [onMetadataUpdatedHandler]が発火します。
     */
    suspend fun updateMetadata(metadata: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeUpdateMetadata(nativePointer, metadata)
    }

    /**
     *  Channelに入室します。
     *  [onMemberJoinedHandler]が発火します。
     *  このChannel内に同時に入室できるLocalPersonは1つだけです。
     */
    suspend fun join(memberInit: Member.Init): LocalPerson? = withContext(Dispatchers.IO) {
        lock.withLock {
            resetLocalPerson()
            val localPersonJson = nativeJoin(
                nativePointer,
                memberInit.name,
                memberInit.metadata ?: "",
                memberInit.type.toString(),
                memberInit.subtype,
                memberInit.keepAliveIntervalSec
            ) ?: return@withContext null
            return@withContext addLocalParson(localPersonJson)
        }
    }

    /**
     *  指定したメンバーをChannelから退室させます。
     *  [onMemberLeftHandler]が発火します。
     */
    suspend fun leave(member: Member): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeLeave(nativePointer, member.nativePointer)
    }

    /**
     *  Channelを閉じます。
     *  [onClosedHandler]が発火します。
     */
    suspend fun close(): Boolean = withContext(Dispatchers.IO) {
        return@withContext nativeClose(nativePointer)
    }

    /**
     *  Channelを破棄します。
     *  破棄されたChannelではイベントが発火しなくなります。
     *  またChannel内の[Member]、[Publication]、[Subscription]も破棄されます。
     */
    fun dispose() {
        nativeDispose(nativePointer)
    }

    internal fun findMember(memberId: String): Member? {
        lock.withLock {
            return _members[memberId]
        }
    }

    internal fun findPublication(publicationId: String): Publication? {
        lock.withLock {
            return _publications[publicationId]
        }
    }

    internal fun findSubscription(subscriptionId: String): Subscription? {
        lock.withLock {
            return _subscriptions[subscriptionId]
        }
    }

    private fun addLocalParson(memberJson: String): LocalPerson {
        lock.withLock {
            val member = factory.createLocalPerson(memberJson)
            _members[member.id] = member
            return member
        }
    }

    internal fun addRemoteMemberIfNeeded(memberJson: String): Member {
        lock.withLock {
            val memberId = Util.getObjectId(memberJson)
            val exist = _members[memberId]
            if (exist != null) return exist

            val member = factory.createRemoteMember(memberJson)
            _members[memberId] = member
            return member
        }
    }

    internal fun addLocalPublication(publicationJson: String, stream: LocalStream): Publication {
        lock.withLock {
            val publicationId = Util.getObjectId(publicationJson)
            val publication = factory.createPublication(publicationJson, stream)
            _publications[publicationId] = publication
            return publication
        }
    }

    internal fun addRemotePublicationIfNeeded(publicationJson: String): Publication {
        lock.withLock {
            val publicationId = Util.getObjectId(publicationJson)
            val exist = _publications[publicationId]
            if (exist != null) return exist

            val publication = factory.createPublication(publicationJson, null)
            _publications[publicationId] = publication
            return publication
        }
    }

    internal fun addLocalSubscription(subscriptionJson: String): Subscription {
        lock.withLock {
            val subscriptionId = Util.getObjectId(subscriptionJson)
            val subscription = factory.createSubscription(subscriptionJson)
            _subscriptions[subscriptionId] = subscription
            return subscription
        }
    }

    internal fun addRemoteSubscriptionIfNeeded(subscriptionJson: String): Subscription {
        lock.withLock {
            val subscriptionId = Util.getObjectId(subscriptionJson)
            val exist = _subscriptions[subscriptionId]
            if (exist != null) return exist

            val subscription = factory.createSubscription(subscriptionJson)
            _subscriptions[subscriptionId] = subscription
            return subscription
        }
    }

    private fun resetLocalPerson() {
        val filterLocalMembers = _members.filter { (_, member) -> member.side == Member.Side.LOCAL }
        filterLocalMembers.forEach { (_, member) ->
            _members.remove(member.id)
        }
    }

    private fun onClosed() {
        onClosedHandler?.invoke()
    }

    private fun onMetadataUpdated(metadata: String) {
        onMetadataUpdatedHandler?.invoke(metadata)
    }

    private fun onMemberListChanged() {
        onMemberListChangedHandler?.invoke()
    }

    private fun onMemberJoined(memberJson: String) {
        val member: Member
        try {
            member = addRemoteMemberIfNeeded(memberJson)
        } catch (e: Exception) {
            onError("onMemberJoined: ${e.message}", e)
            return
        }

        onMemberJoinedHandler?.invoke(member)
    }

    private fun onMemberLeft(memberJson: String) {
        val memberId = Util.getObjectId(memberJson)
        val member = findMember(memberId) ?: run {
            Logger.logW("onMemberLeft: The member is not found")
            return
        }
        onMemberLeftHandler?.invoke(member)
    }

    private fun onMemberMetadataUpdated(memberJson: String, metadata: String) {
        val memberId = Util.getObjectId(memberJson)
        val member = findMember(memberId) ?: run {
            Logger.logW("onMemberMetadataUpdated: The member is not found")
            return
        }
        onMemberMetadataUpdatedHandler?.invoke(member, metadata)
    }

    private fun onPublicationMetadataUpdated(publicationJson: String, metadata: String) {
        val publicationId = Util.getObjectId(publicationJson)
        val publication = findPublication(publicationId) ?: run {
            Logger.logW("onPublicationMetadataUpdated: The publication is not found")
            return
        }
        onPublicationMetadataUpdatedHandler?.invoke(publication, metadata)
    }

    private fun onPublicationListChanged() {
        onPublicationListChangedHandler?.invoke()
    }

    private fun onStreamPublished(publicationJson: String) {
        scope.launch {
            localPerson?.publishMutex?.withLock {
                val publication: Publication

                try {
                    publication = addRemotePublicationIfNeeded(publicationJson)
                } catch (e: Exception) {
                    onError("onStreamPublished: ${e.message}", e)
                    return@launch
                }
                onStreamPublishedHandler?.invoke(publication)
            }
        }
    }

    private fun onStreamUnpublished(publicationJson: String) {
        val publicationId = Util.getObjectId(publicationJson)
        val publication = findPublication(publicationId) ?: run {
            Logger.logW("onStreamUnpublished: The publication is not found")
            return
        }
        onStreamUnpublishedHandler?.invoke(publication)
    }

    private fun onPublicationEnabled(publicationJson: String) {
        val publicationId = Util.getObjectId(publicationJson)
        val publication = findPublication(publicationId) ?: run {
            Logger.logW("onPublicationEnabled: The publication is not found")
            return
        }
        onPublicationEnabledHandler?.invoke(publication)
    }

    private fun onPublicationDisabled(publicationJson: String) {
        val publicationId = Util.getObjectId(publicationJson)
        val publication = findPublication(publicationId) ?: run {
            Logger.logW("onPublicationDisabled: The publication is not found")
            return
        }
        onPublicationDisabledHandler?.invoke(publication)
    }

    private fun onSubscriptionListChanged() {
        onSubscriptionListChangedHandler?.invoke()
    }

    private fun onPublicationSubscribed(subscriptionJson: String) {
        scope.launch {
            localPerson?.subscribeMutex?.withLock {
                val subscription: Subscription
                try {
                    subscription = addRemoteSubscriptionIfNeeded(subscriptionJson)
                } catch (e: Exception) {
                    onError("onPublicationSubscribed: ${e.message}", e)
                    return@launch
                }

                onPublicationSubscribedHandler?.invoke(subscription)
            }
        }
    }

    private fun onPublicationUnsubscribed(subscriptionJson: String) {
        val subscriptionId = Util.getObjectId(subscriptionJson)
        val subscription = findSubscription(subscriptionId) ?: run {
            Logger.logW("onPublicationUnsubscribed: The subscription is not found")
            return
        }
        onPublicationUnsubscribedHandler?.invoke(subscription)
    }

//    private fun onSubscriptionEnabled(subscriptionJson: String) {
//        val subscriptionId = Util.getObjectId(subscriptionJson)
//        val subscription = findSubscription(subscriptionId) ?: run {
//            Logger.logW("onSubscriptionEnabled: The subscription is not found")
//            return
//        }
//        onSubscriptionEnabledHandler?.invoke(subscription)
//    }
//
//    private fun onSubscriptionDisabled(subscriptionJson: String) {
//        val subscriptionId = Util.getObjectId(subscriptionJson)
//        val subscription = findSubscription(subscriptionId) ?: run {
//            Logger.logW("onSubscriptionDisabled: The subscription is not found")
//            return
//        }
//        onSubscriptionDisabledHandler?.invoke(subscription)
//    }

    private fun onError(message: String, e: Exception) {
        Logger.logE(message)
        onErrorHandler?.invoke(e)
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
