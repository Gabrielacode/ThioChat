package com.solt.thiochat.data.Friends

import com.solt.thiochat.data.Friends.Messages.FriendMessageDisplayModel
import com.solt.thiochat.data.Friends.Messages.FriendMessageModel
import kotlinx.coroutines.flow.Flow

data class FriendModel( val userId :String = "",val userName :String = "")
data class FriendDisplayModel(
 val userId :String = "",val userName :String = "",
 var latestMessage : Flow<FriendMessageModel?>? = null
)
