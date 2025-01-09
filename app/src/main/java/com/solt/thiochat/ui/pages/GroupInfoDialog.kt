package com.solt.thiochat.ui.pages

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.Groups.GroupMemberModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageDisplayModel
import com.solt.thiochat.data.OperationResult
import com.solt.thiochat.data.Users.UserModel
import com.solt.thiochat.databinding.GroupInfoDialogBinding
import com.solt.thiochat.ui.adapters.GroupMemberAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import com.solt.thiochat.ui.viewmodel.GroupsViewModel
import com.solt.thiochat.ui.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupInfoDialog:BottomSheetDialogFragment() {
    lateinit var binding:GroupInfoDialogBinding
    val groupViewModel by hiltNavGraphViewModels<GroupsViewModel>(R.id.app_nav_graph)
    val friendViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)
    val userViewModel by hiltNavGraphViewModels<UserViewModel>(R.id.app_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = GroupInfoDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        //Get the groupName
        //Get the group Colour
        if (groupViewModel.selectedGroup ==  null){
            findNavController().popBackStack()
            return
        }else{
            binding.groupName.text = groupViewModel.selectedGroup!!.groupName

            val color = try {Color.parseColor("#${groupViewModel.selectedGroup!!.groupColour}") }catch (e:Exception){
                Color.LTGRAY
            }
            val textColor = {
                val luminance = ColorUtils.calculateLuminance(color)
                if (luminance<0.45) Color.LTGRAY else Color.DKGRAY }
            binding.groupName.setTextColor(textColor())
            binding.groupName.setBackgroundColor(color)
            //Fetch the count of members
            groupViewModel.getNoOfMembersInCurrentGroup({binding.noOfMembers.text = it}){binding.noOfMembers.text = "$it Members"}
            val  membersAdapter = GroupMemberAdapter(this,::checkIfUsersFriends,::sendRequest,::userAreFriends){
                val result =  userViewModel.userDAO.getUserDetailsById(it)
                when(result){
                    is OperationResult.Failure -> null
                    is OperationResult.Loading -> null
                    is OperationResult.Success<*> -> {
                        val userModel = result.data as? UserModel
                        userModel
                    }
                }
            }
            binding.listOfMembers.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = membersAdapter
            }
            binding.requestsBtn.setOnClickListener {
                findNavController().navigate(R.id.action_groupInfoDialog_to_groupRequestsPage)
            }
            //Check if the user is an admin then show requests button
            viewLifecycleOwner.lifecycleScope.launch {
                val isRequestsAccessible = groupViewModel.checkIfRequestAreAccessible { activity.showMessageFailure(it) }
                binding.requestsBtn.visibility = if (isRequestsAccessible) View.VISIBLE else View.GONE

            }
            viewLifecycleOwner.lifecycleScope.launch {
                groupViewModel.getMembersOfCurrentGroup()?.collectLatest {
                    membersAdapter.submitList(it)
                }
            }

            binding.leaveGroupBtn.setOnClickListener {
                groupViewModel.leaveCurrentGroup({activity.showMessageFailure(it)}){
                    activity.showMessageSuccess(it)
                    findNavController().popBackStack(R.id.groupsPage,false)
                }
            }
        }

    }
    suspend fun checkIfUsersFriends(member:GroupMemberModel):Boolean{
        val activity  = requireActivity() as MainActivity
        val messageAsFriend = FriendModel(member.userId,member.userName)
        return friendViewModel.checkIfTwoUsersAreFriends(messageAsFriend){activity.showMessageFailure(it)}
    }

    fun sendRequest(member: GroupMemberModel){
        val activity  = requireActivity() as MainActivity
        val messageAsFriend = FriendModel(member.userId,member.userName)
        friendViewModel.sendFriendRequest(messageAsFriend,{activity.showMessageSuccess(it)}){activity.showMessageFailure(it)}
    }
    fun userAreFriends(friend: FriendModel){
        friendViewModel.selectedFriend = friend
        findNavController().navigate(R.id.action_groupInfoDialog_to_friendMessagePage)
    }
}