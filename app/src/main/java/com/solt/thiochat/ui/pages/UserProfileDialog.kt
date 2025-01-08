package com.solt.thiochat.ui.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.UserProfileDialogBinding
import com.solt.thiochat.ui.viewmodel.AuthenticationViewModel
import com.solt.thiochat.ui.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileDialog:BottomSheetDialogFragment() {
    lateinit var binding :UserProfileDialogBinding
    val userViewModel by hiltNavGraphViewModels<UserViewModel>(R.id.app_nav_graph)
    val authenticationViewModel by hiltNavGraphViewModels<AuthenticationViewModel>(R.id.app_nav_graph)
    val isEditable = MutableStateFlow(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserProfileDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        //Set the details
        userViewModel.getUserDetails({
               binding.userName.setText(it.userName)
               binding.description.setText(it.description)
            }){activity.showMessageFailure(it)}
        binding.submitButton.setOnClickListener {
            val name = binding.userName.text.toString()
            val description = binding.description.text.toString()
            userViewModel.updateUserDetails(name,description,{
                activity.showMessageSuccess(it)
                isEditable.value = false
            }){
            activity.showMessageFailure(it)
            }
        }
        binding.editButton.setOnClickListener{
            isEditable.value = !isEditable.value
        }
        viewLifecycleOwner.lifecycleScope.launch {
            isEditable.collectLatest {
                //Now we will update things based on the
                when(it){
                    true -> {
                        //Here the edit texts will be editable
                        binding.userName.isEnabled = true
                        binding.description.isEnabled = true
                        //The submit button will be be visible
                        binding.submitButton.visibility = View.VISIBLE
                    }
                    false -> {
                        binding.userName.isEnabled = false
                        binding.description.isEnabled = false
                        //The submit button will be be visible
                        binding.submitButton.visibility = View.GONE
                    }
                    }
                }
            }
        //The request button will lead to the requests page
        binding.requestsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileDialog_to_friendRequestPage)
        }
        //The Sign Out Button
        binding.signOutBtn.setOnClickListener {
            authenticationViewModel.signOut()
            findNavController().popBackStack(R.id.friendsPage,true)
        }
        }
    }
