package com.solt.thiochat.data.Groups.Messages

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
interface SearchItem {
    var searchQuery :String?
}
data class GroupMessageModel(
    val userId:String ="",
    val userName:String ="",
    val text:String ="",
    @ServerTimestamp
    val timeStamp : Date? = null)
 open class GroupMessageDisplayModel(
    val userId:String ="",
    val userName:String ="",
    val text:String ="",
    val timeStamp : Date? = null
):SearchItem {
      class UserGroupMessage(
         id: String ,
         name: String ,
        message: String ,
         timeSent: Date?
    ) : GroupMessageDisplayModel(id, name, message, timeSent)

      class NonUserGroupMessage(
        id: String ,
        name: String ,
        message: String ,
        timeSent: Date?
    ) : GroupMessageDisplayModel(id, name, message, timeSent)

    fun  toUserMessage() = UserGroupMessage(this.userId,this.userName,this.text,this.timeStamp)
    fun  toNonUserMessage() = NonUserGroupMessage(this.userId,this.userName,this.text,this.timeStamp)
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
//There will be two types of Message
// One is the user Message the Other is the non user Message
