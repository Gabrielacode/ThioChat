package com.solt.thiochat.data.Friends

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.USERS_COLLECTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

//This is the friends subcollection
const val FRIENDS_COLLECTION = "friends"
class FriendsDao @Inject constructor() {
    @Inject lateinit var firestore: FirebaseFirestore
    suspend fun addFriend( userId:String ,friend:FriendModel):OperationResult {
        return withContext(Dispatchers.IO) {
            try {
              val friendCollectionReference = firestore.collection(USERS_COLLECTION).document(userId).collection(
                  FRIENDS_COLLECTION).document(friend.userId).set(friend).await()
                OperationResult.Success("Added Friend Successfully")
            } catch (e: Exception) {
                OperationResult.Failure(e)
            }
        }
    }
    suspend fun getFriends(userId: String): Flow<List<FriendModel>>{
        return callbackFlow {
            val friendListObserver = firestore.collection(USERS_COLLECTION).document(userId).collection(
                FRIENDS_COLLECTION).addSnapshotListener { value, error ->
                    if (error  != null){
                        return@addSnapshotListener
                    }
                val listOfFriends = value?.toObjects<FriendModel>()
                trySend(listOfFriends?: emptyList())
            }
            awaitClose { friendListObserver.remove() }
        }
    }
}