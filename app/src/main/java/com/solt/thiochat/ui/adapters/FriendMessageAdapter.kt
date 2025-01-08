package com.solt.thiochat.ui.adapters

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.solt.thiochat.data.Friends.Messages.FriendMessageDisplayModel
import com.solt.thiochat.data.Friends.Messages.FriendMessageModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageDisplayModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.databinding.NonUserMessageLayoutBinding
import com.solt.thiochat.databinding.UserMessageLayoutBinding
import java.text.SimpleDateFormat

val friendMessageDiffUtil = object : DiffUtil.ItemCallback<FriendMessageDisplayModel> (){

    override fun areItemsTheSame(
        oldItem: FriendMessageDisplayModel,
        newItem: FriendMessageDisplayModel
    ): Boolean {
       return  newItem.userId == oldItem.userId && newItem.userName == oldItem.userName && newItem.text == oldItem.text && newItem.timeStamp == oldItem.timeStamp && oldItem.searchQuery == newItem.searchQuery
    }

    override fun areContentsTheSame(
        oldItem: FriendMessageDisplayModel,
        newItem: FriendMessageDisplayModel
    ): Boolean {
       return oldItem == newItem
    }

}
const val FRIEND = 11
    //We will just reuse the messages viewholder as the main super class
class FriendMessageAdapter():ListAdapter<FriendMessageDisplayModel,FriendMessagesViewHolder>(friendMessageDiffUtil),Filterable {
        var standardCurrentList = emptyList<FriendMessageDisplayModel>()
        override fun getItemViewType(position: Int): Int {
            val item = getItem(position)
          return  if (item is FriendMessageDisplayModel.UserMessage) USER else FRIEND
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
        fun resetListToStandard(){
            Log.i("SearchList",standardCurrentList.joinToString { it.searchQuery.toString() })
            standardCurrentList.forEach { it.searchQuery = null }
            submitList(standardCurrentList)
        }

        override fun getFilter(): Filter {
            val filter = object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    //We will get the current list of messages
                    //If we paginate we cannot search the messages
                    val listOfMessages = this@FriendMessageAdapter.standardCurrentList
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
                    val list = results?.values as? List<FriendMessageDisplayModel>?: emptyList()
                    this@FriendMessageAdapter.submitList(list)
                    //We have to force update the viewholders for the spannables to show since sometimes the values are the same so the recycler view doesnt call on Bind

                    notifyItemRangeChanged(0,itemCount)
                }

            }
            return filter
        }
    }
open class FriendMessagesViewHolder(val view: View): RecyclerView.ViewHolder(view){
    open fun bind(message:FriendMessageDisplayModel){}
    open fun highlightSearchedText(searchQuery:String){}
}
    class UserToFriendMessageViewHolder(val binding: UserMessageLayoutBinding):FriendMessagesViewHolder(binding.root){
        override fun bind(message: FriendMessageDisplayModel) {
         binding.apply {
             userName.text = message.userName
             messageText.text = message.text
             val time = message.timeStamp
             val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
             val dateString =  if (time != null)formatter.format(time) else "No Date"
             timeSent.text = dateString
             if (!message.searchQuery.isNullOrBlank() ){
                 Log.i("SearchQuery","Is Search Query Null After Search ${message.searchQuery}")
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
                Log.i("SearchQuery","First : ${min}, Last : $max")
                //If we remove all characters then the search query becomes empty space and then when we regex it to find it index it returns -1 which we dont want
                if(min <0 || max < 0) return
                //Now we will get the highest and lowest ranges
                //If the max is the same as the min the spannable will not show so we increase it by 1
                else messageAsSpannableString.setSpan(
                    ForegroundColorSpan(Color.GREEN),min,max+1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }


            //If the string was already a spannable just edit it
            binding.messageText.setText(messageAsSpannableString, TextView.BufferType.SPANNABLE)
        }
    }
    class FriendToUserMessageViewHolder(val binding: NonUserMessageLayoutBinding):FriendMessagesViewHolder(binding.root){
        override fun bind(message: FriendMessageDisplayModel) {
            binding.apply {
                userName.text = message.userName
                messageText.text = message.text
                val time = message.timeStamp
                val formatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
                val dateString =  if (time != null)formatter.format(time) else "No Date"
                timeSent.text = dateString
                if (!message.searchQuery.isNullOrBlank()){
                    Log.i("SearchQuery","Is Search Query Null After Search ${message.searchQuery}")
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
                Log.i("SearchQuery","First : ${min}, Last : $max")
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

