package com.solt.thiochat.ui.pages

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.renderscript.Toolkit
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.ExplorePageBinding
import com.solt.thiochat.ui.adapters.ExploreAdapter
import com.solt.thiochat.ui.viewmodel.ExploreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExplorePage: Fragment() {
    lateinit var binding: ExplorePageBinding
    val exploreViewModel :ExploreViewModel by hiltNavGraphViewModels<ExploreViewModel>(R.id.app_nav_graph)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ExplorePageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val exploreAdapter = ExploreAdapter{ groupDisplayModel ->
            exploreViewModel.joinGroup(groupDisplayModel,{activity.showMessageSuccess(it)},{activity.showMessageFailure(it)}) }
       binding.listOfGroups.apply {
           layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
           adapter = exploreAdapter
       }
        //The menu we will get and set the item
        //Then we will set the item to open the search click listener when expanded
        val menu = binding.toolbar.menu
        val searchItem = menu.findItem(R.id.search_item).setOnMenuItemClickListener {
            //Animate the search drawable
            (it.icon as? AnimatedVectorDrawable)?.start()
            findNavController().navigate(R.id.action_explorePage_to_exploreSearchPage)
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            exploreViewModel.getListOfGroupsForExplore { activity.showMessageFailure(it) }?.collectLatest {
                exploreAdapter.submitList(it)
            }
        }


    }
}