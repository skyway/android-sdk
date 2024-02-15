package com.ntt.skyway.core.channel

import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Util
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@OptIn(InternalCoroutinesApi::class)
class Repository(channel: ChannelImpl) {
    private val _members = mutableMapOf<String, Member>()
    private val _publications = mutableMapOf<String, Publication>()
    private val _subscriptions = mutableMapOf<String, Subscription>()
    private val factory: Factory = Factory(channel)
    private val membersLock = Any()
    private val publicationsLock = Any()
    private val subscriptionsLock = Any()

    val members
        get() = synchronized(membersLock) {
            _members.map { it.value }.toSet()
        }
    val availableMembers
        get() = synchronized(membersLock) {
            _members.map { it.value }.filter { it.state != Member.State.LEFT }.toSet()
        }

    val publications
        get() = synchronized(publicationsLock) {
            _publications.map { it.value }
        }
    val availablePublications
        get() = synchronized(publicationsLock) {
            _publications.map { it.value }.filter { it.state != Publication.State.CANCELED }.toSet()
        }
    val subscriptions
        get() = synchronized(subscriptionsLock) {
            _subscriptions.map { it.value }
        }
    val availableSubscriptions
        get() = synchronized(subscriptionsLock) {
            _subscriptions.map { it.value }.filter { it.state != Subscription.State.CANCELED }
                .toSet()
        }

    internal fun findMember(memberId: String): Member? {
        synchronized(membersLock) {
            return _members[memberId]
        }
    }

    internal fun findPublication(publicationId: String): Publication? {
        synchronized(publicationsLock) {
            return _publications[publicationId]
        }
    }

    internal fun findSubscription(subscriptionId: String): Subscription? {
        synchronized(subscriptionsLock) {
            return _subscriptions[subscriptionId]
        }
    }

    internal fun addMemberIfNeeded(memberJson: String): Member {
        synchronized(membersLock) {
            val memberId = Util.getObjectId(memberJson)
            val exist = _members[memberId]
            if (exist != null) return exist

            val member = factory.createMember(memberJson)
            _members[memberId] = member
            return member
        }
    }

    internal fun addPublicationIfNeeded(
        publicationJson: String,
        stream: LocalStream?
    ): Publication {
        synchronized(publicationsLock) {
            val publicationId = Util.getObjectId(publicationJson)
            if (stream == null) {
                val exist = _publications[publicationId]
                if (exist != null) return exist
            }

            val publication = factory.createPublication(publicationJson, stream)
            _publications[publicationId] = publication
            return publication
        }
    }

    internal fun addSubscriptionIfNeeded(subscriptionJson: String): Subscription {
        synchronized(subscriptionsLock) {
            val subscription = factory.createSubscription(subscriptionJson)

            if (_subscriptions[subscription.id] == null) {
                _subscriptions[subscription.id] = subscription
            }

            if (_subscriptions[subscription.id]?.stream == null && subscription.stream != null) {
                _subscriptions[subscription.id]?.let {
                    (it as SubscriptionImpl).internalStream = subscription.stream
                }
            }
            return _subscriptions[subscription.id]!!
        }
    }
}
