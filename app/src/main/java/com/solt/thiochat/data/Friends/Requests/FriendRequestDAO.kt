package com.solt.thiochat.data.Friends.Requests

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.solt.thiochat.data.Friends.FRIENDS_COLLECTION
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.Groups.GROUP_COLLECTION
import com.solt.thiochat.data.Groups.GROUP_MEMBERS_COLLECTION
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.Groups.GroupMemberModel
import com.solt.thiochat.data.Groups.Request.GROUP_REQUEST_COLLECTION
import com.solt.thiochat.data.Groups.Request.GroupRequestDisplayModel
import com.solt.thiochat.data.Groups.Request.GroupRequestModel
import com.solt.thiochat.data.Groups.Role
import com.solt.thiochat.data.Groups.USER_GROUP_COLLECTION
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.USERS_COLLECTION
import com.solt.thiochat.data.Users.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

const val FRIEND_REQUEST_COLLECTION = "friend_requests"
class FriendRequestDAO  @Inject constructor(){
    @Inject lateinit var firestore: FirebaseFirestore
    suspend  fun addFriendRequest(user: UserModel,friend:FriendModel): OperationResult {
        return withContext(Dispatchers.IO) {
            //We will add the request to the friend we want to add not us
            try {
                val friendRequestCollection =
                    firestore.collection(USERS_COLLECTION).document(friend.userId).collection(
                       FRIEND_REQUEST_COLLECTION
                    )
                val result = friendRequestCollection.add(FriendRequestModel(user)).await()
                if (result != null){
                    OperationResult.Success("Sent request")
                } else return@withContext OperationResult.Failure(IllegalStateException("Failed to send request"))

            } catch (e: Exception) {
                if (e is CancellationException) throw e
                else return@withContext OperationResult.Failure(e)
            }
        }
    }

    suspend fun  acceptFriendRequest(friendRequest: FriendRequestDisplayModel, user:UserModel): OperationResult {
        //This will be that when the user accepts the request
        //The friend will be added the user's friend collection and the user will be friend's friend collection
        //Then delete  the request

        return withContext(Dispatchers.IO){
            try {
                val userFriendDocRef = firestore.collection(USERS_COLLECTION).document(user.userId).collection(
                    FRIENDS_COLLECTION
                ).document(friendRequest.user.userId)
                val friendUserDocRef = firestore.collection(USERS_COLLECTION).document(friendRequest.user.userId).collection(
                    FRIENDS_COLLECTION
                ).document(user.userId)
                val friendRequestDocRef = firestore.collection(USERS_COLLECTION).document(user.userId).collection(
                    FRIEND_REQUEST_COLLECTION).document(friendRequest.documentId)

                val result = firestore.runBatch {
                    val userAsFriend = FriendModel(user.userId,user.userName)
                    val friendRequestAsFriend = FriendModel(friendRequest.user.userId,friendRequest.user.userName)
                    it.set(friendUserDocRef,userAsFriend)
                    it.set(userFriendDocRef,friendRequestAsFriend)
                    it.delete(friendRequestDocRef)
                }.await()
                return@withContext OperationResult.Success("Accepted Friend Request")

            }catch (e:Exception){
                if (e is CancellationException) throw  e
                else return@withContext OperationResult.Failure(e)
            }
        }
    }

    suspend fun rejectFriendRequest(friendRequest: FriendRequestDisplayModel, user:UserModel): OperationResult {
        //Just delete the request
        return  withContext(Dispatchers.IO){
            try {
                val friendRequestDocRef = firestore.collection(USERS_COLLECTION).document(user.userId).collection(
                    FRIEND_REQUEST_COLLECTION
                ).document(friendRequest.documentId)

                val result = friendRequestDocRef.delete().await()
                return@withContext OperationResult.Success("Accepted User Request")

            }catch (e:Exception){
                if (e is CancellationException) throw  e
                else return@withContext OperationResult.Failure(e)
            }
        }
    }


    fun displayFriendRequestsForUser(user:UserModel): Flow<List<FriendRequestDisplayModel>> {
        val friendRequestRef = firestore.collection(USERS_COLLECTION).document(user.userId).collection(
           FRIEND_REQUEST_COLLECTION
        )
        val flowOfRequests = friendRequestRef.snapshots().map {
            it.documents
        }.map {
            it.map { document->
                val documentId = document.id
                val friendRequestModel = document.toObject<FriendRequestModel>()
                FriendRequestDisplayModel(documentId,friendRequestModel?.user?:UserModel())

            }
        }
        return flowOfRequests
    }

}