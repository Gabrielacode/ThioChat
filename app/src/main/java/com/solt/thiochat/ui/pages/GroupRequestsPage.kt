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
import com.solt.thiochat.ui.adapters.GroupRequestsAdapter
import com.solt.thiochat.ui.viewmodel.GroupsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroupRequestsPage:Fragment() {
    lateinit var binding:GroupRequestsLayoutBinding
    val groupsViewModel by hiltNavGraphViewModels<GroupsViewModel>(R.id.app_nav_graph)

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
        val requestAdapter = GroupRequestsAdapter({ request ->
            groupsViewModel.acceptGroupRequest(request,{activity.showMessageSuccess(it)}){activity.showMessageFailure(it)}
        },{ request ->
            groupsViewModel.rejectGroupRequest(request,{activity.showMessageSuccess(it)}){activity.showMessageFailure(it)}})
        binding.listOfRequest.apply {
          layoutManager = LinearLayoutManager(activity)
          adapter = requestAdapter
      }
        viewLifecycleOwner.lifecycleScope.launch {
            groupsViewModel.getGroupRequests { activity.showMessageFailure(it)}?.collectLatest {  requestAdapter.submitList(it)}
        }
    }

}