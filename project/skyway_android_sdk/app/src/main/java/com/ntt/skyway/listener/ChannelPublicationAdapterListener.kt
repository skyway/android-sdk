package com.ntt.skyway.listener

interface ChannelPublicationAdapterListener {
    fun onSubscribeClick(publicationId: String)
    fun onUnSubscribeClick()
    fun onStartForwardingClick(publicationId: String)
    fun onEnableClick(publicationId: String)
    fun onDisableClick(publicationId: String)
}