package org.fossasia.openevent.general.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_search_type.view.eventTypesRecyclerView
import kotlinx.android.synthetic.main.fragment_search_type.view.eventTypesTextTitle
import kotlinx.android.synthetic.main.fragment_search_type.view.shimmerSearchEventTypes
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.navigateTo
import org.fossasia.openevent.general.utils.extensions.navigateWithBundleTo
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchTypeFragment : Fragment() {
    private val typesAdapter: SearchTypeAdapter = SearchTypeAdapter()
    private val searchTypeViewModel by viewModel<SearchTypeViewModel>()
    private val safeArgs: SearchTypeFragmentArgs by navArgs()
    private lateinit var rootView: View
    private val eventTypesList: MutableList<String> = arrayListOf("Anything")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_search_type, container, false)
        setToolbar(activity, "", hasUpEnabled = true)
        setHasOptionsMenu(true)
        rootView.eventTypesRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.eventTypesRecyclerView.adapter = typesAdapter
        searchTypeViewModel.loadEventTypes()

        searchTypeViewModel.connection
            .nonNull()
            .observe(this, Observer { isConnected ->
                if (isConnected) {
                    searchTypeViewModel.loadEventTypes()
                    showNoInternetError(false)
                } else {
                    showNoInternetError(searchTypeViewModel.eventTypes.value == null)
                }
            })

        searchTypeViewModel.showShimmer
            .nonNull()
            .observe(viewLifecycleOwner, Observer { shouldShowShimmer ->
                if (shouldShowShimmer) {
                    rootView.shimmerSearchEventTypes.startShimmer()
                } else {
                    rootView.shimmerSearchEventTypes.stopShimmer()
                }
                rootView.shimmerSearchEventTypes.isVisible = shouldShowShimmer
            })

        searchTypeViewModel.eventTypes
            .nonNull()
            .observe(this, Observer { list ->
                list.forEach {
                    eventTypesList.add(it.name)
                }
                setCurrentChoice(safeArgs.type)
                typesAdapter.addAll(eventTypesList)
                typesAdapter.notifyDataSetChanged()
            })

        val listener: TypeClickListener = object : TypeClickListener {
            override fun onClick(chosenType: String) {
                redirectToSearch(chosenType)
            }
        }
        typesAdapter.setListener(listener)

        rootView.retry.setOnClickListener {
            if (searchTypeViewModel.isConnected()) searchTypeViewModel.loadEventTypes()
        }

        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun redirectToSearch(type: String) {
        searchTypeViewModel.saveType(type)
        val (destFragId, popUpId) = if (safeArgs.fromFragmentName == SEARCH_FILTER_FRAGMENT)
            R.id.searchFilterFragment to R.id.searchResultsFragment
        else
            R.id.searchFragment to R.id.eventsFragment

        val navOptions = NavOptions.Builder().setPopUpTo(popUpId, false).build()
        navigateTo(rootView, destFragId, navOptions)
        val navArgs = if (safeArgs.fromFragmentName == SEARCH_FILTER_FRAGMENT) {
            SearchFilterFragmentArgs(
                query = safeArgs.query
            ).toBundle()
        } else
            null
        navArgs.navigateWithBundleTo(rootView, destFragId, navOptions)
    }

    private fun setCurrentChoice(value: String?) {
        for (pos in 0 until eventTypesList.size) {
            if (eventTypesList[pos] == value) {
                typesAdapter.setCheckTypePosition(pos)
                return
            }
        }
    }

    private fun showNoInternetError(show: Boolean) {
        rootView.noInternetCard.isVisible = show
        rootView.eventTypesRecyclerView.isVisible = !show
        rootView.eventTypesTextTitle.isVisible = !show
    }
}
