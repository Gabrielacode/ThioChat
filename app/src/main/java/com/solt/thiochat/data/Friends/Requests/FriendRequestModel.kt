package com.solt.thiochat.data.Friends.Requests

import com.solt.thiochat.data.Users.UserModel

data class FriendRequestModel(
    val user : UserModel  = UserModel()
)
data class FriendRequestDisplayModel(
    val documentId:String,
    val user: UserModel
)