package com.ntt.skyway.manager

abstract class Manager {
    abstract suspend fun findOrCreate(name:String?) : Boolean
    abstract suspend fun find(name:String? = null,id:String? = null) : Boolean
    abstract suspend fun create(name:String?) : Boolean
    abstract suspend fun join(name:String,metadata:String?):Boolean
    abstract suspend fun close() : Boolean
    abstract suspend fun dispose() : Boolean
}