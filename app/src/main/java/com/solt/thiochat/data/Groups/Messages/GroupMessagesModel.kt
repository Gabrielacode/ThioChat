package com.solt.thiochat.data.Groups.Messages

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

 open class GroupMessageModel(
    val userId:String ="",
    val userName:String ="",
    val text:String ="",
    @ServerTimestamp
    val timeStamp : Date? = null
) {
      class UserGroupMessage(
         id: String ,
         name: String ,
        message: String ,
         timeSent: Date?
    ) : GroupMessageModel(id, name, message, timeSent)

      class NonUserGroupMessage(
        id: String ,
        name: String ,
        message: String ,
        timeSent: Date?
    ) : GroupMessageModel(id, name, message, timeSent)

    fun  toUserMessage() = UserGroupMessage(this.userId,this.userName,this.text,this.timeStamp)
    fun  toNonUserMessage() = NonUserGroupMessage(this.userId,this.userName,this.text,this.timeStamp)
}
//There will be two types of Message
// One is the user Message the Other is the non user Message
