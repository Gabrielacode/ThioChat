package com.solt.thiochat.data.Friends

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.USERS_COLLECTION
import com.solt.thiochat.data.Users.UserModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
                if(e is CancellationException) throw e
                else OperationResult.Failure(e)
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

    suspend fun areTwoOfYouFriends(userModel: UserModel,friend: FriendModel,onFailure:(String)->Unit):Boolean{
        //This will check if two of you are friends by checking in the user collection
        /* First it will check the users collection if there is a document under the friends collection with the id as the user id of the friend
        Then it will do the same for the friend
        //If both options are true then it will return true
        * */
        return withContext(Dispatchers.IO){
            try {
                val userFriendDocRef = firestore.collection(USERS_COLLECTION).document(userModel.userId).collection(
                    FRIENDS_COLLECTION).document(friend.userId)
                val friendUserDocRef = firestore.collection(USERS_COLLECTION).document(friend.userId).collection(
                    FRIENDS_COLLECTION).document(userModel.userId)
                val userResult = userFriendDocRef.get().await()
                val friendResult = friendUserDocRef.get().await()
                return@withContext userResult.exists() && friendResult.exists()
            }catch (e:Exception){
                if (e is CancellationException)throw  e
                else {
                    onFailure(e.message?:"Error")
                    return@withContext false
                }
            }
        }
    }
 fun searchFriendByName( user:UserModel,name:String):Flow<List<FriendModel>>{
     //The idea for the query was by https://medium.com/feedflood/filter-by-search-keyword-in-cloud-firestore-query-638377bf0123
    val friendsCollection =  firestore.collection(USERS_COLLECTION).document(user.userId).collection(
        FRIENDS_COLLECTION)
     val query = friendsCollection.whereGreaterThanOrEqualTo("userName",name)
         .whereLessThan("userName",name+"z")
     val flowOfFriends = query.snapshots().map {
       it.toObjects<FriendModel>()
     }
     return flowOfFriends
 }
}