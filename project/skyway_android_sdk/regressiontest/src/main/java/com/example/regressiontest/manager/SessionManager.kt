package com.example.regressiontest.manager

import android.util.Log
import com.example.regressiontest.model.Result
import com.example.regressiontest.model.SessionDataRequest
import com.example.regressiontest.model.SessionDataResponse
import com.example.regressiontest.task.*
import com.google.gson.Gson
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.local.LocalDataStream
import com.ntt.skyway.core.content.local.source.DataSource
import com.ntt.skyway.core.content.remote.RemoteDataStream
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.RoomSubscription
import kotlinx.coroutines.*

class SessionManager (private val sessionListener: Listener, private val controller: RoomManager){
    class Listener{
        var onInitSuccessHandler: (() -> Unit)? = null
        var onInitFailedHandler: ((message:String) -> Unit)? = null
        var onSubscribeHandler: ((subscription:RoomSubscription) -> Unit)? = null
        var onUpdateBitrateHandler: ((subscription:RoomSubscription,bitrate:Int) -> Unit)? = null
        var onNewTaskHandler: ((taskId:String) -> Unit)? = null
        var onCloseTaskHandler: ((taskId:String) -> Unit)? = null
    }

    val TAG = this.javaClass.simpleName

    private val taskListener:TaskBase.Listener = TaskBase.Listener()
    private var tasks: MutableMap<String,TaskBase> = mutableMapOf()
    private var successTasks: MutableMap<String,MutableSet<String>> = mutableMapOf()

    private var localDataStream: LocalDataStream
    private var publicationLocalDataStream: RoomPublication? = null

    init {
        val dataSource = DataSource()
        localDataStream = dataSource.createStream()
    }

    fun subscribeToControllerDataStream(){
        runBlocking {
            publicationLocalDataStream = controller.localPerson?.publish(localDataStream)
            if(publicationLocalDataStream == null){
                sessionListener.onInitFailedHandler?.invoke("Publish Failed")
                return@runBlocking
            }

            val controllerDataStream = getControllerDataStream()
            if (controllerDataStream == null) {
                sessionListener.onInitFailedHandler?.invoke("ControllerStream is not found")
                return@runBlocking
            }

            val subscription = controller.localPerson?.subscribe(controllerDataStream.id)
            if(subscription == null){
                sessionListener.onInitFailedHandler?.invoke("Subscribe Failed")
                return@runBlocking
            }

            taskListener.onTaskSuccessHandler = { memberId:String, requestId: String, taskId: String ->
                // Send a success response only if a client other than yourself succeeds.
                if(successTasks[requestId] == null) {
                    successTasks[requestId] = mutableSetOf()
                }
                successTasks[requestId]!!.add(memberId)

                // Subtract 1, since "client" is a number that includes yourself.
                if(successTasks[requestId]!!.count() == tasks[taskId]?.params?.clients?.minus(1)){
                    val sessionDataRequest = SessionDataRequest(requestId, Result(true))
                    sendResponse(sessionDataRequest)
                }
            }

            taskListener.onTaskFailedHandler = { requestId: String, message: String ->
                val sessionDataRequest = SessionDataRequest(requestId, Result(false, message))
                sendResponse(sessionDataRequest)
            }

            taskListener.onSubscribeHandler = {
                when (it.contentType) {
                    Stream.ContentType.VIDEO -> {
                        sessionListener.onSubscribeHandler?.invoke(it)
                    }
                    Stream.ContentType.AUDIO -> {}
                    Stream.ContentType.DATA -> {}
                }
            }

            taskListener.onUpdateBitrateHandler = { sub:RoomSubscription, bitrate:Int ->
                sessionListener.onUpdateBitrateHandler?.invoke(sub,bitrate)
            }

            (subscription.stream as RemoteDataStream).onDataHandler = {
                val str = it.replace("skyway_object:", "")
                val dataResponse = Gson().fromJson(str, SessionDataResponse::class.java)
                if (dataResponse.command == "checkBitrate") {
                    tasks[dataResponse.taskId]?.newPhase(dataResponse.requestId)
                }
                else if (dataResponse.command == "closeTask") {
                    Log.i(TAG, "close task : $dataResponse.taskId")
                    closeTask(dataResponse.taskId)
                }
                else {
                    successTasks[dataResponse.requestId]?.clear()
                    tasks[dataResponse.taskId]?.let {
                        Log.i(TAG, "close task : $dataResponse.taskId")
                        closeTask(dataResponse.taskId)
                    }
                    val task = initializeTask(
                        dataResponse.requestId,
                        dataResponse.taskId,
                        dataResponse.payload!!.taskRoom,
                        dataResponse.payload!!.clients
                    )
                    if(task != null) {
                        tasks[dataResponse.taskId] = task
                        Log.i(TAG, "start task : $dataResponse.taskId")
                        tasks[dataResponse.taskId]?.run()
                    }
                }
            }

            sessionListener.onInitSuccessHandler?.invoke()
        }
    }

    private fun initializeTask(requestId: String, taskId: String, taskRoom: String, clients: Int): TaskBase?{
        sessionListener.onNewTaskHandler?.invoke(taskId)
        val params = TaskBase.Params(requestId, taskId, taskRoom, clients)

        when(taskId){
            "P2PRoomDataAutoTask" -> {
                return P2PRoomDataAutoTask(taskListener,params)
            }
            "P2PRoomTurnAutoTask" -> {
                return P2PRoomTurnAutoTask(taskListener,params)
            }
            "P2PRoomEncodingAutoTask" -> {
                return P2PRoomEncodingAutoTask(taskListener,params)
            }
            "SfuRoomEncodingAutoTask" -> {
                return SFURoomEncodingAutoTask(taskListener,params)
            }
            "P2PRoomAudioVideoManualTask" -> {
                return P2PRoomAudioVideoManualTask(taskListener,params)
            }
            "SFURoomAudioVideoManualTask" -> {
                return SFURoomAudioVideoManualTask(taskListener,params)
            }
            "SfuRoomSimulcastAutoTask" -> {
                return SFURoomSimulcastAutoTask(taskListener,params)
            }

            //optional task
            "P2PRoomCodecVP8ManualTask" -> {
                return P2PRoomCodecVP8ManualTask(taskListener,params)
            }
            "SfuRoomCodecVP8ManualTask" -> {
                return SFURoomCodecVP8ManualTask(taskListener,params)
            }
            "P2PRoomCodecH264ManualTask" -> {
                return P2PRoomCodecH264ManualTask(taskListener,params)
            }
            "SfuRoomCodecH264ManualTask" -> {
                return SFURoomCodecH264ManualTask(taskListener,params)
            }
            "P2PRoomCodecRedManualTask" -> {
                return P2PRoomCodecRedManualTask(taskListener,params)
            }
            "P2PRoomReplaceStreamAutoTask" -> {
                return P2PRoomReplaceStreamAutoTask(taskListener,params)
            }
        }
        return null
    }

    private fun closeTask(taskId: String) {
        tasks[taskId]?.closeTask()
        tasks.remove(taskId)
        sessionListener.onCloseTaskHandler?.invoke(taskId)
    }

    private fun getControllerDataStream(): RoomPublication? {
        val controllerDataStream: RoomPublication? = controller.room?.publications?.find {
            it.publisher?.metadata?.contains("controller") == true
        }
        return controllerDataStream
    }

    private fun sendResponse(data: SessionDataRequest) {
        runBlocking {
            val json = Gson().toJson(data)
            localDataStream.write("skyway_object:$json")
        }
    }
}
