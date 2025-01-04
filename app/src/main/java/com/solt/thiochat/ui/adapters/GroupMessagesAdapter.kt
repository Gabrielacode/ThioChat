package com.solt.thiochat.ui.adapters


import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.MaskFilter
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.format.DateFormat
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.MaskFilterSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.solt.thiochat.R
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.databinding.NonUserMessageLayoutBinding
import com.solt.thiochat.databinding.SendFriendRequestLayoutBinding
import com.solt.thiochat.databinding.UserMessageLayoutBinding
import kotlinx.coroutines.launch
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
class GroupMessagesAdapter(val fragment: Fragment,val checkIfUsersAreFriends: suspend (GroupMessageModel)->Boolean , val onSendRequest:  (GroupMessageModel)->Unit,val usersAreFriends:()->Unit):ListAdapter<GroupMessageModel,MessageViewHolder>(groupMessageDiffUtil) {
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
       return if (item is GroupMessageModel.UserGroupMessage) USER else NON_USER

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
      return if(viewType == USER){
          val binding = UserMessageLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
           UserMessageViewHolder(fragment,binding,checkIfUsersAreFriends,onSendRequest,usersAreFriends)
      }else {
          val binding = NonUserMessageLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
          NonUserMessageViewHolder( fragment, binding,checkIfUsersAreFriends,onSendRequest,usersAreFriends)
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
 class UserMessageViewHolder(val fragment :Fragment,val binding: UserMessageLayoutBinding,val checkIfUsersAreFriends: suspend (GroupMessageModel)->Boolean , val onSendRequest:  (GroupMessageModel)->Unit,val usersAreFriends:()->Unit):MessageViewHolder(binding.root){
    override fun bind(message: GroupMessageModel) {
        binding.apply {
            userName.text = message.userName
            //We need to format the date to show the year the month and maybe the day and hour and second
            val time = message.timeStamp
            val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val dateString =  if (time != null)formatter.format(time) else "No Date"
            timeSent.text = dateString
            messageText.text = message.text
            //We are going to use spannable string


            //By setting the buffer type we can edit the span without editing and redrawing the text

            //We will be using bottom dialog sheet
            val requestBinding = SendFriendRequestLayoutBinding.inflate(LayoutInflater.from(root.context))
            val bottomModalDialog = BottomSheetDialog(root.context).apply {
                setContentView(requestBinding.root)
            }
            requestBinding.root.setOnClickListener {
                onSendRequest(message)
                bottomModalDialog.dismiss()
            }
            userName.setOnClickListener {
                fragment.viewLifecycleOwner.lifecycleScope.launch {
                //Check if the users are friends
                if (checkIfUsersAreFriends(message)){
                    usersAreFriends
                }else{
                    //If not show a send request dialog
                bottomModalDialog.show()
                }
            }}
        }
    }
}
class NonUserMessageViewHolder( val fragment: Fragment,val binding: NonUserMessageLayoutBinding,val checkIfUsersAreFriends: suspend (GroupMessageModel)->Boolean , val onSendRequest:  (GroupMessageModel)->Unit,val usersAreFriends:()->Unit):MessageViewHolder(binding.root){
    override fun bind(message: GroupMessageModel) {
        binding.apply {
            userName.text = message.userName
            //We need to format the date to show the year the month and maybe the day and hour and second
            val time = message.timeStamp
            val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val dateString =  if (time != null)formatter.format(time) else "No Date"
            timeSent.text = dateString
            messageText.text = message.text
            val requestBinding = SendFriendRequestLayoutBinding.inflate(LayoutInflater.from(root.context))
            val bottomModalDialog = BottomSheetDialog(root.context).apply {
                setContentView(requestBinding.root)
            }
            userName.setOnClickListener {
                fragment.viewLifecycleOwner.lifecycleScope.launch{
                //We will be using bottom dialog sheet

                //Check if the users are friends
                if (checkIfUsersAreFriends(message)){
                    usersAreFriends
                }else{
                    //If not show a send request dialog
                    bottomModalDialog.show()
                }
            }
            }
            requestBinding.root.setOnClickListener {
                    Log.i("gg","Launch is called")
                    onSendRequest(message)
                    bottomModalDialog.dismiss()
            }
            }


        }
    }
