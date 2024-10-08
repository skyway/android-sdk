package com.ntt.skyway.examples.p2proom.listener

import com.ntt.skyway.room.RoomPublication

interface RoomPublicationAdapterListener {
    fun onUnPublishClick(publication: RoomPublication)
    fun onSubscribeClick(publicationId: String)
    fun onUnSubscribeClick()
}
