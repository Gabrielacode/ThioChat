package com.solt.thiochat.data.Friends.Messages

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.Friends.FRIENDS_COLLECTION
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.USERS_COLLECTION
import com.solt.thiochat.data.Users.UserModel
import com.solt.thiochat.ui.adapters.USER
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
const val FRIEND_MESSAGES_SUBCOLLECTION = "friend_messages"
class FriendMessagesDAO @Inject constructor() {
    @Inject lateinit var firestore: FirebaseFirestore


    fun getMessagesWithFriend(user:UserModel,friendModel: FriendModel): Flow<List<FriendMessageModel>>{
        //We will only get messages from the users friends messages subcollection
        val friendMessagesSubcollection = firestore.collection(USERS_COLLECTION).document(user.userId)
            .collection(FRIENDS_COLLECTION).document(friendModel.userId).collection(
                FRIEND_MESSAGES_SUBCOLLECTION)
        val flowOfMessages = friendMessagesSubcollection.snapshots().map { it.toObjects<FriendMessageModel>() }
        return flowOfMessages

    }
  suspend  fun sendMessageToFriend(user: UserModel,friendModel: FriendModel,message:FriendMessageModel):OperationResult{
       return withContext(Dispatchers.IO){
      try {
          //Here we will add the message to the friend and user message model
            val userFriendsMessageSubCollection = firestore.collection(USERS_COLLECTION).document(user.userId)
                .collection(FRIENDS_COLLECTION).document(friendModel.userId).collection(
                    FRIEND_MESSAGES_SUBCOLLECTION).document()
            val friendFriendMessageSubCollection = firestore.collection(USERS_COLLECTION).document(friendModel.userId)
                .collection(FRIENDS_COLLECTION).document(user.userId).collection(
                    FRIEND_MESSAGES_SUBCOLLECTION).document()
            firestore.runBatch {
                //This will check if the user is sending a message to him or herself
                if(user.userId == friendModel.userId){
                    it.set(userFriendsMessageSubCollection,message)
                }else{
                it.set(userFriendsMessageSubCollection,message)
                it.set(friendFriendMessageSubCollection,message)}
            }.await()
            OperationResult.Success("Successfully sent the message")

        }catch (e:Exception){
            if (e is CancellationException) throw e
            else OperationResult.Failure(e)
        }
    }
  }
}