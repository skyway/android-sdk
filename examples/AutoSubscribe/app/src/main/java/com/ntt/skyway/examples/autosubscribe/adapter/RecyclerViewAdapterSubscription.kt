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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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

        if (item.contentType == Stream.ContentType.VIDEO) {
            holder.surfaveViewRemote_renderer.setup()
            CoroutineScope(Dispatchers.Main).launch {
                bindStreamWithRetry(holder, item, 15)
            }
        }
    }

    private suspend fun bindStreamWithRetry(holder: ViewHolder, item: RoomSubscription, maxRetries: Int, currentAttempt: Int = 1) {
        if (item.stream != null) {
            (item.stream as RemoteVideoStream).addRenderer(holder.surfaveViewRemote_renderer)
        } else if (currentAttempt <= maxRetries) {
            Log.w(TAG, "RemoteVideoStream is NULL, retrying... Attempt: $currentAttempt/$maxRetries")
            delay(1000)
            bindStreamWithRetry(holder, item, maxRetries, currentAttempt + 1)
        } else {
            Log.e(TAG, "RemoteVideoStream is still NULL after $maxRetries attempts")
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

    private fun addSubscription(sub: RoomSubscription) {
        subscriptions.add(sub)
        notifyItemInserted(subscriptions.size - 1)
    }

    private fun removeSubscription(sub: RoomSubscription) {
        val index = subscriptions.indexOf(sub)
        if (index != -1) {
            subscriptions.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    fun setData(newSubscriptions: List<RoomSubscription>) {
        val removedSubscriptions = subscriptions.filter { it !in newSubscriptions }
        val addedSubscriptions = newSubscriptions.filter { it !in subscriptions }

        removedSubscriptions.forEach { removeSubscription(it) }
        addedSubscriptions.forEach { addSubscription(it) }
    }

    fun clearData(){
        subscriptions.clear()
        notifyDataSetChanged()
    }
}
