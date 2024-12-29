package com.solt.thiochat.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.solt.thiochat.data.Friends.Requests.FriendRequestDisplayModel
import com.solt.thiochat.data.Groups.Request.GroupRequestDisplayModel
import com.solt.thiochat.databinding.RequestItemLayoutBinding

val friendRequestDiffUtil = object : DiffUtil.ItemCallback<FriendRequestDisplayModel>() {
    override fun areItemsTheSame(
        oldItem: FriendRequestDisplayModel,
        newItem: FriendRequestDisplayModel
    ): Boolean {
        return oldItem.documentId == newItem.documentId
    }

    override fun areContentsTheSame(
        oldItem: FriendRequestDisplayModel,
        newItem: FriendRequestDisplayModel
    ): Boolean {
        return oldItem == newItem
    }

}
class FriendRequestAdapter(val onAccept:(FriendRequestDisplayModel)->Unit, val onReject:(FriendRequestDisplayModel)->Unit):ListAdapter<FriendRequestDisplayModel,FriendRequestAdapter.FriendRequestViewHolder> (
    friendRequestDiffUtil){
    //For we will use the same layout as the group requests
    inner class FriendRequestViewHolder(val  binding: RequestItemLayoutBinding):ViewHolder(binding.root){
        fun bind(friendRequest:FriendRequestDisplayModel){
            binding.apply {
                userName.text = friendRequest.user.userName
                acceptButton.setOnClickListener {
                    onAccept(friendRequest)
                }
                rejectButton.setOnClickListener {
                    onReject(friendRequest)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val binding = RequestItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FriendRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
       holder.bind(getItem(position))
    }
}