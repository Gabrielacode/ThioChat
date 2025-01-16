package com.solt.thiochat.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.solt.thiochat.R
import com.solt.thiochat.data.Friends.FriendDisplayModel
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.databinding.FriendLayoutBinding
import com.solt.thiochat.databinding.FriendsPageBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

val friendDiffUtil = object:DiffUtil.ItemCallback<FriendDisplayModel>(){

    override fun areItemsTheSame(
        oldItem: FriendDisplayModel,
        newItem: FriendDisplayModel
    ): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(
        oldItem: FriendDisplayModel,
        newItem: FriendDisplayModel
    ): Boolean {
        return  oldItem == newItem
    }

}


class FriendsAdapter(val fragment: Fragment,val onClick:(FriendDisplayModel)->Unit):ListAdapter<FriendDisplayModel,FriendsAdapter.FriendViewHolder>(friendDiffUtil) {
   inner class FriendViewHolder(val binding: FriendLayoutBinding,val fragment :Fragment):ViewHolder(binding.root){
       var flowJob : Job? = null
       fun bind (friend :FriendDisplayModel){
           //We will do the same for the group adapter
           flowJob?.cancel()
          binding.apply {
               friendName.text = friend.userName
               root.setOnClickListener {
                   onClick(friend)
               }

           }
            flowJob = fragment.viewLifecycleOwner.lifecycleScope.launch {
                friend.latestMessage?.collectLatest {
                    binding.latestMessage.text = it?.text?:""
                    val time =it?.timeStamp
                    val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
                    val dateString =  if (time != null)formatter.format(time) else "No Date"
                    binding.timeSent.text = dateString

                }
            }
       }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = FriendLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FriendViewHolder(binding,fragment)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = getItem(position)
       holder.bind(friend)

    }
}