package com.solt.thiochat.data

import android.content.Context
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.solt.thiochat.data.Friends.FRIENDS_COLLECTION

import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.Users.USERS_COLLECTION


import com.solt.thiochat.data.Users.UserModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Authentication  @Inject constructor( ) {
    private val currentUser : FirebaseUser?
        get() = auth.currentUser
    private val currentUserModel:UserModel?
        get() {
            return if(currentUser == null) null
            else UserModel(currentUser!!.uid,currentUser!!.displayName?:"Random User ${currentUser!!.uid.substring(0..4)}")
        }
    val auth = Firebase.auth
    @Inject lateinit var fireStore:FirebaseFirestore



    fun isUserSigned():Boolean{
        Log.i("Error","Current User ${currentUser.toString()}")
     return currentUser != null
    }
    fun getCurrentUserDetails():FirebaseUser?{
        return currentUser
    }
    fun getCurrentUserAsModel():UserModel?{
        return currentUserModel
    }
    fun isUserVerified():Boolean{
       return (isUserSigned() && currentUser?.isEmailVerified == true)
    }
    //Update add the user to the database as the user and as a friend to itself
     suspend fun onNewUser(userId:String , userName:String?):OperationResult{
       return  try {
             val userReference = fireStore.collection(USERS_COLLECTION).document(userId)
             val friendsReference = userReference.collection(FRIENDS_COLLECTION).document(userId)
           fireStore.runBatch {
                 it.set(
                     userReference,
                     UserModel(userId, userName ?: "Random User ${userId.substring(0..4)}")
                 )
                 it.set(friendsReference, FriendModel(userId, "You"))
             }.await()
           OperationResult.Success("Successfully added user")

         }catch (e:FirebaseFirestoreException) {OperationResult.Failure(e)}
     }
    suspend fun signUpWithEmailAndPassword(email:String,password:String):OperationResult{
        return try {
            val result  =  auth.createUserWithEmailAndPassword(email,password).await()

           if(result.user !=null) {
                val isUserAddedBefore = result.additionalUserInfo?.isNewUser
                if (isUserAddedBefore == true) {
                     onNewUser(result.user!!.uid,result.user!!.displayName)
                } else {
                    OperationResult.Failure(IllegalStateException("User couldnt sign in"))
                }
            }else OperationResult.Failure(IllegalStateException("User couldn't sign in , Try again"))
        }catch (e:Exception){
            if (e is CancellationException) throw e
            OperationResult.Failure(e)
        }
    }
   suspend fun signInWithEmailAndPassword(email:String,password:String):OperationResult{
      return withContext(Dispatchers.IO) {
          try {
              val result = auth.signInWithEmailAndPassword(email, password).await()
              if (result.user != null) {
                  OperationResult.Success("User successfully signed in ")
              } else OperationResult.Failure(IllegalStateException("User couldnt sign in"))
          } catch (e: Exception) {
              if (e is CancellationException) throw e
              OperationResult.Failure(e)
          }
      }
       }
   suspend fun signInWithGoogle(context: Context):OperationResult{
       return withContext(Dispatchers.IO) {
           try {
               val credentialResult = getGoogleCredentialIdTokenFromUser(context)
               when (credentialResult) {
                   is OperationResult.Failure -> credentialResult
                   is OperationResult.Loading -> credentialResult
                   is OperationResult.Success<*> -> {
                       val credentialData = credentialResult.data as GoogleIdTokenCredential
                       val firebaseGoogleAuthCredential =
                           GoogleAuthProvider.getCredential(credentialData.idToken, null)
                       val result = auth.signInWithCredential(firebaseGoogleAuthCredential).await()
                       if (result.user != null) {
                           val isUserAddedBefore = result.additionalUserInfo?.isNewUser
                           if (isUserAddedBefore == true) {
                               onNewUser(result.user!!.uid, result.user!!.displayName)
                           } else {
                               OperationResult.Success("User is successfully signed up")
                           }
                       } else OperationResult.Failure(IllegalStateException("User couldn't sign in , Try again"))
                   }

               }
           }catch (e:Exception){
               if(e is CancellationException) throw e
               else return@withContext OperationResult.Failure(e)
           }
       }
   }
    suspend fun getGoogleCredentialIdTokenFromUser( context: Context) :OperationResult {
        return withContext(Dispatchers.Main) {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOptionRequest = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .setServerClientId("873174742298-n7r5ca7maqk7mrlf6dkfvvtu649ufrap.apps.googleusercontent.com")
                    .build()
                val request =
                    GetCredentialRequest.Builder().addCredentialOption(googleIdOptionRequest)
                        .build()
                val credentialResponse = credentialManager.getCredential(context, request)
                if (credentialResponse.credential is CustomCredential && credentialResponse.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken =
                        GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)
                    OperationResult.Success(googleIdToken)
                } else {
                    OperationResult.Failure(IllegalStateException("This type of credential is not required "))
                }
            } catch (e: CancellationException) {
                Log.i("Error",e.toString())
                throw e
            }catch (e:Exception){
                Log.i("Error",e.toString())

                OperationResult.Failure(e)
            }

        }
    }


}