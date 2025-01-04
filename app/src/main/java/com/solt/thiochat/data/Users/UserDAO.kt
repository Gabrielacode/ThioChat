package com.solt.thiochat.data.Users

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.OperationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
const val USERS_COLLECTION = "users"
class UserDAO @Inject constructor(){
    @Inject lateinit var fireStore:FirebaseFirestore


    suspend fun addUser(userId:String,userName:String?):Boolean {
      return  withContext(Dispatchers.IO) {
          try {

              val user = if(userName == null)UserModel(userId)else UserModel(userId,userName)
              val result = fireStore.collection(USERS_COLLECTION).document(userId).set(user).await()
             true
          }catch (e:Exception){
              if(e is CancellationException) throw e
              else false
          }

        }
    }

    suspend fun getCurrentSignedInUserDetails(userId:String):OperationResult{
        return withContext(Dispatchers.IO){
            try {

                val result = fireStore.collection(USERS_COLLECTION).document(userId).get().await()

                val user = result.toObject<UserModel>()

                if(user == null){
                    OperationResult.Failure(IllegalStateException("No users match signed in user id"))
                }else {
                    OperationResult.Success(user)
                }
            }catch (e:Exception) {
            if(e is CancellationException) throw e
            else OperationResult.Failure(e)}

        }

    }
     fun searchUserByName(name:String): Flow<List<UserModel>>{
         val userCollection = fireStore.collection(USERS_COLLECTION)
         val query = userCollection.whereEqualTo("userName",name)
         val flowOfUser = query.snapshots().flowOn(Dispatchers.IO).map {
             it.toObjects<UserModel>()
         }
    return flowOfUser
     }
}