package com.solt.thiochat.ui.adapters


import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.databinding.NonUserMessageLayoutBinding
import com.solt.thiochat.databinding.UserMessageLayoutBinding
import java.text.SimpleDateFormat
const val USER = 1
const val NON_USER = 2


val groupMessageDiffUtil = object : DiffUtil.ItemCallback<GroupMessageModel>() {
    override fun areItemsTheSame(oldItem: GroupMessageModel, newItem: GroupMessageModel): Boolean {
        return  oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: GroupMessageModel,
        newItem: GroupMessageModel
    ): Boolean {
        return newItem.userId == oldItem.userId && newItem.userName == oldItem.userName && newItem.text == oldItem.text && newItem.timeStamp == oldItem.timeStamp
    }

}
class GroupMessagesAdapter:ListAdapter<GroupMessageModel,MessageViewHolder>(groupMessageDiffUtil) {
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
       return if (item is GroupMessageModel.UserGroupMessage) USER else NON_USER

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
      return if(viewType == USER){
          val binding = UserMessageLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
           UserMessageViewHolder(binding)
      }else {
          val binding = NonUserMessageLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
          NonUserMessageViewHolder(binding)
      }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

}
 open class MessageViewHolder(val view:View):ViewHolder(view){
   open fun bind(message:GroupMessageModel){}
}
class UserMessageViewHolder(val binding: UserMessageLayoutBinding):MessageViewHolder(binding.root){
    override fun bind(message: GroupMessageModel) {
        binding.apply {
            userName.text = message.userName
            //We need to format the date to show the year the month and maybe the day and hour and second
            val time = message.timeStamp
            val formatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG)
            val dateString =  if (time != null)formatter.format(time) else "No Date"
            timeSent.text = dateString
            messageText.text = message.text

        }
    }
}
class NonUserMessageViewHolder(val binding: NonUserMessageLayoutBinding):MessageViewHolder(binding.root){
    override fun bind(message: GroupMessageModel) {
        binding.apply {
            userName.text = message.userName
            //We need to format the date to show the year the month and maybe the day and hour and second
            val time = message.timeStamp
            val formatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG)
            val dateString =  if (time != null)formatter.format(time) else "No Date"
            timeSent.text = dateString
            messageText.text = message.text

        }
    }
}