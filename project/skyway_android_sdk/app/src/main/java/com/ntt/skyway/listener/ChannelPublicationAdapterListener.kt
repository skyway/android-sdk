package com.ntt.skyway.listener

interface ChannelPublicationAdapterListener {
    suspend fun onUnPublishClick(publicationId: String):Boolean
    suspend fun onUpdateEncodingClick(publicationId: String):Boolean
    suspend fun onChangeEncodingClick(publicationId: String):Boolean
    suspend fun onSendDataClick(publicationId: String):Boolean
    suspend fun onReplaceStreamClick(publicationId: String):Boolean
    suspend fun onSubscribeClick(publicationId: String):Boolean
    suspend fun onUnSubscribeClick(publicationId: String):Boolean
    suspend fun onStartForwardingClick(publicationId: String):Boolean
    suspend fun onEnableClick(publicationId: String):Boolean
    suspend fun onDisableClick(publicationId: String):Boolean
}