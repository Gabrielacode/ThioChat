package com.solt.thiochat.ui.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.databinding.ExploreGroupItemBinding

class ExploreAdapter(val onJoinButtonClick:(GroupDisplayModel)->Unit) : ListAdapter<GroupDisplayModel,ExploreGroupViewHolder > (groupDiffUtil){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreGroupViewHolder {
        val binding = ExploreGroupItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ExploreGroupViewHolder(binding,onJoinButtonClick)
    }

    override fun onBindViewHolder(holder: ExploreGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
class ExploreGroupViewHolder(val binding:ExploreGroupItemBinding, val onJoinButtonClick: (GroupDisplayModel) -> Unit):ViewHolder(binding.root){
    fun bind(group:GroupDisplayModel) {
        binding.apply {
            groupName.text = group.groupName

            val backGroundDrawable = root.background as GradientDrawable
            val groupColorHex =   try {
                Color.parseColor("#${group.groupColour}")}catch (e:IllegalArgumentException){
                Color.CYAN}
            backGroundDrawable.setColor(groupColorHex)
            if (groupColorHex in (android.graphics.Color.BLACK..android.graphics.Color.DKGRAY)) {
                groupName.setTextColor(android.graphics.Color.WHITE)
            } else groupName.setTextColor(android.graphics.Color.BLACK)

            addGroup.setOnClickListener {
                onJoinButtonClick(group)
            }
        }
    }
}