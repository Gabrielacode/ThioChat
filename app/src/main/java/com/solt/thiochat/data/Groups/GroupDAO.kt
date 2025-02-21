package com.solt.thiochat.data.Groups

import android.util.Log
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.Groups.Request.GROUP_REQUEST_COLLECTION
import com.solt.thiochat.data.Groups.Request.GroupRequestModel
import com.solt.thiochat.data.Groups.Request.GroupRequestsDAO
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.USERS_COLLECTION
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
const val GROUP_COLLECTION = "groups"
const val GROUP_MEMBERS_COLLECTION = "group_members"
const val USER_GROUP_COLLECTION = "user_groups"
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
                    withContext(Dispatchers.Main.immediate){
                        onFailure(e.message ?: "Error")
                    }
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
            firestore.runBatch {
                //Add the group
               val groupDocRef = groupCollection.document()
                it.set(groupDocRef,groupInfoModel)
                //Add the user as the admin
                val member = GroupMemberModel(creator.userId,creator.userName,creator.description, Role.ADMIN.toString())
                val creatorDocRef = groupDocRef.collection(GROUP_MEMBERS_COLLECTION).document(creator.userId)
                it.set(creatorDocRef,member)
                //We also need to add the group to the users list of groups
                val userGroupsCollection = firestore.collection(USERS_COLLECTION).document(creator.userId)
                    .collection(USER_GROUP_COLLECTION)
                val groupDoc = userGroupsCollection.document(groupDocRef.id)
                it.set(groupDoc,groupInfoModel)

            }.await()
         return@withContext OperationResult.Success("Successfully created group")
            
        }catch (e:Exception){
            if(e is CancellationException) throw e
            else  return@withContext OperationResult.Failure(e)
        }
    }
}
    fun getGroups():Flow<List<GroupDisplayModel>>{


            val groupCollectionRef = firestore.collection(GROUP_COLLECTION)
        val flowOfGroups = groupCollectionRef.snapshots().transform {
            val listOfGroups = ArrayList<GroupDisplayModel>()
            //Groups will be accessible to everybody
            for(i in it){

                val documentId = i.id
                val documentInfoModel = i.toObject<GroupInfoModel>()
                listOfGroups.add(GroupDisplayModel(documentId,documentInfoModel.groupName,documentInfoModel.groupColour,documentInfoModel.modeOfAcceptance))
            }
            emit(listOfGroups)
    }
        return flowOfGroups
    }
    fun getGroupsUserIsAMember(user:UserModel):Flow<List<GroupDisplayModel>>{

        //This is another try to improve the performance now
        //Here each user has a group subcollection that contains all groups the user is currently in
        val userGroupsCollection = firestore.collection(USERS_COLLECTION).document(user.userId).collection(
            USER_GROUP_COLLECTION)
        val flowOfGroups =  userGroupsCollection.snapshots().map {
            val listOfGroups = it.documents.mapNotNull {doc ->
                val documentId = doc.id
                val documentModel = doc.toObject<GroupInfoModel>()
                if (documentModel == null) null
                 else GroupDisplayModel(documentId,documentModel.groupName,documentModel.groupColour,documentModel.modeOfAcceptance)
            }
            listOfGroups
        }
        return flowOfGroups
           }
   fun getMembersOfAGroup(groupDisplayModel: GroupDisplayModel):Flow<List<GroupMemberModel>>{
       val groupMembersCollection = firestore.collection(GROUP_COLLECTION).document(groupDisplayModel.documentId).collection(
           GROUP_MEMBERS_COLLECTION)
       val flowOfMembers = groupMembersCollection.snapshots().map {
           it.toObjects<GroupMemberModel>()
       }
       return flowOfMembers
    }
   suspend fun getCountOfMembers(groupModel: GroupDisplayModel):OperationResult{
       return withContext(Dispatchers.IO) {
           try {
               val groupMembersCollection =
                   firestore.collection(GROUP_COLLECTION).document(groupModel.documentId)
                       .collection(
                           GROUP_MEMBERS_COLLECTION
                       )
               val countQuery = groupMembersCollection.count()
               val result = countQuery.get(AggregateSource.SERVER).await()
               OperationResult.Success(result.get(AggregateField.count()))
           }catch (e:Exception){
               if (e is CancellationException) throw e
               else OperationResult.Failure(e)
           }
       }
    }

   suspend fun addUserToGroup(user :UserModel, groupModel: GroupDisplayModel):OperationResult {
       //Do we need to update it to contain the invitations or make a separate method
       //For now i will update it
       return withContext(Dispatchers.IO) {
           try {
               val groupDocRef = firestore.collection(GROUP_COLLECTION).document(groupModel.documentId)
               val groupMemberCollection = groupDocRef
                       .collection(GROUP_MEMBERS_COLLECTION)
               val userGroupsCollection = firestore.collection(USERS_COLLECTION).document(user.userId)
                   .collection(USER_GROUP_COLLECTION)
               //Check if the group mode of acceptance is NONE or REQUEST
               val mode = ModeOfAcceptance.fromString(groupModel.modeOfAcceptance)
               if (mode == null) return@withContext OperationResult.Failure(IllegalStateException("Couldn't determine mode of acceptance"))
               when(mode){
                   ModeOfAcceptance.NONE ->{
                       //Just add the user directly
                       val member = GroupMemberModel(user.userId,user.userName,user.description,Role.MEMBER.toString())
                       val memberDocRef  = groupMemberCollection.document(member.userId)
                       val userGroupRef = userGroupsCollection.document(groupModel.documentId)
                       val group = GroupInfoModel(groupModel.groupName,groupModel.groupColour,groupModel.modeOfAcceptance)

                       firestore.runBatch {
                           //Write to the groupmember collection
                           it.set(userGroupRef,group)
                           it.set(memberDocRef,member)

                       }
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
    fun searchGroupUserInByName( user:UserModel , name:String):Flow<List<GroupDisplayModel>>{
        val groupCollection = firestore.collection(GROUP_COLLECTION)
        val userGroupsCollection = firestore.collection(USERS_COLLECTION).document(user.userId).collection(
            USER_GROUP_COLLECTION)
        //Thois is the same method used in the friend dao
        val query = userGroupsCollection.whereGreaterThanOrEqualTo("groupName",name)
            .whereLessThan("groupName",name+"z")
        val flowOfGroups = query.snapshots().map {
            val listOfGroups = it.documents.mapNotNull {doc ->
                val documentId = doc.id
                val documentModel = doc.toObject<GroupInfoModel>()
                if (documentModel == null) null
                else GroupDisplayModel(documentId,documentModel.groupName,documentModel.groupColour,documentModel.modeOfAcceptance)
            }
            listOfGroups
        }
        return flowOfGroups
    }

    suspend fun leaveGroup(user: UserModel, groupModel: GroupDisplayModel):OperationResult{
        //To leave the user must be removed from the members collection of the group
        //And the group be removed from the user's group collection
       return withContext(Dispatchers.IO){
           try {
               val userGroupCollection =
                   firestore.collection(USERS_COLLECTION).document(user.userId).collection(
                       USER_GROUP_COLLECTION
                   )
               val groupMemberCollection =
                   firestore.collection(GROUP_COLLECTION).document(groupModel.documentId).collection(
                       GROUP_MEMBERS_COLLECTION
                   )
               firestore.runBatch {
                   it.delete(groupMemberCollection.document(user.userId))
                   it.delete(userGroupCollection.document(groupModel.documentId))
               }
               OperationResult.Success("Successfully Left Group")
           }catch (e:Exception){
               if (e is CancellationException) throw  e
               else OperationResult.Failure(e)
           }
       }
    }
}