package com.solt.thiochat.data.Groups

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.Groups.Request.GROUP_REQUEST_COLLECTION
import com.solt.thiochat.data.Groups.Request.GroupRequestModel
import com.solt.thiochat.data.Groups.Request.GroupRequestsDAO
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
const val GROUP_COLLECTION = "groups"
const val GROUP_MEMBERS_COLLECTION = "group_members"
class GroupDAO @Inject constructor() {
  @Inject lateinit var firestore: FirebaseFirestore
  @Inject lateinit var groupRequestsDAO: GroupRequestsDAO
    suspend fun checkIfUserIsAdmin(userModel: UserModel, groupModel: GroupDisplayModel, onFailure:(String)->Unit):Boolean {
       return withContext(Dispatchers.IO) {
            try {
                val groupMembersCollection =
                    firestore.collection(GROUP_COLLECTION).document(groupModel.documentId)
                        .collection(
                            GROUP_MEMBERS_COLLECTION
                        ).document(userModel.userId)
                val isUserAdminResult = groupMembersCollection.get().await()
                if (isUserAdminResult.exists()) {
                    val memberModel = isUserAdminResult.toObject<GroupMemberModel>()
                    if (Role.fromString(memberModel?.role) == Role.ADMIN) return@withContext true
                    else return@withContext false
                } else return@withContext false
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                else {
                    onFailure(e.message ?: "Error")
                    return@withContext false
                }
            }
        }
    }

    suspend fun createGroup( creator:UserModel,groupInfoModel: GroupInfoModel):OperationResult{
    //Only create if it doesn't exist
    return withContext(Dispatchers.IO){
        try {
            val groupCollection = firestore.collection(GROUP_COLLECTION)
         //Get list of groups with the name if empty
            val listOfGroupsResults = groupCollection.whereEqualTo("groupName",groupInfoModel.groupName).get().await()
            val listOfGroups =  listOfGroupsResults.toObjects<GroupInfoModel>()

            if(listOfGroups.isNotEmpty()){
                return@withContext OperationResult.Failure(IllegalStateException("Groups with the same name exists"))
            }
            //Create group then

            //Batch write
           val operations =  firestore.runBatch {
                //Add the group
               val groupDocRef = groupCollection.document()
                it.set(groupDocRef,groupInfoModel)
                //Add the user as the admin
                val member = GroupMemberModel(creator.userId,creator.userName, Role.ADMIN.toString())
                val creatorDocRef = groupDocRef.collection(GROUP_MEMBERS_COLLECTION).document(creator.userId)
                it.set(creatorDocRef,member)

            }.await()
         return@withContext OperationResult.Success("Successfully created group")
            
        }catch (e:Exception){
            if(e is CancellationException) throw e
            else  return@withContext OperationResult.Failure(e)
        }
    }
}
    fun getGroups():Flow<List<GroupDisplayModel>>{
        return callbackFlow {
            val groupCollectionObserver = firestore.collection(GROUP_COLLECTION)
                .addSnapshotListener { value, error ->
                    if(error != null){
                        return@addSnapshotListener
                    }

                    val listOfGroups = ArrayList<GroupDisplayModel>()
                    //Groups will be accessible to everybody
                    for(i in value!!){

                        val documentId = i.id
                        val documentInfoModel = i.toObject<GroupInfoModel>()
                        listOfGroups.add(GroupDisplayModel(documentId,documentInfoModel.groupName,documentInfoModel.groupColour,documentInfoModel.modeOfAcceptance))
                    }

                    //Send it
                     val sending=trySend(listOfGroups)


                }
            awaitClose { groupCollectionObserver.remove() }
        }
    }
    fun getGroupsUserIsAMember(user:UserModel):Flow<List<GroupDisplayModel>>{
        return callbackFlow {
            val coroutineScope =  CoroutineScope(Dispatchers.IO)
            val groupCollectionObserver = firestore.collection(GROUP_COLLECTION)
                .addSnapshotListener { value, error ->
                    if(error != null){
                        return@addSnapshotListener
                    }
                    //Run the checking on another thread
                  coroutineScope.launch {
                      if (isActive) {
                          val listOfGroups = ArrayList<GroupDisplayModel>()
                          //Groups will be accessible to everybody
                          for (i in value!!) {
                              val documentId = i.id
                              val documentInfoModel = i.toObject<GroupInfoModel>()
                              //Search if there is a member collection of the user
                             val getUserTask = firestore.collection(GROUP_COLLECTION).document(i.id).collection(
                                 GROUP_MEMBERS_COLLECTION
                             ).document(user.userId).get().await()
                              if (getUserTask != null && getUserTask.exists()){
                                  val memberModel = getUserTask.toObject<GroupMemberModel>()
                                  if(memberModel?.userId == user.userId){
                                     listOfGroups.add(GroupDisplayModel(documentId,documentInfoModel.groupName,documentInfoModel.groupColour, documentInfoModel.modeOfAcceptance))
                                  }
                                  }
                              }
                          //Send it
                          val sending = trySend(listOfGroups)
                      }

                      }
                  }
            awaitClose {
                coroutineScope.cancel()
                groupCollectionObserver.remove() }
        }
    }

   suspend fun addUserToGroup(user :UserModel, groupModel: GroupDisplayModel):OperationResult {
       //Do we need to update it to contain the invitations or make a separate method
       //For now i will update it
       return withContext(Dispatchers.IO) {
           try {
               val groupDocRef = firestore.collection(GROUP_COLLECTION).document(groupModel.documentId)
               val groupMemberCollection = firestore.collection(GROUP_COLLECTION).document(groupModel.documentId)
                       .collection(GROUP_MEMBERS_COLLECTION)
               val groupRequestsCollection =  firestore.collection(GROUP_COLLECTION).document(groupModel.documentId)
                   .collection(GROUP_REQUEST_COLLECTION)
               //Check if the group mode of acceptance is NONE or REQUEST
               val mode = ModeOfAcceptance.fromString(groupModel.modeOfAcceptance)
               if (mode == null) return@withContext OperationResult.Failure(IllegalStateException("Couldn't determine mode of acceptance"))
               when(mode){
                   ModeOfAcceptance.NONE ->{
                       //Just add the user directly
                       val member = GroupMemberModel(user.userId,user.userName,Role.MEMBER.toString())
                       val result  = groupMemberCollection.document(member.userId).set(member).await()
                       return@withContext OperationResult.Success("Successfully added user")

                   }
                   ModeOfAcceptance.REQUEST -> {
                       //Send a request
                       val request = GroupRequestModel(user)
                       val result = groupRequestsDAO.addGroupRequest(request,groupModel)
                       return@withContext  result
                   }
               }
           } catch (e: Exception) {
               if (e is CancellationException) throw e
               else return@withContext OperationResult.Failure(e)
           }
       }
   }

}