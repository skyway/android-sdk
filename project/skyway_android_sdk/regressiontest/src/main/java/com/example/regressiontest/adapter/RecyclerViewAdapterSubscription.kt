package com.example.regressiontest.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.regressiontest.R
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ntt.skyway.core.SkyWayOptIn
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import com.ntt.skyway.room.RoomSubscription


@OptIn(SkyWayOptIn::class)
class RecyclerViewAdapterSubscription :
    RecyclerView.Adapter<RecyclerViewAdapterSubscription.ViewHolder>() {
    val TAG = this.javaClass.simpleName
    private var subscriptions = mutableListOf<RoomSubscription>()
    private var mainHandlers = mutableListOf<Handler>()

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_surface_view_renderer, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = subscriptions[position]

        holder.textViewPublisherId.text = "publisher_id:" + item.publication.publisher?.id
        holder.textViewClientMetadata.text = Gson().fromJson(
            item.publication.publisher?.metadata,
            JsonObject::class.java
        )["info"].asString

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            var preBytesReceived = 0

            @SuppressLint("SetTextI18n")
            override fun run() {
                val codec =
                    item.getStats()?.reports?.filter { it.id.contains("RTCCodec") || it.id.contains("CIT01") }
                if (codec != null && codec.isNotEmpty()) {
                    holder.textViewCodec.text = "codec: " + codec[0].params
                }
                val rtp =
                    item.getStats()?.reports?.filter { it.id.contains("InboundRTPVideo") || it.type == "inbound-rtp" }
                if (rtp != null && rtp.isNotEmpty()) {
                    val bytesReceived = rtp[0].params["bytesReceived"].toString().toInt()
                    val bitrate = (bytesReceived - preBytesReceived) * 8
                    holder.textViewBitrate.text = "bitrate: " + (bitrate / 1000).toString() + "kbps"
                    preBytesReceived = bytesReceived
                }
                handler.postDelayed(this, 1000)
            }
        })
        mainHandlers.add(handler)

        if (item.contentType == Stream.ContentType.VIDEO) {
            holder.surfaceViewRemoteRenderer.setup()
            if (item.stream != null) {
                (item.stream as RemoteVideoStream).addRenderer(holder.surfaceViewRemoteRenderer)
            } else {
                Log.e(TAG, " RemoteVideoStream is NULL")
            }

        }

    }

    override fun getItemCount(): Int {
        return subscriptions.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewPublisherId: TextView = itemView.findViewById(R.id.tv_remote_member_id)
        val textViewClientMetadata: TextView = itemView.findViewById(R.id.tv_client_metadata)
        val textViewCodec: TextView = itemView.findViewById(R.id.tv_codec)
        val textViewBitrate: TextView = itemView.findViewById(R.id.tv_bitrate)
        val surfaceViewRemoteRenderer: SurfaceViewRenderer =
            itemView.findViewById(R.id.remote_renderer)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: RoomSubscription) {
        subscriptions.add(newData)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        for (it in subscriptions) {
            if (it.contentType == Stream.ContentType.VIDEO) {
                if (it.stream != null) {
                    (it.stream as RemoteVideoStream).removeAllRenderer()
                }
            }
        }
        for (it in mainHandlers) {
            it.removeCallbacksAndMessages(null)
        }
        mainHandlers.clear()
        subscriptions.clear()
        notifyDataSetChanged()
    }
}
