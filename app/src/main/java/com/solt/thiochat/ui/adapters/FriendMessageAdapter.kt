package com.solt.thiochat.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.solt.thiochat.data.Friends.Messages.FriendMessageModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.databinding.NonUserMessageLayoutBinding
import com.solt.thiochat.databinding.UserMessageLayoutBinding
import java.text.SimpleDateFormat

val friendMessageDiffUtil = object : DiffUtil.ItemCallback<FriendMessageModel> (){
    override fun areItemsTheSame(
        oldItem: FriendMessageModel,
        newItem: FriendMessageModel
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: FriendMessageModel,
        newItem: FriendMessageModel
    ): Boolean {
        return newItem.userId == oldItem.userId && newItem.userName == oldItem.userName && newItem.text == oldItem.text && newItem.timeStamp == oldItem.timeStamp
    }

}
const val FRIEND = 11
    //We will just reuse the messages viewholder as the main super class
class FriendMessageAdapter():ListAdapter<FriendMessageModel,FriendMessagesViewHolder>(friendMessageDiffUtil) {

        override fun getItemViewType(position: Int): Int {
            val item = getItem(position)
          return  if (item is FriendMessageModel.UserMessage) USER else FRIEND
        }
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FriendMessagesViewHolder {
            return if(viewType == USER){
                val binding = UserMessageLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                UserToFriendMessageViewHolder(binding)
            }else {
                val binding = NonUserMessageLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                FriendToUserMessageViewHolder( binding)
            }
        }

        override fun onBindViewHolder(holder: FriendMessagesViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
open class FriendMessagesViewHolder(val view: View): RecyclerView.ViewHolder(view){
    open fun bind(message:FriendMessageModel){}
}
    class UserToFriendMessageViewHolder(val binding: UserMessageLayoutBinding):FriendMessagesViewHolder(binding.root){
        override fun bind(message: FriendMessageModel) {
         binding.apply {
             userName.text = message.userName
             messageText.text = message.text
             val time = message.timeStamp
             val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG)
             val dateString =  if (time != null)formatter.format(time) else "No Date"
             timeSent.text = dateString
         }
        }
    }
    class FriendToUserMessageViewHolder(val binding: NonUserMessageLayoutBinding):FriendMessagesViewHolder(binding.root){
        override fun bind(message: FriendMessageModel) {
            binding.apply {
                userName.text = message.userName
                messageText.text = message.text
                val time = message.timeStamp
                val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG)
                val dateString =  if (time != null)formatter.format(time) else "No Date"
                timeSent.text = dateString
            }
        }
    }

