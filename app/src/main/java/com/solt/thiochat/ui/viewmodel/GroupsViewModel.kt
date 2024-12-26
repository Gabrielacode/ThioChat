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
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(val groupsDAO: GroupDAO, val authentication: Authentication, val groupMessageDAO: GroupMessageDAO):ViewModel() {
    var selectedGroup :GroupDisplayModel? = null
    fun getGroups():Flow<List<GroupDisplayModel>>{

        return groupsDAO.getGroups()
    }
    fun getGroupsUserIsIn(onFailure: (String) -> Unit):Flow<List<GroupDisplayModel>>?{
        val userModel = authentication.getCurrentUserAsModel()
        if(userModel == null){
            onFailure("Is User Signed In?")
            return null
        }
        return groupsDAO.getGroupsUserIsAMember(userModel)
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
        if (currentUserModel == null){
            onFailure("Is User Signed In?")
            return null
        }
        if (selectedGroup  == null) return null
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
}