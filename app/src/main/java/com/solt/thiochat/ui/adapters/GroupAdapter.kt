package com.solt.thiochat.ui.adapters

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.databinding.GroupItemBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

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
class GroupAdapter( val fragment : Fragment, val onGroupClicked:(GroupDisplayModel)->Unit):ListAdapter<GroupDisplayModel,GroupAdapter.GroupViewHolder>(groupDiffUtil) {
    class GroupViewHolder(val binding: GroupItemBinding,val fragment :Fragment,val onGroupClicked: (GroupDisplayModel) -> Unit): RecyclerView.ViewHolder(binding.root){
        //We are creating a Job (Coroutine ) so that we can store the job that will be created when the flow is be collected
        //We dont want to listen to the same flow when the same data source is not bound so we will cancel the former job on bind again
        var flowJob :Job? =null
        fun bind(group:GroupDisplayModel) {
            binding.apply {
                //If there is a former Job cancel it
                flowJob?.cancel()
                groupName.text = group.groupName
                val backGroundDrawable = root.background as GradientDrawable
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
            //Here we will list for the flow Of Messages
            //Then assign the job here
           flowJob =  fragment.viewLifecycleOwner.lifecycleScope.launch {
               //If the latest message is null hide the latest message layout
                group.latestMessages?.collectLatest {
                    if (it == null){
                        binding.latestMessage.visibility = View.GONE
                    }else{
                    binding.latestMesssageContent.text = it.text
                    val time =it.timeStamp
                    val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
                    val dateString =  if (time != null)formatter.format(time) else "No Date"
                    binding.latestMesssageTime.text = dateString
                }}
            }
        }}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return GroupViewHolder(binding,fragment,onGroupClicked)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
       val groupItem = getItem(position)
        holder.bind(groupItem)
        }
    }

