package com.solt.thiochat.ui.pages

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.solt.thiochat.MainActivity
import com.solt.thiochat.R
import com.solt.thiochat.databinding.SearchBottomDialogBinding
import com.solt.thiochat.ui.adapters.ExploreSearchAdapter
import com.solt.thiochat.ui.adapters.GroupAdapter
import com.solt.thiochat.ui.viewmodel.ExploreViewModel
import com.solt.thiochat.ui.viewmodel.GroupsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExploreSearchPage: BottomSheetDialogFragment() {
    lateinit var binding : SearchBottomDialogBinding
    //This will be the flow of search queries like in the friends search page
    val flowOfQuery = MutableStateFlow("")
    val exploreViewModel by hiltNavGraphViewModels<ExploreViewModel>(R.id.app_nav_graph)
    val groupViewModel by hiltNavGraphViewModels<GroupsViewModel>(R.id.app_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchBottomDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                //Set the state flow
                flowOfQuery.value = s?.toString()?:""
            }

        }
        val activity = requireActivity() as MainActivity
        val searchAdapter = ExploreSearchAdapter( this, {group-> exploreViewModel.joinGroup(group,{activity.showMessageSuccess(it)},{activity.showMessageFailure(it)}) })
        {  model,layout -> groupViewModel.selectedGroup = model
            val sharedElementTransition = FragmentNavigatorExtras(layout to "groupName")
            findNavController().navigate(R.id.action_exploreSearchPage_to_groupMessagesPage,null,sharedElementTransition)}
        binding.searchBar.addTextChangedListener(textWatcher)
        binding.listOfFriends.apply {
            layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            adapter = searchAdapter
        }
        viewLifecycleOwner.lifecycleScope.launch {
            flowOfQuery.collectLatest {
                exploreViewModel.searchForGroups(it)?.collectLatest {list ->
                    searchAdapter.submitList(list )
                }
            }}
            }


    }
