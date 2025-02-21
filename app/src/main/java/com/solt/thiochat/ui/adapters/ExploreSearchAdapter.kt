package com.solt.thiochat.ui.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.databinding.ExploreGroupItemBinding
import com.solt.thiochat.databinding.GroupItemBinding

const val USERGROUP = 1
const val NOTUSERGROUP = 2
class ExploreSearchAdapter(val fragment : Fragment, val onJoinButtonClick:(GroupDisplayModel)->Unit, val onGroupClicked:(GroupDisplayModel, View)->Unit) : ListAdapter<GroupDisplayModel, ViewHolder>(groupDiffUtil){
    //Here we will show groups based on whether the user is in them or not
    override fun getItemViewType(position: Int): Int {
       val group = getItem(position)
       return if (group is GroupDisplayModel.UserInGroup) USERGROUP
        else NOTUSERGROUP
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      if (viewType == USERGROUP) {
          val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
          binding.latestMessage.visibility = View.GONE
          return GroupAdapter.GroupViewHolder(binding,fragment,onGroupClicked)
      }else {
          val binding = ExploreGroupItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
          return  ExploreGroupViewHolder(binding,onJoinButtonClick)
      }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder){
            is ExploreGroupViewHolder -> holder.bind(getItem(position))
            is GroupAdapter.GroupViewHolder ->  holder.bind(getItem(position))
        }
    }


}