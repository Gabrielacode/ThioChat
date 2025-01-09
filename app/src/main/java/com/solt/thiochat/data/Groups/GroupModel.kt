package com.solt.thiochat.data.Groups

import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.util.CustomClassMapper
import com.solt.thiochat.data.Groups.Messages.GroupMessageDisplayModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.data.Users.UserModel
import kotlinx.coroutines.flow.Flow
import java.util.Date

data class GroupInfoModel(
    val groupName:String = "",
    val groupColour :String = "",
    val modeOfAcceptance:String = ""
)
data class GroupMemberModel(
    val userId:String="",
    val userName:String="",
    val description:String= "",
    val role:String =""
)

open class GroupDisplayModel(
    val documentId:String,
    val groupName:String,
    val groupColour: String,
    val modeOfAcceptance: String,

){
    //This will be used to set the latest message of the group which will be a flow

    var latestMessages : Flow<GroupMessageModel?>? = null



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
        //We will return true if the groupName, groupColour , modeOfAcceptance and documentId are the same
        //Always generate a hashcode that obeys the equals function
        return  if(other is GroupDisplayModel){
            this.documentId == other.documentId && this.groupName == other.groupName && this.groupColour == other.groupColour && this.modeOfAcceptance == other.modeOfAcceptance && this.hashCode() == other.hashCode()
        }
        else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = documentId.hashCode()
        result = 31 * result + groupName.hashCode()
        result = 31 * result + groupColour.hashCode()
        result = 31 * result + modeOfAcceptance.hashCode()
        return result
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

