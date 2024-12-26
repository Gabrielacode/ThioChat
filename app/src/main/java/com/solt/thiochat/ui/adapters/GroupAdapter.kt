package com.solt.thiochat.ui.adapters

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.collection.LLRBNode.Color
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
       return oldItem == newItem
    }

}
class GroupAdapter( val onGroupClicked:(GroupDisplayModel)->Unit):ListAdapter<GroupDisplayModel,GroupAdapter.GroupViewHolder>(groupDiffUtil) {
    class GroupViewHolder(val binding: GroupItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {

       val groupItem = getItem(position)
        holder.binding.apply {
            groupName.text = groupItem.groupName
           val backGroundDrawable =  root.background as? GradientDrawable
            val groupColorHex =  try{groupItem.groupColour.toColorInt()}catch (e:IllegalArgumentException){android.graphics.Color.BLUE}
             backGroundDrawable?.setColor(groupColorHex)
            if (groupColorHex in (android.graphics.Color.BLACK .. android.graphics.Color.DKGRAY)){
                groupName.setTextColor(android.graphics.Color.WHITE)
            }else groupName.setTextColor(android.graphics.Color.BLACK)
            root.setOnClickListener {
               onGroupClicked(groupItem)
            }
        }
    }

}