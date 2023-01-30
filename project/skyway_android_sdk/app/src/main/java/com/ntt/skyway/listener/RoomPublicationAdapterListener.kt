package com.ntt.skyway.listener

interface RoomPublicationAdapterListener {
    fun onSubscribeClick(publicationId: String)
    fun onUnSubscribeClick()
    fun onEnableClick(publicationId: String)
    fun onDisableClick(publicationId: String)
}
