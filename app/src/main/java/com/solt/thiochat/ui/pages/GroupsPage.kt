package com.solt.thiochat.ui.pages

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.data.Groups.GroupInfoModel
import com.solt.thiochat.databinding.ExplorePageBinding
import com.solt.thiochat.databinding.GroupPageBinding
import com.solt.thiochat.ui.adapters.GroupAdapter
import com.solt.thiochat.ui.viewmodel.GroupsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupsPage:Fragment() {
    lateinit var binding: GroupPageBinding

    val groupViewModel :GroupsViewModel by hiltNavGraphViewModels<GroupsViewModel>(R.id.app_nav_graph)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GroupPageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val groupAdapter = GroupAdapter(this){
            groupViewModel.selectedGroup = it
          findNavController().navigate(R.id.action_groupsPage_to_groupMessagesPage)
        }
        //The menu we will get and set the item
        //Then we will set the item to open the search click listener when expanded
        val menu = binding.toolbar.menu
        val searchItem = menu.findItem(R.id.search_item).setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_groupsPage_to_groupSearchPage)
            true
        }

        binding.listOfGroups.apply {
            layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            adapter = groupAdapter
        }
        //Listen for groups
        viewLifecycleOwner.lifecycleScope.launch {
            groupViewModel.getGroupsUserIsIn { activity.showMessageFailure(it) }?.collectLatest {
             Log.i("Groups",it.toString())
                groupAdapter.submitList(it)
            }
        }

        binding.addGroup.setOnClickListener {
            findNavController().navigate(R.id.action_groupsPage_to_addGroupDialog)
        }


    }

}