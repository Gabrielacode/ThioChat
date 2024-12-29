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
       binding.root.apply {
           layoutManager = LinearLayoutManager(requireContext())
           adapter = exploreAdapter
       }

        viewLifecycleOwner.lifecycleScope.launch {
            exploreViewModel.getListOfGroupsForExplore { activity.showMessageFailure(it) }?.collectLatest {
                exploreAdapter.submitList(it)
            }
        }

    }
}