package com.solt.thiochat.data.Explore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.solt.thiochat.data.Groups.GROUP_COLLECTION
import com.solt.thiochat.data.Groups.GROUP_MEMBERS_COLLECTION
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.Groups.GroupInfoModel
import com.solt.thiochat.data.Groups.GroupMemberModel
import com.solt.thiochat.data.Users.UserModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExploreDAO @Inject constructor() {
    @Inject lateinit var firestore: FirebaseFirestore

    fun getGroupsUserIsNotIn(user: UserModel): Flow<List<GroupDisplayModel>> {
        return callbackFlow {
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            val groupCollectionObserver = firestore.collection(GROUP_COLLECTION)
                .addSnapshotListener { value, error ->
                    if(error != null){
                        return@addSnapshotListener
                    }

                    //Groups will be accessible to everybody

                        coroutineScope.launch {
                            try {
                                if (isActive) {
                                    val listOfGroupsUserNotIn = ArrayList<GroupDisplayModel>()
                                    //Groups will be accessible to everybody
                                    for (i in value!!) {
                                        val documentId = i.id
                                        val documentInfoModel = i.toObject<GroupInfoModel>()
                                        //Search if there is a member collection of the user
                                        val getUserTask =
                                            firestore.collection(GROUP_COLLECTION).document(i.id)
                                                .collection(
                                                    GROUP_MEMBERS_COLLECTION
                                                ).document(user.userId).get().await()
                                        if (!getUserTask.exists() || getUserTask == null) {
                                            listOfGroupsUserNotIn.add(
                                                GroupDisplayModel(
                                                    documentId,
                                                    documentInfoModel.groupName,
                                                    documentInfoModel.groupColour,
                                                    documentInfoModel.modeOfAcceptance
                                                )
                                            )
                                        }
                                    }
                                    //Send it
                                    trySend(listOfGroupsUserNotIn)
                                }
                            }catch (e:Exception){
                                if (e is CancellationException) throw e
                            }


                    }

                }
            awaitClose {
                coroutineScope.cancel()
                groupCollectionObserver.remove() }
        }
    }
}