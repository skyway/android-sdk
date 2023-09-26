/*
 * Copyright © 2023 NTT Communications. All rights reserved.
 */

package com.ntt.skyway.core.channel.member

import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.core.channel.Subscription
import com.ntt.skyway.core.content.local.LocalStream

/**
 * LocalPersonの操作を行うクラス。
 */
abstract class LocalPerson : Member {
    override val type = Member.Type.PERSON
    override val subType = "person"

    /**
     *  常に[Member.Side.LOCAL]を返します。
     */
    override val side = Member.Side.LOCAL

    /**
     * このLocalPersonがpublishした時に発火するハンドラ。
     */
    abstract var onStreamPublishedHandler: ((publication: Publication) -> Unit)?

    /**
     * このLocalPersonがunpublishした時に発火するハンドラ。
     */
    abstract var onStreamUnpublishedHandler: ((publication: Publication) -> Unit)?

    /**
     * このLocalPersonがsubscribeした時に発火するハンドラ。
     */
    abstract var onPublicationSubscribedHandler: ((subscription: Subscription) -> Unit)?

    /**
     * このLocalPersonがunsubscribeした時に発火するハンドラ。
     */
    abstract var onPublicationUnsubscribedHandler: ((subscription: Subscription) -> Unit)?

    /**
     *  Streamをpublishします。
     */
    abstract suspend fun publish(
        localStream: LocalStream, options: Publication.Options? = null
    ): Publication?

    /**
     *  Publicationをunpublishします。
     */
    abstract suspend fun unpublish(publicationId: String): Boolean

    abstract suspend fun unpublish(publication: Publication): Boolean

    /**
     *  Publicationをsubscribeします。
     */
    abstract suspend fun subscribe(
        publicationId: String, options: Subscription.Options? = null
    ): Subscription?

    abstract suspend fun subscribe(
        publication: Publication, options: Subscription.Options? = null
    ): Subscription?

    /**
     *  Publicationをunsubscribeします。
     */
    abstract suspend fun unsubscribe(subscriptionId: String): Boolean

    abstract suspend fun unsubscribe(subscription: Subscription): Boolean
}
