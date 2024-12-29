package com.solt.thiochat.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.solt.thiochat.data.Groups.Request.GroupRequestDisplayModel
import com.solt.thiochat.data.Groups.Request.GroupRequestModel
import com.solt.thiochat.databinding.GroupRequestsLayoutBinding
import com.solt.thiochat.databinding.RequestItemLayoutBinding

val groupRequestDiffUtil = object :DiffUtil.ItemCallback<GroupRequestDisplayModel>(){

    override fun areItemsTheSame(
        oldItem: GroupRequestDisplayModel,
        newItem: GroupRequestDisplayModel
    ): Boolean {
        return oldItem.documentId == newItem.documentId
    }

    override fun areContentsTheSame(
        oldItem: GroupRequestDisplayModel,
        newItem: GroupRequestDisplayModel
    ): Boolean {
        return oldItem == newItem
    }

}
class GroupRequestsAdapter(val onAccept:(GroupRequestDisplayModel)->Unit,val onReject:(GroupRequestDisplayModel)->Unit):ListAdapter<GroupRequestDisplayModel,GroupRequestsAdapter.GroupRequestViewHolder>(
    groupRequestDiffUtil) {
    inner class GroupRequestViewHolder(val binding: RequestItemLayoutBinding):ViewHolder(binding.root){
        fun bind(requestModel: GroupRequestDisplayModel){
            binding.apply {
                userName.text = requestModel.userModel.userName
                acceptButton.setOnClickListener {
                    onAccept(requestModel)
                }
                rejectButton.setOnClickListener {
                    onReject(requestModel)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupRequestViewHolder {
        val binding = RequestItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return GroupRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupRequestViewHolder, position: Int) {
       holder.bind(getItem(position))
    }
}