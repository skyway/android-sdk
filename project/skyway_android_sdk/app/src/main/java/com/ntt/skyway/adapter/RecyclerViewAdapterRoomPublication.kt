package com.ntt.skyway.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ntt.skyway.R
import com.ntt.skyway.core.channel.Publication
import com.ntt.skyway.listener.RoomPublicationAdapterListener
import com.ntt.skyway.manager.RoomManager
import com.ntt.skyway.room.RoomPublication


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

        if (RoomManager.localPerson?.id == item.publisher?.id) {
            holder.buttonSubscribe.visibility = View.GONE
            holder.buttonUnSubscribe.visibility = View.GONE
            if (item.state == Publication.State.ENABLED) {
                holder.buttonEnable.visibility = View.GONE
                holder.buttonDisable.visibility = View.VISIBLE
            } else {
                holder.buttonEnable.visibility = View.VISIBLE
                holder.buttonDisable.visibility = View.GONE
            }
        } else {
            holder.buttonSubscribe.visibility = View.VISIBLE
            holder.buttonEnable.visibility = View.GONE
            holder.buttonDisable.visibility = View.GONE
        }

        holder.buttonSubscribe.setOnClickListener {
            listener.onSubscribeClick(item.id)
            holder.buttonUnSubscribe.visibility = View.VISIBLE
        }

        holder.buttonUnSubscribe.setOnClickListener {
            listener.onUnSubscribeClick()

            holder.buttonSubscribe.visibility = View.VISIBLE
            holder.buttonUnSubscribe.visibility = View.GONE
        }

        holder.buttonEnable.setOnClickListener {
            listener.onEnableClick(item.id)

            holder.buttonEnable.visibility = View.GONE
            holder.buttonDisable.visibility = View.VISIBLE
        }

        holder.buttonDisable.setOnClickListener {
            listener.onDisableClick(item.id)

            holder.buttonDisable.visibility = View.GONE
            holder.buttonEnable.visibility = View.VISIBLE
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
        val buttonSubscribe: Button = itemView.findViewById(R.id.btn_subscribe)
        val buttonUnSubscribe: Button = itemView.findViewById(R.id.btn_unsubscribe)
        val buttonEnable: Button = itemView.findViewById(R.id.btn_enable)
        val buttonDisable: Button = itemView.findViewById(R.id.btn_disable)
    }

    fun setData(newData: MutableList<RoomPublication>) {
        members.clear()
        members.addAll(newData)
        notifyDataSetChanged()
    }
}
