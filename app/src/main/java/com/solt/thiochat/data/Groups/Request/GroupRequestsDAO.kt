package com.solt.thiochat.data.Groups.Request

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.Groups.GROUP_COLLECTION
import com.solt.thiochat.data.Groups.GROUP_MEMBERS_COLLECTION
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.Groups.GroupInfoModel
import com.solt.thiochat.data.Groups.GroupMemberModel
import com.solt.thiochat.data.Groups.Role
import com.solt.thiochat.data.Groups.USER_GROUP_COLLECTION
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.USERS_COLLECTION
import com.solt.thiochat.data.Users.UserModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
const val GROUP_REQUEST_COLLECTION = "group_requests"
class GroupRequestsDAO @Inject constructor() {
    @Inject lateinit var firestore: FirebaseFirestore

     suspend  fun addGroupRequest(groupRequest: GroupRequestModel,group:GroupDisplayModel):OperationResult{
         return withContext(Dispatchers.IO) {
             try {
                 val groupRequestCollection =
                     firestore.collection(GROUP_COLLECTION).document(group.documentId).collection(
                         GROUP_REQUEST_COLLECTION
                     )

                 val result = groupRequestCollection.add(groupRequest).await()
                 if (result != null){
                     OperationResult.Success("Sent request")
                 } else return@withContext OperationResult.Failure(IllegalStateException("Failed to send request"))

             } catch (e: Exception) {
                 if (e is CancellationException) throw e
                 else return@withContext OperationResult.Failure(e)
             }
         }
    }
     //For we add the user to the members collection and delete the request
    //In the update we will add the user to the users list of groups he is in

    suspend fun  acceptRequest(groupRequest: GroupRequestDisplayModel,group :GroupDisplayModel):OperationResult{
       return withContext(Dispatchers.IO){
           try {
               val groupRequestDocRef = firestore.collection(GROUP_COLLECTION).document(group.documentId).collection(
                   GROUP_REQUEST_COLLECTION).document(groupRequest.documentId)
               val groupMembersRef = firestore.collection(GROUP_COLLECTION).document(group.documentId).collection(
                   GROUP_MEMBERS_COLLECTION)
               //This is the user we want to accept request his
               val userGroupsCollection = firestore.collection(USERS_COLLECTION).document(groupRequest.userModel.userId)
                   .collection(USER_GROUP_COLLECTION)

               firestore.runBatch {
                   val memberModel = GroupMemberModel(groupRequest.userModel.userId,groupRequest.userModel.userName,groupRequest.userModel.description,Role.MEMBER.toString())
                   it.set(groupMembersRef.document(memberModel.userId),memberModel)
                   it.set(userGroupsCollection.document(group.documentId),
                       GroupInfoModel(group.groupName,group.groupColour,group.modeOfAcceptance)
                   )
                   it.delete(groupRequestDocRef)
               }.await()
                return@withContext OperationResult.Success("Accepted User Request")

           }catch (e:Exception){
               if (e is CancellationException) throw  e
               else return@withContext OperationResult.Failure(e)
           }
       }
     }
    //Just delete the request
     suspend fun rejectRequest(groupRequest: GroupRequestDisplayModel,group: GroupDisplayModel):OperationResult{
         return  withContext(Dispatchers.IO){
           try {
               val groupRequestDocRef = firestore.collection(GROUP_COLLECTION).document(group.documentId).collection(
                   GROUP_REQUEST_COLLECTION).document(groupRequest.documentId)


               val result = groupRequestDocRef.delete().await()
               return@withContext OperationResult.Success("Accepted User Request")

           }catch (e:Exception){
               if (e is CancellationException) throw  e
               else return@withContext OperationResult.Failure(e)
           }
       }
     }

    //The user must be an admin to do the operations apart from the add group request
    fun displayRequestsForGroup(group: GroupDisplayModel): Flow<List<GroupRequestDisplayModel>>{
        val groupRequestRef = firestore.collection(GROUP_COLLECTION).document(group.documentId).collection(
            GROUP_REQUEST_COLLECTION)
        val flowOfRequests = groupRequestRef.snapshots().map {
            it.documents
        }.map {
           it.map { document->
               val documentId = document.id
               val groupRequestModel = document.toObject<GroupRequestModel>()
               GroupRequestDisplayModel(documentId,groupRequestModel?.userModel?: UserModel())

           }
        }
        return flowOfRequests
    }
}