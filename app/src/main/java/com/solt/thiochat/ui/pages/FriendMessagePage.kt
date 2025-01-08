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
import com.solt.thiochat.databinding.GroupMessageLayoutBinding
import com.solt.thiochat.ui.adapters.FriendMessageAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendMessagePage: Fragment() {
    lateinit var binding:GroupMessageLayoutBinding
    val friendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)
    val friendMessagesAdapter = FriendMessageAdapter()
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
        binding.messageList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendMessagesAdapter
        }
        binding.toolbar.title = friendsViewModel.selectedFriend?.userName?:"Friend Not None"
        binding.toolbar.setBackgroundColor(Color.WHITE)

        viewLifecycleOwner.lifecycleScope.launch {
            friendsViewModel.getMessagesWithCurrentFriend { activity.showMessageFailure(it) }?.collectLatest {
                friendMessagesAdapter.submitList(it)
                friendMessagesAdapter.standardCurrentList = it
            }
        }
        //Send Message
        binding.sendButton.setOnClickListener {
            val message = binding.messageEt.text
            if (message.isBlank()) return@setOnClickListener
            val text = message.toString()
            friendsViewModel.sendMessage(text)
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
        val textWatcher = object : TextWatcher {
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
                friendMessagesAdapter.filter.filter(it)
            }
        }

        //Now set the back button if the search edit text is visible then set it
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchEdittext.visibility == View.VISIBLE){
                    binding.searchEdittext.visibility = View.GONE
                    //reset the list to the standard
                    friendMessagesAdapter.resetListToStandard()

                }else{
                    findNavController().popBackStack()
                }

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,backCallback)


    }
    override fun onDestroy() {
        //I want do the same as the group
        super.onDestroy()
        friendsViewModel.selectedFriend = null
    }

}