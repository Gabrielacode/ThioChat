package com.solt.thiochat.data.Friends.Messages

import com.google.firebase.firestore.ServerTimestamp
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import java.util.Date

open class FriendMessageModel (
     val userId:String ="",
     val userName:String ="",
     val text:String ="",
     @ServerTimestamp
     val timeStamp : Date? = null
 ){
    class UserMessage(
        id: String ,
        name: String ,
        message: String ,
        timeSent: Date?
    ) : FriendMessageModel(id, name, message, timeSent)

    class FriendMessage(
        id: String ,
        name: String ,
        message: String ,
        timeSent: Date?
    ) : FriendMessageModel(id, name, message, timeSent)
    //Convert to use and friend message
    fun  toUserMessage() =
        UserMessage(this.userId, this.userName, this.text, this.timeStamp)
    fun  toFriendMessage() =
        FriendMessage(this.userId, this.userName, this.text, this.timeStamp)
}

