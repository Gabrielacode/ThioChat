package com.solt.thiochat.ui.pages

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.internal.TextWatcherAdapter
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.data.Friends.FriendDisplayModel
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.databinding.SearchBottomDialogBinding
import com.solt.thiochat.ui.adapters.FriendsAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FriendSearchPage:BottomSheetDialogFragment() {
    lateinit var binding :SearchBottomDialogBinding
    //This will be the flow of search queries
    val flowOfQuery = MutableStateFlow("")
    val friendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchBottomDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
             //Set the state flow
                flowOfQuery.value = s?.toString()?:""
            }

        }
        val activity = requireActivity() as MainActivity
        //We will sort based on the current friend
        val friendsAdapter = FriendsAdapter(this){
            friendsViewModel.selectedFriend = FriendModel(it.userId,it.userName)
            findNavController().navigate(R.id.action_friendSearchPage_to_friendMessagePage)
        }
          binding.searchBar.addTextChangedListener(textWatcher)
          binding.listOfFriends.apply {
              layoutManager = LinearLayoutManager(requireActivity())
              adapter = friendsAdapter
          }
        viewLifecycleOwner.lifecycleScope.launch {
            friendsViewModel.getFriends { activity.showMessageFailure(it) }?.collectLatest {
                friendsAdapter.submitList(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            flowOfQuery.collectLatest {
                friendsViewModel.searchFriends(it)?.collectLatest { friends ->
                    friendsAdapter.submitList(friends.map { friend ->FriendDisplayModel(friend.userId,friend.userName,null) })

                }
            }
        }
    }
}