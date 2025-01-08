package com.solt.thiochat.data.Friends.Messages

import com.google.firebase.firestore.ServerTimestamp
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.data.Groups.Messages.SearchItem
import java.util.Date
data  class FriendMessageModel (
    val userId:String ="",
    val userName:String ="",
    val text:String ="",
    @ServerTimestamp
    val timeStamp : Date? = null
)
open class FriendMessageDisplayModel (
     val userId:String ="",
     val userName:String ="",
     val text:String ="",
     @ServerTimestamp
     val timeStamp : Date? = null
 ):SearchItem{
    class UserMessage(
        id: String ,
        name: String ,
        message: String ,
        timeSent: Date?
    ) : FriendMessageDisplayModel(id, name, message, timeSent)

    class FriendMessage(
        id: String ,
        name: String ,
        message: String ,
        timeSent: Date?
    ) : FriendMessageDisplayModel(id, name, message, timeSent)
    //Convert to use and friend message
    fun  toUserMessage() =
        UserMessage(this.userId, this.userName, this.text, this.timeStamp)
    fun  toFriendMessage() =
        FriendMessage(this.userId, this.userName, this.text, this.timeStamp)

    override var searchQuery: String? = null
    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }
    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + (timeStamp?.hashCode() ?: 0)
        result = 31 * result + (searchQuery?.hashCode() ?: 0)
        return result
    }

}

