package com.solt.thiochat.ui.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.GroupRequestsLayoutBinding
import com.solt.thiochat.ui.adapters.FriendRequestAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendRequestPage:Fragment() {
    lateinit var binding: GroupRequestsLayoutBinding
    val friendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GroupRequestsLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val friendRequestAdapter = FriendRequestAdapter({
            request->
            friendsViewModel.acceptFriendRequest(request,{activity.showMessageSuccess(it)}){activity.showMessageFailure(it)}
        }){request ->
            friendsViewModel.rejectFriendRequest(request,{activity.showMessageSuccess(it)}){activity.showMessageFailure(it)}
        }

        binding.listOfRequest.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = friendRequestAdapter
        }
     viewLifecycleOwner.lifecycleScope.launch {
         friendsViewModel.getFriendRequest { activity.showMessageFailure(it) }?.collectLatest {
             friendRequestAdapter.submitList(it)
         }
     }
    }

}