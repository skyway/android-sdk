package com.ntt.skyway.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ntt.skyway.R
import com.ntt.skyway.core.channel.member.Member


class RecyclerViewAdapterMember : RecyclerView.Adapter<RecyclerViewAdapterMember.ViewHolder>() {

    private var members = mutableListOf<Member>()

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_user, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = members[position]
        holder.textViewUserId.text =
            if (itemsViewModel.name != "") itemsViewModel.name else itemsViewModel.subType
    }

    override fun getItemCount(): Int {
        return members.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textViewUserId: TextView = itemView.findViewById(R.id.tv_user_id)
    }


    fun setData(newData: MutableList<Member>) {
        members.clear()
        members.addAll(newData)
        notifyDataSetChanged()
    }
}