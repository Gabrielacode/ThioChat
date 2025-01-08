package com.solt.thiochat.ui.pages

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageDisplayModel
import com.solt.thiochat.data.Groups.Messages.GroupMessageModel
import com.solt.thiochat.databinding.GroupMessageLayoutBinding
import com.solt.thiochat.ui.adapters.GroupMessagesAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import com.solt.thiochat.ui.viewmodel.GroupsViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class GroupMessagesPage: Fragment() {
lateinit var binding: GroupMessageLayoutBinding
val groupViewModel : GroupsViewModel by hiltNavGraphViewModels<GroupsViewModel>(R.id.app_nav_graph)
    val friendViewModel:FriendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)
    val flowOfQueries = MutableStateFlow("")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GroupMessageLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val messageAdapter = GroupMessagesAdapter( this,::checkIfUsersFriends,::sendRequest){}

        binding.messageList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = messageAdapter
        }
        binding.toolbar.title= groupViewModel.selectedGroup?.groupName?:"No Group"
        binding.toolbar.setBackgroundColor(try{groupViewModel.selectedGroup?.groupColour?.toColorInt()?:Color.BLUE}catch (e:IllegalArgumentException){android.graphics.Color.BLUE})
        //Monitor messages
        fun getMessages (){
            viewLifecycleOwner.lifecycleScope.launch {
                groupViewModel.getGroupsMessagesOfSelectedGroup { activity.showMessageFailure(it) }?.catch { activity.showMessageFailure(it.message?:"ERROR") }
                    ?.collectLatest { displayModels ->
                        messageAdapter.submitList(displayModels)
                        //The standard list is a reference for the search lists
                        messageAdapter.standardCurrentList = displayModels


                    }
            }
        }
        getMessages()

        //Send Message
        binding.sendButton.setOnClickListener {
            val message = binding.messageEt.text
            if (message.isBlank()) return@setOnClickListener
            val text = message.toString()
            groupViewModel.sendMessageToGroup(text)
        }
        binding.toolbar.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if(groupViewModel.checkIfRequestAreAccessible { activity.showMessageFailure(it) }){
                    findNavController().navigate(R.id.action_groupMessagesPage_to_groupRequestsPage)
            }
            }

        }
        //Now we want that when the user clicks the search
        //The edit text becomes visible
        //And back resets the messages
        val menu = binding.toolbar.menu
        val searchMenuItem = menu.findItem(R.id.search_item)
        searchMenuItem.setOnMenuItemClickListener {
            binding.searchEdittext.visibility = if(binding.searchEdittext.visibility == View.GONE) View.VISIBLE else binding.searchEdittext.visibility
            true
        }
        //Now the text watcher
        val textWatcher = object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                  flowOfQueries.value = s?.toString()?:""
            }
        }
        binding.searchEdittext.addTextChangedListener(textWatcher)
        //Now monitor the query flows
        viewLifecycleOwner.lifecycleScope.launch {
            flowOfQueries.collectLatest {
                messageAdapter.filter.filter(it)
            }
        }

      //Now set the back button if the search edit text is visible then set it
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchEdittext.visibility == View.VISIBLE){
                    binding.searchEdittext.visibility = View.GONE
                    //reset the list to the standard
                    messageAdapter.resetListToStandard()

                }else{
                    findNavController().popBackStack()
                }

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,backCallback)
    }

    override fun onDestroy() {
        //I want that we return from the group messages page we deselect the selected group
        super.onDestroy()
        Log.i("Erorro","Destroy called ")
        groupViewModel.selectedGroup = null
    }
    suspend fun checkIfUsersFriends(message:GroupMessageDisplayModel):Boolean{
        val activity  = requireActivity() as MainActivity
        val messageAsFriend = FriendModel(message.userId,message.userName)
        return friendViewModel.checkIfTwoUsersAreFriends(messageAsFriend){activity.showMessageFailure(it)}
    }

    fun sendRequest(message: GroupMessageDisplayModel){
        val activity  = requireActivity() as MainActivity
        val messageAsFriend = FriendModel(message.userId,message.userName)
        friendViewModel.sendFriendRequest(messageAsFriend,{activity.showMessageSuccess(it)}){activity.showMessageFailure(it)}
    }



    }