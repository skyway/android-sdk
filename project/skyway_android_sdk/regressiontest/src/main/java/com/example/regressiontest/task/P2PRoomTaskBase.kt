package com.example.regressiontest.task

import com.example.regressiontest.util.Util
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.p2p.P2PRoom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.UUID

abstract class P2PRoomTaskBase(listener: Listener, params: Params) :
    TaskBase(listener, params) {

    override val TAG = this.javaClass.simpleName
    val JOIN_RETRY_COUNT = 5
    val JOIN_RETRY_WAIT_TIME = 1000L

    var p2PRoom: P2PRoom? = null
    var localP2PRoomMember: LocalRoomMember? = null

    fun joinTask() {
        runBlocking {
            for (i in 0..JOIN_RETRY_COUNT) {
                p2PRoom = P2PRoom.find(null, params.taskRoom)
                if (p2PRoom == null) {
                    return@runBlocking
                }

                val metadata = Util.getClientMetadata()
                val memberInit = RoomMember.Init(UUID.randomUUID().toString(), metadata)

                localP2PRoomMember = p2PRoom?.join(memberInit)
                if (localP2PRoomMember != null) {
                    break
                }
            }
            delay(JOIN_RETRY_WAIT_TIME)
        }
    }

    fun subscribe(
        it: RoomPublication,
        options: RoomSubscription.Options = RoomSubscription.Options()
    ): RoomSubscription? {
        if (it.publisher?.id != localP2PRoomMember?.id) {
            return runBlocking {
                return@runBlocking localP2PRoomMember?.subscribe(it.id, options)
            }
        }
        return null
    }

    fun getDataTaskStream(): LocalDataStream {
        val dataSource = DataSource()
        val localDataStream = dataSource.createStream()
        val message = JSONObject()
        message.put("message", "message")
        job = GlobalScope.launch {
            while (true) {
                if (isClose) return@launch
                localDataStream.write("skyway_object:$message")
                delay(500)
            }
        }
        return localDataStream
    }

    override fun closeTask() {
        super.closeTask()
        p2PRoom?.dispose()
    }
}
