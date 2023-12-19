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
        get() = _members.map { it.value }.toSet()
    val availableMembers
        get() = members.filter { it.state != Member.State.LEFT }.toSet()
    val publications
        get() = _publications.map { it.value }
    val availablePublications
        get() = publications.filter { it.state != Publication.State.CANCELED }.toSet()
    val subscriptions
        get() = _subscriptions.map { it.value }
    val availableSubscriptions
        get() = subscriptions.filter { it.state != Subscription.State.CANCELED }.toSet()

    internal fun findMember(memberId: String): Member? {
        return _members[memberId]
    }

    internal fun findPublication(publicationId: String): Publication? {
        return _publications[publicationId]
    }

    internal fun findSubscription(subscriptionId: String): Subscription? {
        return _subscriptions[subscriptionId]
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

    internal fun resetLocalPerson() {
        synchronized(membersLock) {
            val filterLocalMembers =
                _members.filter { (_, member) -> member.side == Member.Side.LOCAL }
            filterLocalMembers.forEach { (_, member) ->
                _members.remove(member.id)
            }
        }
    }
}
