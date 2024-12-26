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
import com.solt.thiochat.ui.adapters.GroupMessagesAdapter
import com.solt.thiochat.ui.viewmodel.GroupsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroupMessagesPage: Fragment() {
lateinit var binding: GroupMessageLayoutBinding
val groupViewModel : GroupsViewModel by hiltNavGraphViewModels<GroupsViewModel>(R.id.app_nav_graph)
    val messageAdapter = GroupMessagesAdapter()
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
            adapter = messageAdapter
        }
        binding.groupName.text = groupViewModel.selectedGroup?.groupName?:"No Group"
        binding.titleFrame.setBackgroundColor(try{groupViewModel.selectedGroup?.groupColour?.toColorInt()?:Color.BLUE}catch (e:IllegalArgumentException){android.graphics.Color.BLUE})
        //Monitor messages
        viewLifecycleOwner.lifecycleScope.launch {
            groupViewModel.getGroupsMessagesOfSelectedGroup { activity.showMessageFailure(it) }?.collectLatest {
               Log.i("Errorr",it.toString())
                messageAdapter.submitList(it)
            }
        }

        //Send Message
        binding.sendButton.setOnClickListener {
            val message = binding.messageEt.text
            if (message.isBlank()) return@setOnClickListener
            val text = message.toString()
            groupViewModel.sendMessageToGroup(text)
        }


    }
}