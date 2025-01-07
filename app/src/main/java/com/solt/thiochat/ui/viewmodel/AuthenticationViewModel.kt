package com.solt.thiochat.ui.viewmodel

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseUser
import com.solt.thiochat.R
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.OperationResult
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(val authService :Authentication): ViewModel() {

fun checkIfUserIsAuthenticated(action:()->Unit):Boolean{
    if(authService.isUserSigned()){
       action()
    }
    return authService.isUserSigned()
}


    fun signInWithEmailAndPassword(email: String,password:String,onSuccess: (String) -> Unit,onFailure :(String)->Unit){
        viewModelScope.launch {
            val result = authService.signInWithEmailAndPassword(email,password)
            withContext(Dispatchers.Main) {
                when (result) {
                    is OperationResult.Failure -> onFailure(result.e.message?:"Error Unknown")
                    is OperationResult.Loading -> {}
                    is OperationResult.Success<*> -> {
                        onSuccess(result.data.toString())
                    }
                }
            }
        }
    }
    fun signUpWithEmailAndAddress(email: String,password:String,onSuccess: (String) -> Unit,onFailure :(String)->Unit){
        viewModelScope.launch {
            val result = authService.signUpWithEmailAndPassword(email,password)
            withContext(Dispatchers.Main) {
                when (result) {
                    is OperationResult.Failure -> onFailure(result.e.message?:"Error Unknown")
                    is OperationResult.Loading -> {}
                    is OperationResult.Success<*> -> {
                        onSuccess(result.data.toString())
                    }
                }
            }
        }
        }
    fun signInWithGoogle(context: Context, onSuccess: (String) -> Unit,onFailure :(String)->Unit){
        viewModelScope.launch {
            val result = authService.signInWithGoogle(context)
            when(result){
                is OperationResult.Failure -> {onFailure(result.e.message?:"Error Unknown")}
                is OperationResult.Loading -> {}
                is OperationResult.Success<*> -> {
                    onSuccess(result.data.toString())
                }
            }
        }
    }
    fun signOut() = authService.signOut()
}