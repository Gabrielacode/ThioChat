package com.solt.thiochat.data.Users

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.OperationResult
import kotlinx.coroutines.Dispatchers
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
              Log.i("Error",e.toString())
              false
          }

        }
    }

    suspend fun getCurrentSignedInUserDetails(userId:String):OperationResult{
        return withContext(Dispatchers.IO){
            try {
                Log.i("Errorr",userId.toString())
                val result = fireStore.collection(USERS_COLLECTION).document(userId).get().await()
                Log.i("Errorr",result.toString())
                val user = result.toObject<UserModel>()

                if(user == null){
                    OperationResult.Failure(IllegalStateException("No users match signed in user id"))
                }else {
                    OperationResult.Success(user)
                }
            }catch (e:Exception) {
                Log.i("Errorr",e.toString())
                OperationResult.Failure(e)}

        }

    }
}