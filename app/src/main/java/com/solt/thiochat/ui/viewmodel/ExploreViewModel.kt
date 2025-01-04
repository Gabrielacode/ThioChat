package com.solt.thiochat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solt.thiochat.data.Authentication
import com.solt.thiochat.data.Explore.ExploreDAO
import com.solt.thiochat.data.Groups.GroupDAO
import com.solt.thiochat.data.Groups.GroupDisplayModel
import com.solt.thiochat.data.OperationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cache
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ExploreViewModel@Inject constructor( val authentication: Authentication,val exploreDAO: ExploreDAO, val groupsDAO: GroupDAO):ViewModel() {
    fun getListOfGroupsForExplore(onFailure:(String)->Unit):Flow<List<GroupDisplayModel>>?{
        val userModel = authentication.getCurrentUserAsModel()
        if (userModel == null){
           onFailure("Is User signed In ?" )
            return null
        }

        val  flowOfGroups = exploreDAO.getGroupsUserIsNotIn(userModel).catch { onFailure(it.message?:"Error") }
        return flowOfGroups
    }
    fun joinGroup( group: GroupDisplayModel,onSuccess:(String)->Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val userModel = authentication.getCurrentUserAsModel()

            if (userModel == null) {
                onFailure("Is User signed In ?")
                return@launch
            }
             val result = groupsDAO.addUserToGroup(userModel, group)
            when(result){
                is OperationResult.Failure -> onFailure(result.e.message?:"Error")
                is OperationResult.Loading -> {}
                is OperationResult.Success<*> -> onSuccess(result.data.toString())
            }

        }
    }
}