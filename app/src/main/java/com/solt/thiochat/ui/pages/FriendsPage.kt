package com.solt.thiochat.ui.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.utilities.CorePalette
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.FriendsPageBinding
import com.solt.thiochat.ui.adapters.FriendsAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FriendsPage: Fragment() {
    val friendsViewModel:FriendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)

    lateinit var binding: FriendsPageBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FriendsPageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val friendAdapter = FriendsAdapter{
            friendsViewModel.selectedFriend = it
            findNavController().navigate(R.id.action_friendsPage_to_friendMessagePage)
        }

        //Should we check whether the user is signed in again
    binding.listOfFriends.apply {
        layoutManager = LinearLayoutManager(requireActivity())
        adapter = friendAdapter
    }
        binding.searchButton.setOnClickListener {
            findNavController().navigate(R.id.action_friendsPage_to_friendSearchPage)
        }
        //Get user details
        friendsViewModel.getUserDetails({

            binding.userName.text = it.userName
        }){

            activity.showMessageFailure(it)
        }
        //Get list of friends
        viewLifecycleOwner.lifecycleScope.launch {

             friendsViewModel.getFriends { activity.showMessageFailure(it) }
                 ?.collectLatest {
                Log.i("stateflow",it.toString())
                friendAdapter.submitList(it)
            }
        }
        binding.userName.setOnClickListener {
            findNavController().navigate(R.id.action_friendsPage_to_friendRequestPage)
        }

    }
}