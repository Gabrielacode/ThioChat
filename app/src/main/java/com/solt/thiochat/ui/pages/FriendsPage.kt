package com.solt.thiochat.ui.pages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.FriendsPageBinding
import com.solt.thiochat.ui.adapters.FriendsAdapter
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FriendsPage: Fragment() {
    val friendsViewModel:FriendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)

    lateinit var binding: FriendsPageBinding
    val friendAdapter = FriendsAdapter()
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
        //Should we check whether the user is signed in again
    binding.listOfFriends.apply {
        layoutManager = LinearLayoutManager(requireActivity())
        adapter = friendAdapter
    }
        //Get user details
        friendsViewModel.getUserDetails({
            Log.i("Errorr",it.toString())
            binding.userName.text = it.userName
        }){
            val activity = requireActivity() as MainActivity
            activity.showMessageFailure(it)
        }
        //Get list of friends
        viewLifecycleOwner.lifecycleScope.launch {

            friendsViewModel.getFriends {
                val activity = requireActivity() as MainActivity
                activity.showMessageFailure(it)
            }?.collectLatest {
                friendAdapter.submitList(it)
            }
        }

    }
}