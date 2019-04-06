package org.fossasia.openevent.general.search

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.fragment_search_results.view.chipGroup
import kotlinx.android.synthetic.main.fragment_search_results.view.chipGroupLayout
import kotlinx.android.synthetic.main.fragment_search_results.view.eventsRecycler
import kotlinx.android.synthetic.main.fragment_search_results.view.noSearchResults
import kotlinx.android.synthetic.main.fragment_search_results.view.searchRootLayout
import kotlinx.android.synthetic.main.fragment_search_results.view.shimmerSearch
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.di.Scopes
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDetailsFragmentArgs
import org.fossasia.openevent.general.favorite.FavoriteEventsRecyclerAdapter
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.longSnackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import androidx.appcompat.view.ContextThemeWrapper
import org.jetbrains.anko.design.snackbar

class SearchResultsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {

    private lateinit var rootView: View
    private val searchViewModel by viewModel<SearchViewModel>()
    private val safeArgs: SearchResultsFragmentArgs by navArgs()
    private val favoriteEventsRecyclerAdapter: FavoriteEventsRecyclerAdapter by inject(
        scope = getOrCreateScope(Scopes.SEARCH_RESULTS_FRAGMENT.toString())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScope(getOrCreateScope(Scopes.SEARCH_RESULTS_FRAGMENT.toString()))

        if (searchViewModel.eventDate == null) {
            searchViewModel.eventDate = safeArgs.date
        }
        if (searchViewModel.eventType == null) {
            searchViewModel.eventType = safeArgs.type
        }
        if (searchViewModel.events.value == null) {
            performSearch()
        }
        if (searchViewModel.eventTypes.value == null) {
            searchViewModel.loadEventTypes()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_results, container, false)

        setChips()
        setToolbar(activity, getString(R.string.search_results))
        setHasOptionsMenu(true)

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(context)

        rootView.eventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        searchViewModel.events
            .nonNull()
            .observe(this, Observer { list ->
                showNoSearchResults(list)
                favoriteEventsRecyclerAdapter.submitList(list)
                Timber.d("Fetched events of size %s", favoriteEventsRecyclerAdapter.itemCount)
            })

        searchViewModel.showShimmerResults
            .nonNull()
            .observe(this, Observer {
                if (it) {
                    rootView.shimmerSearch.startShimmer()
                } else {
                    rootView.shimmerSearch.stopShimmer()
                }
                rootView.shimmerSearch.isVisible = it
                rootView.eventsRecycler.isVisible = !it
            })

        searchViewModel.showNoInternetError
            .nonNull()
            .observe(this, Observer {
                if (it) {
                    rootView.searchRootLayout.snackbar(getString(R.string.no_internet_connection_message))
                }
                showNoInternetError(it && searchViewModel.events.value.isNullOrEmpty())
            })

        searchViewModel.error
            .nonNull()
            .observe(this, Observer {
                rootView.searchRootLayout.longSnackbar(it)
            })

        searchViewModel.chipClickable
            .nonNull()
            .observe(this, Observer {
                rootView.chipGroup.children.forEach { chip ->
                    if (chip is Chip) {
                        chip.isClickable = it
                        if (chip.isChecked) chip.isClickable = false
                    }
                }
            })

        rootView.retry.setOnClickListener {
            rootView.noSearchResults.isVisible = false
            performSearch()
        }

        return rootView
    }

    private fun setChips(
        date: String = searchViewModel.eventDate ?: safeArgs.date,
        type: String = searchViewModel.eventType ?: safeArgs.type
    ) {
        if (rootView.chipGroup.childCount>0) {
            rootView.chipGroup.removeAllViews()
        }
            when {
            date != getString(R.string.anytime) && type != getString(R.string.anything) -> {
                addChips(date, true)
                addChips(type, true)
                addChips("Clear All", false)
            }
            date != getString(R.string.anytime) && type == getString(R.string.anything) -> {
                addChips(date, true)
                searchViewModel.eventTypes
                    .nonNull()
                    .observe(this, Observer { list ->
                        list.forEach {
                            addChips(it.name, false)
                        }
                    })
            }
            date == getString(R.string.anytime) && type != getString(R.string.anything) -> {
                addChips(type, true)
                searchViewModel.days?.forEach {
                    addChips(it, false)
                }
            }
            else -> {
                searchViewModel.days?.forEach {
                    addChips(it, false)
                }
            }
        }
    }

    private fun addChips(name: String, checked: Boolean) {
        val newContext = ContextThemeWrapper(context, R.style.CustomChipChoice)
        val chip = Chip(newContext)
        chip.text = name
        chip.isCheckable = true
        chip.isChecked = checked
        chip.isClickable = true
        if (checked) {
            chip.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
        chip.setOnCheckedChangeListener(this)
        rootView.chipGroup.addView(chip)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long) {
                EventDetailsFragmentArgs.Builder()
                    .setEventId(eventID)
                    .build()
                    .toBundle()
                    .also { bundle ->
                        Navigation.findNavController(view).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
                    }
            }
        }
        val favFabClickListener: FavoriteFabClickListener = object : FavoriteFabClickListener {
            override fun onClick(event: Event, itemPosition: Int) {
                searchViewModel.setFavorite(event.id, !event.favorite)
                event.favorite = !event.favorite
                favoriteEventsRecyclerAdapter.notifyItemChanged(itemPosition)
            }
        }

        favoriteEventsRecyclerAdapter.apply {
            onEventClick = eventClickListener
            onFavFabClick = favFabClickListener
        }
    }

    private fun performSearch() {
        val query = safeArgs.query
        val location = safeArgs.location
        val type = searchViewModel.eventType ?: safeArgs.type
        val date = searchViewModel.eventDate ?: safeArgs.date
        searchViewModel.searchEvent = query
        searchViewModel.loadEvents(location, date, type)
    }

    private fun showNoSearchResults(events: List<Event>) {
        rootView.noSearchResults.isVisible = events.isEmpty()
    }

    private fun showNoInternetError(show: Boolean) {
        rootView.noInternetCard.isVisible = show
        rootView.chipGroupLayout.visibility = if (show) View.GONE else View.VISIBLE
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
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            if (searchViewModel.isConnected()) {
                if (buttonView?.text == "Clear All") {
                    searchViewModel.eventDate = getString(R.string.anytime)
                    searchViewModel.eventType = getString(R.string.anything)
                    rootView.noSearchResults.isVisible = false
                    performSearch()
                    setChips()
                }
                searchViewModel.days?.forEach {
                    if (it == buttonView?.text) {
                        searchViewModel.eventDate = it
                        setChips(date = it)
                        rootView.noSearchResults.isVisible = false
                        performSearch()
                        return@forEach
                    }
                }
                searchViewModel.eventTypes.value?.forEach {
                    if (it.name == buttonView?.text) {
                        searchViewModel.eventType = it.name
                        setChips(type = it.name)
                        rootView.noSearchResults.isVisible = false
                        performSearch()
                        return@forEach
                    }
                }
            } else {
                buttonView?.isChecked = false
            }
        }
    }
}
