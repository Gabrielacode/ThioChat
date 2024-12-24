package com.solt.thiochat.data.Groups.Messages

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.Groups.GROUP_COLLECTION
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.OperationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
const val GROUP_MESSAGES_COLLECTION = "group_messages"
class GroupMessageDAO @Inject constructor() {
    @Inject lateinit var firestore: FirebaseFirestore

    fun getGroupMessagesOfGroup(group :GroupDisplayModel): Flow<List<GroupMessageModel>>{
        val groupMessagesCollectionRef = firestore.collection(GROUP_COLLECTION).document(group.documentId).collection(
            GROUP_MESSAGES_COLLECTION)
        //We will get the flow of messages using the new snapshots method which returns a flow of the document snapshots
        val flowOfMessages = groupMessagesCollectionRef.snapshots().map { it.toObjects<GroupMessageModel>() }
        return flowOfMessages
    }
    //We will map it to the individual sub classes
   suspend fun addMessage(group:GroupDisplayModel,message :GroupMessageModel):OperationResult{
        return withContext(Dispatchers.IO){
            try {
                val groupMessagesCollectionRef = firestore.collection(GROUP_COLLECTION).document(group.documentId).collection(
                    GROUP_MESSAGES_COLLECTION)
                val task = groupMessagesCollectionRef.add(message).await()
                if (task != null){
                    OperationResult.Success("Sent")
                }
                OperationResult.Failure( IllegalStateException("Message not sent"))

            }catch (e:Exception){
                if (e is CancellationException) throw e
                else OperationResult.Failure(e)
            }
        }
    }
}