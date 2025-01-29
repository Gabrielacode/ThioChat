package com.solt.thiochat.ui.pages

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.data.Groups.GroupInfoModel
import com.solt.thiochat.data.Groups.ModeOfAcceptance
import com.solt.thiochat.databinding.AddGroupLayoutBinding
import com.solt.thiochat.ui.viewmodel.GroupsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddGroupDialog: DialogFragment() {
    lateinit var binding:AddGroupLayoutBinding
    val groupsViewModel: GroupsViewModel by hiltNavGraphViewModels<GroupsViewModel>(R.id.app_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = AddGroupLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity

        //Listen to the color from the selector and change the background accordingly
        viewLifecycleOwner.lifecycleScope.launch {
            listenForFlowOfColours().collectLatest {
                val backgroundDrawable = binding.mainLayout.background as GradientDrawable
                backgroundDrawable.apply {
                    colors = intArrayOf(it.color,it.color)
             }
                //We might check whether it is light and dark and change the text color
                val luminance = ColorUtils.calculateLuminance(it.color)
                binding.groupName.apply{
                    setTextColor(if (luminance <0.5)Color.WHITE else Color.BLACK)
                    setHintTextColor(if (luminance <0.5)Color.LTGRAY else Color.DKGRAY)
                }
            }
        }
        //OnClick Dismiss the dialog and add the group
        binding.button.setOnClickListener {
            val text = binding.groupName.text
            if (text.isBlank()){
                binding.groupName.error = "Name is empty"
                return@setOnClickListener
            }
            val name = text.toString()
            val colourasHex = binding.colourPicker.colorEnvelope.hexCode
            val modeOfAcceptance = when(binding.modeOfAcceptance.checkedButtonId){
                R.id.none_mode -> ModeOfAcceptance.NONE
                R.id.request_mode ->ModeOfAcceptance.REQUEST
                else -> ModeOfAcceptance.NONE
            }
            val groupInfoModel = GroupInfoModel(name,colourasHex,modeOfAcceptance.toString())
            groupsViewModel.addGroup(groupInfoModel,{
                dismiss()
                activity.showMessageSuccess(it)

            }){
                dismiss()
                activity.showMessageFailure(it)
            }
        }


    }
    fun listenForFlowOfColours ():Flow<ColorEnvelope>{
       return callbackFlow<ColorEnvelope> {
           val listener = object :ColorEnvelopeListener{
               override fun onColorSelected(color: ColorEnvelope?,fromUser: Boolean) {
                   if (color != null) trySend(color)
               }

           }
            binding.colourPicker.setColorListener(listener)
           awaitClose {
               binding.colourPicker.setColorListener(null)
           }
    }
}}