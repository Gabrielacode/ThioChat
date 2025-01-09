package com.solt.thiochat.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.Groups.GroupMemberModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageDisplayModel
import com.solt.thiochat.data.Groups.Role
import com.solt.thiochat.data.Users.UserModel
import com.solt.thiochat.databinding.GroupMembersLayoutBinding
import com.solt.thiochat.databinding.GroupProfileDialogBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

val groupMemberDiffUtil = object :DiffUtil.ItemCallback<GroupMemberModel>(){
    override fun areItemsTheSame(oldItem: GroupMemberModel, newItem: GroupMemberModel): Boolean {
        return oldItem.userId == newItem.userId
    }

    override fun areContentsTheSame(oldItem: GroupMemberModel, newItem: GroupMemberModel): Boolean {
        return oldItem == newItem
    }

}
class GroupMemberAdapter(val fragment: Fragment, val checkIfUsersAreFriends: suspend (GroupMemberModel)->Boolean, val onSendRequest:  (GroupMemberModel)->Unit, val usersAreFriends:(FriendModel)->Unit, val getUserInfo: suspend (String)-> UserModel?):ListAdapter<GroupMemberModel,GroupMemberAdapter.MemberViewHolder> (groupMemberDiffUtil){
    class MemberViewHolder(val binding: GroupMembersLayoutBinding, val fragment: Fragment, val checkIfUsersAreFriends: suspend (GroupMemberModel)->Boolean, val onSendRequest:  (GroupMemberModel)->Unit, val usersAreFriends:(FriendModel)->Unit, val getUserInfo: suspend (String)-> UserModel?): RecyclerView.ViewHolder(binding.root){
        fun bind(member:GroupMemberModel){
            binding.apply {
                memberName.text = member.userName
                val role = Role.fromString(member.role)
                if (role == Role.ADMIN) {
                    binding.adminTag.visibility = View.VISIBLE
                } else binding.adminTag.visibility = View.GONE
                //We will do the same as the groupMessagesAdapter
                val requestBinding =
                    GroupProfileDialogBinding.inflate(LayoutInflater.from(root.context))
                val bottomModalDialog = BottomSheetDialog(root.context).apply {
                    setContentView(requestBinding.root)
                }
                root.setOnClickListener {
                    //Show the dialog
                    bottomModalDialog.show()
                    fragment.viewLifecycleOwner.lifecycleScope.launch {
                        //Get the user details of the user who sent the message
                        val userDetails = async { getUserInfo(member.userId) }.await()
                        requestBinding.userName.setText(
                            userDetails?.userName ?: "User Cannot be identified"
                        )
                        requestBinding.description.setText(userDetails?.description ?: "")

                        //Check if the users are friends
                        if (checkIfUsersAreFriends(member)) {
                            //If the users are friends
                            //Show the Go to Message
                            requestBinding.messageOrRequestBtn.text = "Message Me"
                            requestBinding.messageOrRequestBtn.setOnClickListener {
                                usersAreFriends(FriendModel(member.userId, member.userName))
                                bottomModalDialog.dismiss()
                            }
                        } else {
                            //If the users are not friends
                            //Then set button to sent request
                            requestBinding.messageOrRequestBtn.text = "Send Request"
                            requestBinding.messageOrRequestBtn.setOnClickListener {
                                onSendRequest(member)
                                bottomModalDialog.dismiss()
                            }

                        }
                    }
                }

            }
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = GroupMembersLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MemberViewHolder(binding,fragment,checkIfUsersAreFriends,onSendRequest,usersAreFriends,getUserInfo)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
       holder.bind(getItem(position))
    }
}