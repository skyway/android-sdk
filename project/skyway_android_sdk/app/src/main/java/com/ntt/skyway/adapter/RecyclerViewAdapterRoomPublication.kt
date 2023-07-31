package com.ntt.skyway.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ntt.skyway.R
import com.ntt.skyway.core.content.Stream
import com.ntt.skyway.listener.RoomPublicationAdapterListener
import com.ntt.skyway.room.RoomPublication
import com.ntt.skyway.room.member.LocalRoomMember
import com.ntt.skyway.room.member.RoomMember
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RecyclerViewAdapterRoomPublication(private val activity: AppCompatActivity, private var listener: RoomPublicationAdapterListener) :
    RecyclerView.Adapter<RecyclerViewAdapterRoomPublication.ViewHolder>() {
    private var members = mutableListOf<RoomPublication>()
    private var publisher: RoomMember? = null

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_room_publication, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    @DelicateCoroutinesApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = members[position]
        holder.textViewUserId.text = item.id
        holder.textViewPublisherName.text = item.publisher?.name
        holder.textViewPublicationType.text = item.contentType.toString()

        holder.buttonUnPublish.setOnClickListener {
            GlobalScope.launch {
                listener.onUnPublishClick(item.id)
            }
        }

        holder.buttonUpdateEncoding.setOnClickListener {
            GlobalScope.launch {
                listener.onUpdateEncodingClick(item.id)
            }
        }

        holder.buttonChangeEncoding.setOnClickListener {
            GlobalScope.launch {
                listener.onChangeEncodingClick(item.id)
            }
        }

        holder.buttonSendData.setOnClickListener {
            GlobalScope.launch {
                listener.onSendDataClick(item.id)
            }
        }

        holder.buttonUpdateEncoding.setOnClickListener {
            GlobalScope.launch {
                listener.onUpdateEncodingClick(item.id)
            }
        }

        holder.buttonChangeEncoding.setOnClickListener {
            GlobalScope.launch {
                listener.onChangeEncodingClick(item.id)
            }
        }

        holder.buttonSendData.setOnClickListener {
            GlobalScope.launch {
                listener.onSendDataClick(item.id)
            }
        }

        holder.buttonReplaceStream.setOnClickListener {
            GlobalScope.launch {
                listener.onReplaceStreamClick(item.id)
            }
        }

        holder.buttonSubscribe.setOnClickListener {
            GlobalScope.launch {
                if (listener.onSubscribeClick(item.id)) {
                    activity.runOnUiThread {
                        holder.buttonSubscribe.visibility = View.GONE
                        holder.buttonUnSubscribe.visibility = View.VISIBLE
                    }
                }
            }
        }

        holder.buttonUnSubscribe.setOnClickListener {
            GlobalScope.launch {
                if (listener.onUnSubscribeClick(item.id)) {
                    activity.runOnUiThread {
                        holder.buttonSubscribe.visibility = View.VISIBLE
                        holder.buttonUnSubscribe.visibility = View.GONE
                    }
                }
            }
        }
        holder.buttonEnable.setOnClickListener {
            GlobalScope.launch {
                if (listener.onEnableClick(item.id)) {
                    activity.runOnUiThread {
                        holder.buttonEnable.visibility = View.GONE
                        holder.buttonDisable.visibility = View.VISIBLE
                    }
                }
            }
        }

        holder.buttonDisable.setOnClickListener {
            GlobalScope.launch {
                if (listener.onDisableClick(item.id)) {
                    activity.runOnUiThread {
                        holder.buttonEnable.visibility = View.VISIBLE
                        holder.buttonDisable.visibility = View.GONE
                    }
                }
            }
        }

        if (item.publisher?.id == publisher?.id ) {
            holder.buttonEnable.visibility = View.GONE
            holder.buttonDisable.visibility = View.VISIBLE
            holder.buttonUnPublish.visibility = View.VISIBLE
            holder.buttonSubscribe.visibility = View.GONE
            holder.buttonUnSubscribe.visibility = View.GONE
            holder.buttonChangeEncoding.visibility = View.GONE
            when (item.contentType) {
                Stream.ContentType.VIDEO -> {
                    holder.buttonUpdateEncoding.visibility = View.VISIBLE
                    holder.buttonSendData.visibility = View.GONE
                    holder.buttonReplaceStream.visibility = View.VISIBLE
                }
                Stream.ContentType.AUDIO -> {
                    holder.buttonUpdateEncoding.visibility = View.GONE
                    holder.buttonSendData.visibility = View.GONE
                    holder.buttonReplaceStream.visibility = View.GONE
                }
                Stream.ContentType.DATA -> {
                    holder.buttonUpdateEncoding.visibility = View.GONE
                    holder.buttonSendData.visibility = View.VISIBLE
                    holder.buttonReplaceStream.visibility = View.GONE
                }
            }
        } else {
            if(publisher == null){
                holder.buttonDisable.visibility = View.VISIBLE
                holder.buttonUnPublish.visibility = View.VISIBLE
            }
            else{
                holder.buttonDisable.visibility = View.GONE
                holder.buttonUnPublish.visibility = View.GONE
            }
            holder.buttonEnable.visibility = View.GONE
            holder.buttonChangeEncoding.visibility = View.VISIBLE
            holder.buttonUpdateEncoding.visibility = View.GONE
            holder.buttonSubscribe.visibility = View.VISIBLE
            holder.buttonUnSubscribe.visibility = View.GONE
            holder.buttonSendData.visibility = View.GONE
            holder.buttonReplaceStream.visibility = View.GONE
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
        val buttonUnPublish: Button = itemView.findViewById(R.id.btn_unpublish)
        val buttonUpdateEncoding: Button = itemView.findViewById(R.id.btn_update_encoding)
        val buttonChangeEncoding: Button = itemView.findViewById(R.id.btn_change_encoding)
        val buttonSendData: Button = itemView.findViewById(R.id.btn_send_data)
        val buttonReplaceStream: Button = itemView.findViewById(R.id.btn_replace_stream)
        val buttonSubscribe: Button = itemView.findViewById(R.id.btn_subscribe)
        val buttonUnSubscribe: Button = itemView.findViewById(R.id.btn_unsubscribe)
        val buttonEnable: Button = itemView.findViewById(R.id.btn_enable)
        val buttonDisable: Button = itemView.findViewById(R.id.btn_disable)
    }

    fun setData(newData: MutableList<RoomPublication>,localRoomMember: LocalRoomMember?) {
        members.clear()
        members.addAll(newData)
        publisher = localRoomMember
        notifyDataSetChanged()
    }
}
