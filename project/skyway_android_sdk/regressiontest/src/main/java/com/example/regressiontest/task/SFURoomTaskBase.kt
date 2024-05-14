package com.example.regressiontest.task

import com.example.regressiontest.util.Util
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import com.ntt.skyway.room.sfu.SFURoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.UUID

abstract class SFURoomTaskBase(listener: Listener, params: Params) :
    TaskBase(listener, params) {

    override val TAG = this.javaClass.simpleName
    val JOIN_RETRY_COUNT = 5
    val JOIN_RETRY_WAIT_TIME = 1000L

    var sfuRoom: SFURoom? = null
    var localSFURoomMember: LocalRoomMember? = null

    fun joinTask() {
        runBlocking {
            for (i in 0..JOIN_RETRY_COUNT) {
                sfuRoom = SFURoom.find(null, params.taskRoom)
                if (sfuRoom == null) {
                    return@runBlocking
                }

                val metadata = Util.getClientMetadata()
                val memberInit = RoomMember.Init(UUID.randomUUID().toString(), metadata)

                localSFURoomMember = sfuRoom?.join(memberInit)
                if (localSFURoomMember != null) {
                    break
                }
            }
            delay(JOIN_RETRY_WAIT_TIME)
        }
    }

    fun subscribe(
        it: RoomPublication,
        options: RoomSubscription.Options = RoomSubscription.Options()
    ) {
        if (it.publisher?.id != localSFURoomMember?.id) {
            runBlocking {
                localSFURoomMember?.subscribe(it.id, options)
            }
        }
    }

    override fun closeTask() {
        super.closeTask()
        sfuRoom?.dispose()
    }
}
