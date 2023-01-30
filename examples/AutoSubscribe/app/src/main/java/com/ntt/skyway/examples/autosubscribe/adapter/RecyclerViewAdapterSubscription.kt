package com.ntt.skyway.examples.autosubscribe.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.core.content.remote.RemoteVideoStream
import com.ntt.skyway.core.content.sink.SurfaceViewRenderer
import com.ntt.skyway.examples.autosubscribe.R
import com.ntt.skyway.room.RoomSubscription


class RecyclerViewAdapterSubscription() : RecyclerView.Adapter<RecyclerViewAdapterSubscription.ViewHolder>() {
    private val TAG: String = this.javaClass.simpleName
    private var subscriptions = mutableListOf<RoomSubscription>()

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_surface_view_renderer, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = subscriptions[position]

        holder.textViewPublisherId.text = item.publication?.publisher?.name

        if(item.contentType == Stream.ContentType.VIDEO) {
            holder.surfaveViewRemote_renderer.setup()
            if(item.stream != null){
                (item.stream as RemoteVideoStream).addRenderer(holder.surfaveViewRemote_renderer)
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
        val surfaveViewRemote_renderer: SurfaceViewRenderer = itemView.findViewById(R.id.remote_renderer_item)
    }

    fun addSubscription(sub: RoomSubscription) {
        subscriptions.add(sub)
        notifyDataSetChanged()
    }

    fun removeSubscription(sub: RoomSubscription) {
        subscriptions.remove(sub)
        notifyDataSetChanged()
    }

    fun setData(newData: MutableList<RoomSubscription>) {
        Log.e(TAG, "SurfaceView addData:")
        subscriptions.clear()
        subscriptions.addAll(newData)
        notifyDataSetChanged()
    }

    fun clearData(){
        subscriptions.clear()
        notifyDataSetChanged()
    }
}
