package com.ntt.skyway.examples.common.listener

import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription

interface RoomPublicationAdapterListener {
    fun onUnPublishClick(publication: RoomPublication)
    fun onSubscribeClick(publicationId: String)
    fun onUnSubscribeClick()
    fun onSFUChangeEncodingClick(subscription: RoomSubscription)
}
