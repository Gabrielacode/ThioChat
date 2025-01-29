package com.solt.thiochat.ui.pages

import android.graphics.BitmapFactory
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.color.utilities.CorePalette
import com.google.android.renderscript.Toolkit
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.data.Friends.FriendModel
import com.solt.thiochat.databinding.FriendsPageBinding
import com.solt.thiochat.ui.adapters.FriendsAdapter
import com.solt.thiochat.ui.utils.BlurBuilder
import com.solt.thiochat.ui.viewmodel.AuthenticationViewModel
import com.solt.thiochat.ui.viewmodel.FriendsViewModel
import com.solt.thiochat.ui.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FriendsPage: Fragment() {
    val friendsViewModel:FriendsViewModel by hiltNavGraphViewModels<FriendsViewModel>(R.id.app_nav_graph)
    val authViewModel :AuthenticationViewModel by hiltNavGraphViewModels<AuthenticationViewModel>(R.id.app_nav_graph)
    val userViewModel :UserViewModel by hiltNavGraphViewModels<UserViewModel>(R.id.app_nav_graph)
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
        val friendAdapter = FriendsAdapter(this){
            friendsViewModel.selectedFriend = FriendModel(it.userId,it.userName)
            findNavController().navigate(R.id.action_friendsPage_to_friendMessagePage)
        }


        //If the user is signed in the back button should send you out of the app
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
              authViewModel.checkIfUserIsAuthenticated {
                  requireActivity().finish()
              }
            }})

        //Should we check whether the user is signed in again
    binding.listOfFriends.apply {
        layoutManager = LinearLayoutManager(requireActivity())
        adapter = friendAdapter
    }
        //Get the menu and set the items
        val menu = binding.toolbar.menu
        val searchMenuItem = menu.findItem(R.id.search_item)
       searchMenuItem.setOnMenuItemClickListener {
           //Animate the search drawable
           (it.icon as?AnimatedVectorDrawable)?.start()
            findNavController().navigate(R.id.action_friendsPage_to_friendSearchPage)
           true
        }

        //Get user details
        userViewModel.getUserDetails({
            binding.toolbar.title = it.userName
        }){
            activity.showMessageFailure(it)
        }
        //Get list of friends
        viewLifecycleOwner.lifecycleScope.launch {

             friendsViewModel.getFriends { activity.showMessageFailure(it) }
                 ?.collectLatest {

                friendAdapter.submitList(it)
            }
        }
        binding.toolbar.setOnClickListener {
            findNavController().navigate(R.id.action_friendsPage_to_userProfileDialog)
        }

    }
}