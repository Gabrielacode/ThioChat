package com.solt.thiochat.data.Groups

import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.util.CustomClassMapper
import com.solt.thiochat.data.Users.UserModel
import java.util.Date

data class GroupInfoModel(
    val groupName:String = "",
    val groupColour :String = ""
)
data class GroupMemberModel(
    val userId:String="",
    val userName:String="",
    val role:String =""
)

data class GroupDisplayModel(
    val documentId:String,
    val groupName:String,
    val groupColour: String,
)
enum class Role(val title:String){
ADMIN("admin"),MEMBER("member");

    override fun toString(): String {
        return this.title
    }
    fun fromString(role:String): Role?{
        for (entity in entries){
            if (entity.title == role){
                return entity
            } else return null
        }
       return null
    }

}

