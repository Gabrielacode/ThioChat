package com.solt.thiochat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.Friends.FriendsDao
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserDAO
import com.solt.thiochat.data.Users.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(val userDao:UserDAO, val authentication: Authentication,val friendsDao: FriendsDao):ViewModel() {

    fun getUserDetails(onSuccess :(UserModel)->Unit,onFailure:(String)->Unit){
        viewModelScope.launch {
            val userId = authentication.getCurrentUserDetails()?.uid
            if (userId == null){
                onFailure("No Id for User , Is User Logged In ?")
                return@launch
            }
            when(val userDetail = userDao.getCurrentSignedInUserDetails(userId)){
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
  suspend  fun getFriends(onFailure: (String) -> Unit): Flow<List<FriendModel>>?{
        val userId = authentication.getCurrentUserDetails()?.uid
       return if (userId == null){
            onFailure("Is User signed in?")
           null
        }else{
            friendsDao.getFriends(userId)
        }
    }
}