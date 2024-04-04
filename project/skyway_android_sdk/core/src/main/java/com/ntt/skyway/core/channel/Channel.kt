package com.ntt.skyway.core.channel

import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.util.Logger

/**
 * Channelの操作を行うクラス。
 */
interface Channel {
    companion object {
        /**
         *  Channelを探します。
         */
        @JvmStatic
        suspend fun find(name: String? = null, id: String? = null): Channel? {
            return ChannelImpl.find(name, id)
        }

        /**
         *  Channelを作成します。
         */
        @JvmStatic
        suspend fun create(name: String? = null, metadata: String? = null): Channel? {
            return ChannelImpl.create(name, metadata)
        }

        /**
         *  Channelを探し、見つからなかった場合は作成します。
         */
        @JvmStatic
        suspend fun findOrCreate(name: String? = null, metadata: String? = null): Channel? {
            return ChannelImpl.findOrCreate(name, metadata)
        }
    }

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
                    else -> {
                        Logger.logE("Invalid state string($state). CLOSED is return as default state")
                        return CLOSED
                    }
                }
            }
        }
    }

    /**
     * このChannelのID。
     */
    val id: String

    /**
     * このChannelの名前。
     */
    val name: String?

    /**
     *  このChannelのMetadata。
     */
    val metadata: String?

    /**
     *  このChannelの状態。
     */
    val state: State

    /**
     * このChannel内の[Member]一覧。
     */
    val members: Set<Member>

    /**
     * このChannel内にこのSDKから参加しているLocalPerson。
     */
    val localPerson: LocalPerson?

    /**
     * このChannel内のBotの一覧。
     */
    val bots: Set<Member>

    /**
     * このChannel内の[Publication]の一覧。
     */
    val publications: Set<Publication>

    /**
     * このChannel内の[Subscription]の一覧。
     */
    val subscriptions: Set<Subscription>

    /**
     * このChannelが閉じられた時に発火するハンドラ。
     */
    var onClosedHandler: (() -> Unit)?

    /**
     * このChannelのMetadataが更新された時に発火するハンドラ。
     */
    var onMetadataUpdatedHandler: ((metadata: String) -> Unit)?

    /**
     * このChannel内のMemberの数が変更された時に発火するハンドラ。
     */
    var onMemberListChangedHandler: (() -> Unit)?

    /**
     * このChannelにMemberが入室した時に発火するハンドラ。
     */
    var onMemberJoinedHandler: ((member: Member) -> Unit)?

    /**
     * このChannelからMemberが退出した時に発火するハンドラ。
     */
    var onMemberLeftHandler: ((member: Member) -> Unit)?

    /**
     * このChannel内のMemberのMetadataが更新された時に発火するハンドラ。
     */
    var onMemberMetadataUpdatedHandler: ((member: Member, metadata: String) -> Unit)?

    /**
     * このChannel内のPublicationの数が変更された時に発火するハンドラ。
     */
    var onPublicationListChangedHandler: (() -> Unit)?

    /**
     * このChannel内にStreamがPublishされた時に発火するハンドラ。
     */
    var onStreamPublishedHandler: ((publication: Publication) -> Unit)?

    /**
     * このChannel内にStreamがUnpublishされた時に発火するハンドラ。
     */
    var onStreamUnpublishedHandler: ((publication: Publication) -> Unit)?

    /**
     * このChannel内のPublicationがEnableになった時に発火するハンドラ。
     */
    var onPublicationEnabledHandler: ((publication: Publication) -> Unit)?

    /**
     * このChannel内のPublicationがDisableになった時に発火するハンドラ。
     */
    var onPublicationDisabledHandler: ((publication: Publication) -> Unit)?

    /**
     * このChannel内のPublicationのMetadataが更新された時に発火するハンドラ。
     */
    var onPublicationMetadataUpdatedHandler: ((publication: Publication, metadata: String) -> Unit)?

    /**
     * このChannel内のSubscriptionのList変わった時に発火するハンドラ。
     */
    var onSubscriptionListChangedHandler: (() -> Unit)?

    /**
     * このChannel内のPublicationがSubscribeされた時に発火するハンドラ。
     * Subscriptionにはまだstreamがsetされていない可能性があります。
     */
    var onPublicationSubscribedHandler: ((subscription: Subscription) -> Unit)?

    /**
     * このChannel内のPublicationがUnsubscribeされた時に発火するハンドラ。
     */
    var onPublicationUnsubscribedHandler: ((subscription: Subscription) -> Unit)?

    /**
     * このChannel内の各種イベントでエラーが起きた時に発火するハンドラ。
     */
    var onErrorHandler: ((e: Exception) -> Unit)?

    /**
     *  Channelの[metadata]を更新します。
     *  [onMetadataUpdatedHandler]が発火します。
     */
    suspend fun updateMetadata(metadata: String): Boolean

    /**
     *  Channelに入室します。
     *  [onMemberJoinedHandler]が発火します。
     *  このChannel内に同時に入室できるLocalPersonは1つだけです。
     */
    suspend fun join(memberInit: Member.Init): LocalPerson?

    /**
     *  指定したメンバーをChannelから退室させます。
     *  [onMemberLeftHandler]が発火します。
     */
    suspend fun leave(member: Member): Boolean

    /**
     *  Channelを閉じます。
     *  [onClosedHandler]が発火します。
     */
    suspend fun close(): Boolean

    /**
     *  Channelを破棄します。
     *  破棄されたChannelではイベントが発火しなくなります。
     *  またChannel内の[Member]、[Publication]、[Subscription]も破棄されます。
     */
    fun dispose()
}
