package com.solt.thiochat.ui.adapters


import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.graphics.MaskFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.format.DateFormat
import android.text.style.BackgroundColorSpan
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
import android.widget.Filter
import android.widget.Filterable
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
import com.solt.thiochat.data.Groups.Messages.GroupMessageDisplayModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.databinding.NonUserMessageLayoutBinding
import com.solt.thiochat.databinding.SendFriendRequestLayoutBinding
import com.solt.thiochat.databinding.UserMessageLayoutBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
const val USER = 1
const val NON_USER = 2


val groupMessageDiffUtil = object : DiffUtil.ItemCallback<GroupMessageDisplayModel>() {

    override fun areItemsTheSame(
        oldItem: GroupMessageDisplayModel,
        newItem: GroupMessageDisplayModel
    ): Boolean {
        return newItem.userId == oldItem.userId && newItem.userName == oldItem.userName && newItem.text == oldItem.text && newItem.timeStamp == oldItem.timeStamp && oldItem.searchQuery == newItem.searchQuery
    }

    override fun areContentsTheSame(
        oldItem: GroupMessageDisplayModel,
        newItem: GroupMessageDisplayModel
    ): Boolean {
        return oldItem == newItem
    }

}
class GroupMessagesAdapter(val fragment: Fragment,val checkIfUsersAreFriends: suspend (GroupMessageDisplayModel)->Boolean , val onSendRequest:  (GroupMessageDisplayModel)->Unit,val usersAreFriends:()->Unit):ListAdapter<GroupMessageDisplayModel,MessageViewHolder>(groupMessageDiffUtil),Filterable {
    var standardCurrentList = emptyList<GroupMessageDisplayModel>()

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
       return if (item is GroupMessageDisplayModel.UserGroupMessage) USER else NON_USER

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
        Log.i("SearchQuery","Bind View Holder called for ${getItem(position).text}")
        val item = getItem(position)
        holder.bind(item)
    }
    fun resetListToStandard(){
        //This will reset the list after a search
      //We will reset it to null
        standardCurrentList.forEach { it.searchQuery = null }
        submitList(standardCurrentList)


    }

    override fun getFilter(): Filter {
        val filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
               //We will get the current list of messages
                //If we paginate we cannot search the messages
                val listOfMessages = ArrayList(this@GroupMessagesAdapter.standardCurrentList)

                //Then we filter the list of messages  based on the constraint
                val filteredList = if (constraint == null) listOfMessages
                else listOfMessages.filter { it.text.contains(constraint.toString(),false) }.map { it.apply { it.searchQuery =
                    constraint.toString()
                } }
                return  FilterResults().apply {
                    values = filteredList
                    count = filteredList.count()
                }

            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val list = results?.values as? List<GroupMessageDisplayModel>?: emptyList()
                this@GroupMessagesAdapter.submitList(list)
                //We have to force update the viewholders for the spannables to show since sometimes the values are the same so the recycler view doesnt call on Bind

                notifyItemRangeChanged(0,itemCount)
            }

        }
        return filter
    }


}
 open class MessageViewHolder(val view:View):ViewHolder(view){
   open fun bind(message:GroupMessageDisplayModel){}
     open fun highlightSearchedText(searchQuery:String){}

}
 class UserMessageViewHolder(val fragment :Fragment,val binding: UserMessageLayoutBinding,val checkIfUsersAreFriends: suspend (GroupMessageDisplayModel)->Boolean , val onSendRequest:  (GroupMessageDisplayModel)->Unit,val usersAreFriends:()->Unit):MessageViewHolder(binding.root){
    override fun bind(message: GroupMessageDisplayModel) {
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
            if (!message.searchQuery.isNullOrBlank()){
                Log.i("SearchQuery","Is Search Query Null After Search ${message.searchQuery?.isBlank()}")
                highlightSearchedText(message.searchQuery!!)
            }
        }
    }
     override fun highlightSearchedText(searchQuery: String) {
         //Get the text
         val message = binding.messageText.text.toString()
         val messageAsSpannableString = message.toSpannable()
         //Check for the occurrence of the search Query and thier index
         val results =  try{searchQuery.toRegex().findAll(message)}catch(e:Exception ){
             emptySequence<MatchResult>()
         }

         results.forEach {
             //High light all occurances
             val range = it.range
             val max = range.maxOrNull()?:0
             val min = range.minOrNull()?:0
             Log.i("SearchQuery"," Item : $message First : ${min}, Last : $max")
             //If we remove all characters then the search query becomes empty space and then when we regex it to find it index it returns -1 which we dont want
             if(min <0 || max < 0) return
             //Now we will get the highest and lowest ranges
             //If the max is the same as the min the spannable will not show so we increase it by 1
             else messageAsSpannableString.setSpan(ForegroundColorSpan(Color.GREEN),min,max+1 ,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
         }


         //If the string was already a spannable just edit it
         binding.messageText.setText(messageAsSpannableString,TextView.BufferType.SPANNABLE)
     }


}
class NonUserMessageViewHolder( val fragment: Fragment,val binding: NonUserMessageLayoutBinding,val checkIfUsersAreFriends: suspend (GroupMessageDisplayModel)->Boolean , val onSendRequest:  (GroupMessageDisplayModel)->Unit,val usersAreFriends:()->Unit):MessageViewHolder(binding.root){
    override fun bind(message: GroupMessageDisplayModel) {
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
//If the search query is not null highlight the
        if (!message.searchQuery.isNullOrBlank()){
            highlightSearchedText(message.searchQuery!!)
        }

        }
    override fun highlightSearchedText(searchQuery: String) {
        //Get the text
        val message = binding.messageText.text.toString()
        val messageAsSpannableString = message.toSpannable()
        //Check for the occurrence of the search Query and thier index
        val results =  try{searchQuery.toRegex().findAll(message)}catch(e:Exception ){
            emptySequence<MatchResult>()
        }

        results.forEach {
            //High light all occurances
            val range = it.range
            val max = range.maxOrNull()?:0
            val min = range.minOrNull()?:0
            Log.i("SearchQuery"," Item : $message First : ${min}, Last : $max")
            //If we remove all characters then the search query becomes empty space and then when we regex it to find it index it returns -1 which we dont want
            if(min <0 || max < 0) return
            //Now we will get the highest and lowest ranges
            //If the max is the same as the min the spannable will not show so we increase it by 1
            else messageAsSpannableString.setSpan(ForegroundColorSpan(Color.GREEN),min,max+1 ,Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }


        //If the string was already a spannable just edit it
        binding.messageText.setText(messageAsSpannableString,TextView.BufferType.SPANNABLE)
    }
    }
