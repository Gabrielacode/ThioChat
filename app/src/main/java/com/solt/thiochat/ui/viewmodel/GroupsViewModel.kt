package com.solt.thiochat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.Groups.GroupDAO
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.Groups.GroupInfoModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageDAO
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.data.Groups.ModeOfAcceptance
import com.solt.thiochat.data.Groups.Request.GroupRequestDisplayModel
import com.solt.thiochat.data.Groups.Request.GroupRequestModel
import com.solt.thiochat.data.Groups.Request.GroupRequestsDAO
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(val groupsDAO: GroupDAO, val authentication: Authentication, val groupMessageDAO: GroupMessageDAO, val groupRequestsDAO: GroupRequestsDAO):ViewModel() {
    var selectedGroup :GroupDisplayModel? = null

    fun getGroupsUserIsIn(onFailure: (String) -> Unit):Flow<List<GroupDisplayModel>>?{
        val userModel = authentication.getCurrentUserAsModel()
        if(userModel == null){
            onFailure("Is User Signed In?")
            return null
        }
        return groupsDAO.getGroupsUserIsAMember(userModel).catch { onFailure(it.message?:"Error") }
    }
    fun addGroup(groupInfo : GroupInfoModel, onSuccess:(String)->Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
            val userModel = authentication.getCurrentUserAsModel()
            if(userModel == null){
                onFailure("Is User Signed In?")
                return@launch
            }
            val result = groupsDAO.createGroup(userModel,groupInfo)
           when(result ){
               is OperationResult.Failure -> onFailure(result.e.message?:"Error")
               is OperationResult.Loading -> {}
               is OperationResult.Success<*> -> onSuccess(result.data.toString())
           }
        }
    }

    fun getGroupsMessagesOfSelectedGroup(onFailure: (String) -> Unit):Flow<List< GroupMessageModel>>?{
        val currentUserModel = authentication.getCurrentUserAsModel()
        if (currentUserModel == null || selectedGroup == null){
            onFailure("Is User Signed In?")
            return null
        }

        val flowOfMessagesSorted = groupMessageDAO.getGroupMessagesOfGroup(selectedGroup!!)
            .map{ it.map { message->
                if(message.userId == currentUserModel.userId)  message.toUserMessage()
                else message.toNonUserMessage()
            } }
        return flowOfMessagesSorted
    }
    fun sendMessageToGroup(message:String){
        viewModelScope.launch {

            val currentUserModel = authentication.getCurrentUserAsModel() ?: return@launch
            val groupMessage =
                GroupMessageModel(currentUserModel.userId, currentUserModel.userName, message)

            groupMessageDAO.addMessage(selectedGroup ?: return@launch, groupMessage)
        }
    }
   suspend fun checkIfRequestAreAccessible(onFailure: (String) -> Unit):Boolean{
        //Here we will check if the group mode of acceptance is Request and if the user is an admin
        val userModel = authentication.getCurrentUserAsModel()
        if (userModel == null || selectedGroup == null){
            onFailure("Error Group Not Selected ")
            return false
        }
        val isGroupModeOfAcceptanceRequest = ModeOfAcceptance.fromString(selectedGroup?.modeOfAcceptance) == ModeOfAcceptance.REQUEST
        val isUserAdmin = groupsDAO.checkIfUserIsAdmin(userModel,selectedGroup!!,onFailure)
        return isGroupModeOfAcceptanceRequest && isUserAdmin

    }
    fun getGroupRequests(onFailure: (String) -> Unit):Flow<List<GroupRequestDisplayModel>>? {
        if (selectedGroup == null) {
            onFailure("No group info")
            return null
        }
        return groupRequestsDAO.displayRequestsForGroup(selectedGroup!!).catch { onFailure(it.message?:"Error") }
    }
   fun acceptGroupRequest(groupRequest: GroupRequestDisplayModel, onSuccess: (String) -> Unit, onFailure: (String) -> Unit){
      viewModelScope.launch {
          if (selectedGroup == null){
              onFailure("Error Group Not Selected ")
              return@launch
          }
          val result = groupRequestsDAO.acceptRequest(groupRequest,selectedGroup!!)
          when(result){
              is OperationResult.Failure -> onFailure(result.e.message?:"Error")
              is OperationResult.Loading -> {}
              is OperationResult.Success<*> -> onSuccess(result.data.toString())
          }
      }

   }
    fun  rejectGroupRequest(groupRequest: GroupRequestDisplayModel,onSuccess: (String) -> Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
            if (selectedGroup == null){
                onFailure("Error Group Not Selected ")
                return@launch
            }
            val result = groupRequestsDAO.rejectRequest(groupRequest,selectedGroup!!)
            when(result){
                is OperationResult.Failure -> onFailure(result.e.message?:"Error")
                is OperationResult.Loading -> {}
                is OperationResult.Success<*> -> onSuccess(result.data.toString())
            }
        }
    }

}