package com.solt.thiochat.ui.pages

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.GroupMessageLayoutBinding
import com.solt.thiochat.ui.adapters.FriendMessageAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendMessagePage: Fragment() {
    lateinit var binding:GroupMessageLayoutBinding
    val friendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)
    val friendMessagesAdapter = FriendMessageAdapter()

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
        binding.messageList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendMessagesAdapter
        }
        binding.groupName.text = friendsViewModel.selectedFriend?.userName?:"Friend Not None"
        binding.titleFrame.setBackgroundColor(Color.WHITE)

        viewLifecycleOwner.lifecycleScope.launch {
            friendsViewModel.getMessagesWithCurrentFriend { activity.showMessageFailure(it) }?.collectLatest {
                friendMessagesAdapter.submitList(it)
            }
        }
        //Send Message
        binding.sendButton.setOnClickListener {
            val message = binding.messageEt.text
            if (message.isBlank()) return@setOnClickListener
            val text = message.toString()
            friendsViewModel.sendMessage(text)
        }

    }
    override fun onDestroy() {
        //I want do the same as the group
        super.onDestroy()
        friendsViewModel.selectedFriend = null
    }

}