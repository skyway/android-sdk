package com.ntt.skyway.examples.sfuroom.listener

import com.ntt.skyway.room.RoomPublication

interface RoomPublicationAdapterListener {
    fun onUnPublishClick(publication: RoomPublication)
    fun onSubscribeClick(publicationId: String)
    fun onUnSubscribeClick()
}
