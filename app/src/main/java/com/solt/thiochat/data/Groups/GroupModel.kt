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
    val groupColour :String = "",
    val modeOfAcceptance:String = ""
)
data class GroupMemberModel(
    val userId:String="",
    val userName:String="",
    val role:String =""
)

open class GroupDisplayModel(
    val documentId:String,
    val groupName:String,
    val groupColour: String,
    val modeOfAcceptance: String
){
    class UserInGroup(
        id:String,
        name:String,
        colour:String,
        modeOfAcceptance: String
    ):GroupDisplayModel(id,name,colour,modeOfAcceptance)
    class UserNotInGroup(
        id:String,
        name:String,
        colour:String,
        modeOfAcceptance: String
    ):GroupDisplayModel(id,name,colour,modeOfAcceptance)
    //We are going to overide the equals funtion
    //Lets see
    override fun equals(other: Any?): Boolean {
        //We will return
        return  if(other is GroupDisplayModel){
            this.documentId == other.documentId && this.groupName == other.groupName && this.groupColour == other.groupColour && this.modeOfAcceptance == other.modeOfAcceptance
        }
        else super.equals(other)
    }
}
//It will have subclasses
enum class Role(val title:String){
ADMIN("admin"),MEMBER("member");

    override fun toString(): String {
        return this.title
    }
    companion object{
        fun fromString(role:String?): Role?{
            for (entity in entries){
                if (entity.title == role){
                    return entity
                }
            }
            return null
        }
    }


}
enum class ModeOfAcceptance(val title: String){
    NONE("none"),REQUEST("request");

    override fun toString(): String {
        return title
    }
    companion object{
        fun fromString(mode:String?):ModeOfAcceptance?{
            for (entity in ModeOfAcceptance.entries){
                if (entity.title == mode){
                    return entity
                }
            }
            return null
        }
    }

}

