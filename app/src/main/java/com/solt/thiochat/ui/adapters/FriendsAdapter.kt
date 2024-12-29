package com.solt.thiochat.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.solt.thiochat.R
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.databinding.FriendLayoutBinding
import com.solt.thiochat.databinding.FriendsPageBinding
val friendDiffUtil = object:DiffUtil.ItemCallback<FriendModel>(){
    override fun areItemsTheSame(oldItem: FriendModel, newItem: FriendModel): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: FriendModel, newItem: FriendModel): Boolean {
        return  oldItem == newItem
    }

}


class FriendsAdapter(val onClick:(FriendModel)->Unit):ListAdapter<FriendModel,FriendsAdapter.FriendViewHolder>(friendDiffUtil) {
    class FriendViewHolder(val binding: FriendLayoutBinding):ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = FriendLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = getItem(position)
        holder.binding.apply {
            friendName.text = friend.userName
            root.setOnClickListener {
                onClick(friend)
            }
        }
    }
}