package com.solt.thiochat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserDAO
import com.solt.thiochat.data.Users.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor( val authentication: Authentication,val userDAO :UserDAO): ViewModel() {
    fun getUserDetails(onSuccess :(UserModel)->Unit, onFailure:(String)->Unit){
        viewModelScope.launch {
            val userId = authentication.getCurrentUserDetails()?.uid
            if (userId == null){
                onFailure("No Id for User , Is User Logged In ?")
                return@launch
            }
            when(val userDetail = userDAO.getUserDetailsById(userId)){
                is OperationResult.Failure -> onFailure(userDetail.e.message?:"Error")
                is OperationResult.Loading -> {}
                is OperationResult.Success<*> -> {
                    if (userDetail.data is UserModel){
                        onSuccess(userDetail.data)
                    }
                }
            }
        }
    }
    fun updateUserDetails(name:String,description:String,onSuccess :(String)->Unit, onFailure:(String)->Unit) {
        viewModelScope.launch {
            val userModel = authentication.getCurrentUserAsModel()
            if (userModel == null) {
                onFailure("Is User Signed In ")
                return@launch
            }
            val result = userDAO.updateUserNameAndDescription(userModel.userId, name, description)
            when(result){
                is OperationResult.Failure -> onFailure(result.e.message?:"Error")
                is OperationResult.Loading -> {}
                is OperationResult.Success<*> -> onSuccess(result.data.toString())
            }


        }
    }
}