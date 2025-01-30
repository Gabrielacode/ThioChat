package com.solt.thiochat.ui.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DrawFilter
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BitmapCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionInflater
import com.google.android.renderscript.Toolkit
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
    lateinit var groupAdapter:GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = requireActivity() as MainActivity
         groupAdapter = GroupAdapter(this){model,layout->
            groupViewModel.selectedGroup = model
            val sharedElementTransition = FragmentNavigatorExtras(layout to "groupItem${model.groupName}")
            findNavController().navigate(R.id.action_groupsPage_to_groupMessagesPage,null,null,sharedElementTransition)
        }
        lifecycleScope.launch {
            groupViewModel.getGroupsUserIsIn { activity.showMessageFailure(it) }?.collectLatest {

                groupAdapter.submitList(it)
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GroupPageBinding.inflate(inflater,container,false)
        postponeEnterTransition()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)






        //The menu we will get and set the item
        //Then we will set the item to open the search click listener when expanded
        val menu = binding.toolbar.menu
        val searchItem = menu.findItem(R.id.search_item).setOnMenuItemClickListener {
            //Animate the search drawable
            (it.icon as? AnimatedVectorDrawable)?.start()
            findNavController().navigate(R.id.action_groupsPage_to_groupSearchPage)
            true
        }

        binding.listOfGroups.apply {
            layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL).apply {
                this.gapStrategy =StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
            adapter = groupAdapter
        }
        //Listen for groups


        binding.addGroup.setOnClickListener {
            findNavController().navigate(R.id.action_groupsPage_to_addGroupDialog)
        }




    }



}