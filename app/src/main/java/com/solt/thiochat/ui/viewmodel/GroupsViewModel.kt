package com.solt.thiochat.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.Groups.GroupDAO
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.Groups.GroupInfoModel
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(val groupsDAO: GroupDAO, val authentication: Authentication):ViewModel() {

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
}