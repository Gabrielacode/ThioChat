package com.solt.thiochat.data.Groups.Request

import com.solt.thiochat.data.Users.UserModel

data class GroupRequestModel(
    val userModel: UserModel = UserModel(),

)
data class GroupRequestDisplayModel(
    val documentId:String,
    val userModel: UserModel

)
//Maybe i will use it later
enum class STATE_REQUEST(val title:String){

}