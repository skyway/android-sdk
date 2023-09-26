package com.ntt.skyway.core.channel

import com.ntt.skyway.core.channel.member.LocalPerson
import com.ntt.skyway.core.channel.member.Member
import com.ntt.skyway.core.content.local.LocalStream
import com.ntt.skyway.core.util.Util

class Repository(channel: ChannelImpl) {
    private val _members = mutableMapOf<String, Member>()
    private val _publications = mutableMapOf<String, Publication>()
    private val _subscriptions = mutableMapOf<String, Subscription>()
    private val factory: Factory = Factory(channel)

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

    internal fun addLocalParson(memberJson: String): LocalPerson {
        val member = factory.createLocalPerson(memberJson)
        _members[member.id] = member
        return member
    }

    internal fun addRemoteMemberIfNeeded(memberJson: String): Member {
        val memberId = Util.getObjectId(memberJson)
        val exist = _members[memberId]
        if (exist != null) return exist

        val member = factory.createRemoteMember(memberJson)
        _members[memberId] = member
        return member
    }

    internal fun addLocalPublication(publicationJson: String, stream: LocalStream): Publication {
        val publicationId = Util.getObjectId(publicationJson)
        val publication = factory.createPublication(publicationJson, stream)
        _publications[publicationId] = publication
        return publication
    }

    internal fun addRemotePublicationIfNeeded(publicationJson: String): Publication {
        val publicationId = Util.getObjectId(publicationJson)
        val exist = _publications[publicationId]
        if (exist != null) return exist

        val publication = factory.createPublication(publicationJson, null)
        _publications[publicationId] = publication
        return publication
    }

    internal fun addLocalSubscription(subscriptionJson: String): Subscription {
        val subscriptionId = Util.getObjectId(subscriptionJson)
        val subscription = factory.createSubscription(subscriptionJson)
        _subscriptions[subscriptionId] = subscription
        return subscription
    }

    internal fun addRemoteSubscriptionIfNeeded(subscriptionJson: String): Subscription {
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

    internal fun resetLocalPerson() {
        val filterLocalMembers = _members.filter { (_, member) -> member.side == Member.Side.LOCAL }
        filterLocalMembers.forEach { (_, member) ->
            _members.remove(member.id)
        }
    }
}
