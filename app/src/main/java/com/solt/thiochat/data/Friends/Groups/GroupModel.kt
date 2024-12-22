package com.solt.thiochat.data.Friends.Groups

import com.solt.thiochat.data.Users.UserModel

data class GroupInfoModel(
    val groupName:String = "",
    val  admin : UserModel = UserModel(),
    val listOfMembers :List<UserModel> = emptyList(),
    val groupColour :String = ""
)
