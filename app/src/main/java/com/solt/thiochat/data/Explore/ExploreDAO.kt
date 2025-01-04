package com.solt.thiochat.data.Explore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.solt.thiochat.data.Groups.GROUP_COLLECTION
import com.solt.thiochat.data.Groups.GROUP_MEMBERS_COLLECTION
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.Groups.GroupInfoModel
import com.solt.thiochat.data.Groups.GroupMemberModel
import com.solt.thiochat.data.Groups.USER_GROUP_COLLECTION
import com.solt.thiochat.data.Users.USERS_COLLECTION
import com.solt.thiochat.data.Users.UserModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExploreDAO @Inject constructor() {
    @Inject lateinit var firestore: FirebaseFirestore

    fun getGroupsUserIsNotIn(user: UserModel): Flow<List<GroupDisplayModel>> {

        //This will be based on two flows
        // We will get the list of groups the user is in
        //Then get the list of groups that are
        //Then combine them and find out which groups are not in the user groups

        val userGroupsCollection = firestore.collection(USERS_COLLECTION).document(user.userId).collection(
            USER_GROUP_COLLECTION
        )
        val groupCollection = firestore.collection(GROUP_COLLECTION)
        val flowOfGroupsUserIsIn = userGroupsCollection.snapshots().map {
            val documents  = it.documents.mapNotNull {doc ->
                val documentId = doc.id
                val documentModel = doc.toObject<GroupInfoModel>()
                if (documentModel == null) null
                else GroupDisplayModel(documentId,documentModel.groupName,documentModel.groupColour,documentModel.modeOfAcceptance)
            }
            documents
        }
        val flowOfGroups = groupCollection.snapshots().map {
            val documents  = it.documents.mapNotNull {doc ->
                val documentId = doc.id
                val documentModel = doc.toObject<GroupInfoModel>()
                if (documentModel == null) null
                else GroupDisplayModel(documentId,documentModel.groupName,documentModel.groupColour,documentModel.modeOfAcceptance)
            }
            documents
        }
        val   flowOfGroupsUserNotIn = flowOfGroups.combine(flowOfGroupsUserIsIn){ allGroups,userInGroups->
            allGroups.subtract(userInGroups).toList()
        }
        return  flowOfGroupsUserNotIn
    }
}