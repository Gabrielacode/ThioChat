package com.solt.thiochat.data.Groups.Messages

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

sealed class GroupMessageModel(
    val userId:String ="",
    val userName:String ="",
    val text:String ="",
    @ServerTimestamp
    val timeStamp : Date
) {
     data class UserGroupMessage(
        val id: String = "",
        val name: String = "",
        val message: String = "",
        @ServerTimestamp
        val timeSent: Date
    ) : GroupMessageModel(id, name, message, timeSent)

     data class NonUserGroupMessage(
        val id: String = "",
        val name: String = "",
        val message: String = "",
        @ServerTimestamp
        val timeSent: Date
    ) : GroupMessageModel(id, name, message, timeSent)
}
//There will be two types of Message
// One is the user Message the Other is the non user Message
