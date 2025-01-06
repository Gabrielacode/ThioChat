package com.solt.thiochat.ui.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.AuthPageBinding
import com.solt.thiochat.ui.viewmodel.AuthenticationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationFragment: Fragment(){
 lateinit var binding: AuthPageBinding
 val authViewModel : AuthenticationViewModel by hiltNavGraphViewModels(R.id.app_nav_graph)

 override fun onCreateView(
  inflater: LayoutInflater,
  container: ViewGroup?,
  savedInstanceState: Bundle?
 ): View {
  binding = AuthPageBinding.inflate(inflater,container,false)
  return binding.root
 }

 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
  super.onViewCreated(view, savedInstanceState)
  //First check if the user is authenticated
//  authViewModel.checkIfUserIsAuthenticated{
//   findNavController().navigate(R.id.action_authenticationFragment_to_friendsPage)
//  }
  //Make sure at least one option is selected
  binding.materialButtonToggleGroup.apply {
   if (checkedButtonIds.isEmpty()){
    check(R.id.sign_in)
   }
  }

  binding.submitButton.setOnClickListener {
   val emailAddress = binding.emailAddressInput.text.toString()
   val password = binding.passwordInput.text.toString()
   if(password.length < 6){
    binding.passwordInputLayout.error = "Password must be 6 characters or above"
    return@setOnClickListener
   }
   val activity = requireActivity() as MainActivity
 when(binding.materialButtonToggleGroup.checkedButtonId){
    binding.signIn.id ->{
     authViewModel.signInWithEmailAndPassword(emailAddress,password,{activity.showMessageSuccess(it)
     findNavController().navigate(R.id.action_authenticationFragment_to_friendsPage)}){activity.showMessageFailure(it)}
    }
  binding.signUp.id->{
   authViewModel.signUpWithEmailAndAddress(emailAddress,password,{activity.showMessageSuccess(it)
    findNavController().navigate(R.id.action_authenticationFragment_to_friendsPage)}){activity.showMessageFailure(it)}
  }
  else -> return@setOnClickListener
 }

  }
  binding.googleSignInButton.setOnClickListener{
   val activity = requireActivity() as MainActivity
   authViewModel.signInWithGoogle(requireActivity(),{activity.showMessageSuccess(it)
    findNavController().navigate(R.id.action_authenticationFragment_to_friendsPage)}){activity.showMessageFailure(it)}
  }
 }

}