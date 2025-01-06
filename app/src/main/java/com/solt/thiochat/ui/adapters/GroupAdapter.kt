package com.solt.thiochat.ui.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.databinding.GroupItemBinding

val groupDiffUtil = object :DiffUtil.ItemCallback<GroupDisplayModel>(){
    override fun areItemsTheSame(oldItem: GroupDisplayModel, newItem: GroupDisplayModel): Boolean {
       return oldItem.documentId == newItem.documentId
    }

    override fun areContentsTheSame(
        oldItem: GroupDisplayModel,
        newItem: GroupDisplayModel
    ): Boolean {
        return newItem.groupName == oldItem.groupName && newItem.groupColour == oldItem.groupColour && newItem.modeOfAcceptance == oldItem.modeOfAcceptance && newItem.documentId == oldItem.documentId
    }

}
class GroupAdapter( val onGroupClicked:(GroupDisplayModel)->Unit):ListAdapter<GroupDisplayModel,GroupAdapter.GroupViewHolder>(groupDiffUtil) {
    class GroupViewHolder(val binding: GroupItemBinding,val onGroupClicked: (GroupDisplayModel) -> Unit): RecyclerView.ViewHolder(binding.root){
        fun bind(group:GroupDisplayModel) {
            binding.apply {
                groupName.text = group.groupName
                val backGroundDrawable = groupName.background as GradientDrawable
                //We need to attach the hash symbol since it it needs
                val groupColorHex = try {
                    Color.parseColor("#${group.groupColour}")
                } catch (e: IllegalArgumentException) {
                    Color.CYAN
                }
                backGroundDrawable.setColor(groupColorHex)
                //
                root.setOnClickListener {
                    onGroupClicked(group)
                }
            }
        }}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return GroupViewHolder(binding,onGroupClicked)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
       val groupItem = getItem(position)
        holder.bind(groupItem)
        }
    }

