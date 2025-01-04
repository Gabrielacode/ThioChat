package com.solt.thiochat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.Friends.FriendsDao
import com.solt.thiochat.data.Friends.Messages.FriendMessageModel
import com.solt.thiochat.data.Friends.Messages.FriendMessagesDAO
import com.solt.thiochat.data.Friends.Requests.FriendRequestDAO
import com.solt.thiochat.data.Friends.Requests.FriendRequestDisplayModel
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserDAO
import com.solt.thiochat.data.Users.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(val userDao:UserDAO, val authentication: Authentication,val friendsDao: FriendsDao,val friendRequestDAO: FriendRequestDAO, val friendMessages:FriendMessagesDAO):ViewModel() {
   var selectedFriend :FriendModel? = null


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
    suspend fun getFriends(onFailure: (String) -> Unit): Flow<List<FriendModel>>? {
        val userId = authentication.getCurrentUserDetails()?.uid
        return if (userId == null){
          null
        }else{
           friendsDao.getFriends(userId)
        }
    }
     fun searchFriends(name:String):Flow<List<FriendModel>>?{
         val userModel = authentication.getCurrentUserAsModel()
         return if (userModel == null ){
              null
         }else friendsDao.searchFriendByName(userModel,name)
     }


    suspend fun checkIfTwoUsersAreFriends( friend:FriendModel,onFailure: (String) -> Unit):Boolean{
        val userModel = authentication.getCurrentUserAsModel()
        if (userModel == null ){
            onFailure("Is User Signed In ")
            return false
        }
       return friendsDao.areTwoOfYouFriends(userModel,friend,onFailure)
    }
    fun sendFriendRequest(friend: FriendModel,onSuccess: (String) -> Unit,onFailure: (String) -> Unit){
        viewModelScope.launch {
        val userModel = authentication.getCurrentUserAsModel()
        if (userModel == null ){
            onFailure("Is User Signed In ")
        }
        val result = friendRequestDAO.addFriendRequest(userModel!!,friend)
        when(result){
            is OperationResult.Failure -> onFailure(result.e.message?:"Error")
            is OperationResult.Loading -> {}
            is OperationResult.Success<*> -> onSuccess(result.data.toString())
        }
    }}
    fun acceptFriendRequest(friendRequest:FriendRequestDisplayModel, onSuccess: (String) -> Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
            val userModel = authentication.getCurrentUserAsModel()
            if (userModel == null) {
                onFailure("Is User Signed In ")
            }
         val result =   friendRequestDAO.acceptFriendRequest(friendRequest,userModel!!)
            when(result){
                is OperationResult.Failure -> onFailure(result.e.message?:"Error")
                is OperationResult.Loading -> {}
                is OperationResult.Success<*> -> onSuccess(result.data.toString())
            }
        }
    }
    fun rejectFriendRequest(friendRequest: FriendRequestDisplayModel, onSuccess: (String) -> Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
            val userModel = authentication.getCurrentUserAsModel()
            if (userModel == null) {
                onFailure("Is User Signed In ")
            }
            val result =   friendRequestDAO.rejectFriendRequest(friendRequest,userModel!!)
            when(result){
                is OperationResult.Failure -> onFailure(result.e.message?:"Error")
                is OperationResult.Loading -> {}
                is OperationResult.Success<*> -> onSuccess(result.data.toString())
            }
        }
    }

    fun getFriendRequest(onFailure: (String) -> Unit): Flow<List<FriendRequestDisplayModel>>?{
        val userModel = authentication.getCurrentUserAsModel()
        return if (userModel == null){
            onFailure("Is User signed in?")
            null
        }else{
            friendRequestDAO.displayFriendRequestsForUser(userModel)
        }
    }
    fun getMessagesWithCurrentFriend (onFailure: (String) -> Unit):Flow<List<FriendMessageModel>>?{
        val userModel = authentication.getCurrentUserAsModel()
        if (userModel == null||selectedFriend == null){
            onFailure("Couldn't get Messages")
            return null
        }
        val messagesSorted = friendMessages.getMessagesWithFriend(userModel, selectedFriend!!)
            .map {
                it.map {
                    when(it.userId){
                        userModel.userId -> it.toUserMessage()
                        selectedFriend!!.userId ->it.toFriendMessage()
                        else -> throw IllegalStateException("THe message is not meant to be here")
                    }
                }
            }.catch { onFailure(it.message?:"Error") }
return messagesSorted

    }
    fun sendMessage(message:String){
        viewModelScope.launch {
            //For now we just return if they are null
            val userModel = authentication.getCurrentUserAsModel()?:return@launch
            val friend = selectedFriend?:return@launch
            //The user id will be of the user as the user is the one sending the message
            friendMessages.sendMessageToFriend(userModel,friend,
                FriendMessageModel(userModel.userId,userModel.userName,message)
            )

        }
    }


}