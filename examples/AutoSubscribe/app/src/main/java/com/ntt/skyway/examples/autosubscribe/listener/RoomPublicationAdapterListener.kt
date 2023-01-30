package com.ntt.skyway.examples.autosubscribe.listener

import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription

interface RoomPublicationAdapterListener {
    fun onUnPublishClick(publication: RoomPublication)
    fun onUnSubscribeClick()
    fun onSFUChangeEncodingClick(subscription: RoomSubscription)
}
