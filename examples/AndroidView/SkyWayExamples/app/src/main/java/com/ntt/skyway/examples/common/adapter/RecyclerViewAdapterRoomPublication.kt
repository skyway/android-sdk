package com.ntt.skyway.examples.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ntt.skyway.examples.R
import com.ntt.skyway.examples.common.listener.RoomPublicationAdapterListener
import com.ntt.skyway.examples.common.manager.RoomManager
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.p2p.LocalP2PRoomMember
import com.ntt.skyway.room.sfu.LocalSFURoomMember


class RecyclerViewAdapterRoomPublication(var listener: RoomPublicationAdapterListener) :
    RecyclerView.Adapter<RecyclerViewAdapterRoomPublication.ViewHolder>() {
    val TAG = this.javaClass.simpleName
    var members = mutableListOf<RoomPublication>()

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_room_publication, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = members[position]
        holder.textViewUserId.text = item.id
        holder.textViewPublisherName.text = item.publisher?.name
        holder.textViewPublicationType.text = item.contentType.toString()

        holder.buttonUnSubscribe.visibility = View.GONE
        holder.buttonUnPublish.visibility = View.GONE
        holder.buttonSFUChangeEncoding.visibility = View.GONE
        if (RoomManager.localPerson == null) {
            return
        }
        if(RoomManager.localPerson is LocalP2PRoomMember) {
            if (RoomManager.localPerson?.id == item.publisher?.id) {
                holder.buttonUnPublish.visibility = View.VISIBLE
            } else {
                holder.buttonUnSubscribe.visibility = View.VISIBLE
                holder.buttonUnPublish.visibility = View.GONE
            }
        } else if(RoomManager.localPerson is LocalSFURoomMember) {
            holder.buttonSFUChangeEncoding.visibility = View.VISIBLE
            if (RoomManager.localPerson?.id == item.publisher?.id) {
                holder.buttonUnPublish.visibility = View.VISIBLE
                holder.buttonSFUChangeEncoding.visibility = View.GONE
            } else {
                holder.buttonUnSubscribe.visibility = View.VISIBLE
                holder.buttonUnPublish.visibility = View.GONE
            }
        }
        holder.buttonUnSubscribe.setOnClickListener {
            listener.onUnSubscribeClick()
            holder.buttonUnSubscribe.visibility = View.GONE
        }

        holder.buttonUnPublish.setOnClickListener {
            listener.onUnPublishClick(item)
        }

        holder.buttonSFUChangeEncoding.setOnClickListener {
            var subscription = RoomManager.localPerson?.subscriptions?.find { it.publication?.id == item.id }
            subscription?.let { it1 -> listener.onSFUChangeEncodingClick(it1) }
        }
    }

    override fun getItemCount(): Int {
        return members.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewUserId: TextView = itemView.findViewById(R.id.tv_publication_id)
        val textViewPublisherName: TextView = itemView.findViewById(R.id.tv_publisher_name)
        val textViewPublicationType: TextView = itemView.findViewById(R.id.tv_publication_type)
        val buttonUnSubscribe: Button = itemView.findViewById(R.id.btn_unsubscribe)
        val buttonUnPublish: Button = itemView.findViewById(R.id.btn_un_publish)
        val buttonSFUChangeEncoding: Button = itemView.findViewById(R.id.btn_change_encoding)
    }

    fun setData(newData: MutableList<RoomPublication>) {
        members.clear()
        members.addAll(newData)
        notifyDataSetChanged()
    }
}
