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
import org.fossasia.openevent.general.event.types.EventType
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

class SearchResultsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {

    private lateinit var rootView: View
    private val searchViewModel by viewModel<SearchViewModel>()
    private val safeArgs: SearchResultsFragmentArgs by navArgs()
    private val favoriteEventsRecyclerAdapter: FavoriteEventsRecyclerAdapter by inject(
        scope = getOrCreateScope(Scopes.SEARCH_RESULTS_FRAGMENT.toString())
    )
    private lateinit var days: Array<String>
    private lateinit var eventDate: String
    private lateinit var eventType: String
    private var eventTypesList: List<EventType>? = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScope(getOrCreateScope(Scopes.SEARCH_RESULTS_FRAGMENT.toString()))

        days = resources.getStringArray(R.array.days)
        eventDate = safeArgs.date
        eventType = safeArgs.type

        searchViewModel.loadEventTypes()
        searchViewModel.eventTypes
            .nonNull()
            .observe(this, Observer { list ->
                eventTypesList = list
            })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_results, container, false)

        setChips(safeArgs.date, safeArgs.type)
        setToolbar(activity, getString(R.string.search_results))
        setHasOptionsMenu(true)

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(context)

        rootView.eventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        searchViewModel.events
            .nonNull()
            .observe(this, Observer { list ->
                favoriteEventsRecyclerAdapter.submitList(list)
                showNoSearchResults(list)
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
            })

        searchViewModel.connection
            .nonNull()
            .observe(this, Observer { isConnected ->
                if (isConnected) {
                    showNoInternetError(false)
                    val currentEvents = searchViewModel.events.value
                    if (currentEvents == null) performSearch()
                } else {
                    showNoInternetError(searchViewModel.events.value == null)
                }
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
            performSearch()
        }

        return rootView
    }

    private fun setChips(date: String = eventDate, type: String = eventType) {
        if (rootView.chipGroup.childCount>0) {
            rootView.chipGroup.removeAllViews()
        }
            when {
            date != getString(R.string.anytime) && type != getString(R.string.anything) -> {
                addChips(date, true)
                addChips(type, true)
                addChips(getString(R.string.clear_all), false)
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
                days.forEach {
                    addChips(it, false)
                }
            }
            else -> {
                days.forEach {
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
        searchViewModel.clearEvents()
        val query = safeArgs.query
        val location = safeArgs.location
        val type = eventType
        val date = eventDate
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
            if (buttonView?.text == "Clear All") {
                eventDate = getString(R.string.anytime)
                eventType = getString(R.string.anything)
                rootView.noSearchResults.isVisible = false
                favoriteEventsRecyclerAdapter.submitList(null)
                searchViewModel.clearEvents()
                if (searchViewModel.isConnected()) performSearch()
                else showNoInternetError(true)
                setChips()
            }
            days.forEach {
                if (it == buttonView?.text) {
                    eventDate = it
                    setChips(date = it)
                    rootView.noSearchResults.isVisible = false
                    favoriteEventsRecyclerAdapter.submitList(null)
                    searchViewModel.clearEvents()
                    if (searchViewModel.isConnected()) performSearch()
                    else showNoInternetError(true)
                    return@forEach
                }
            }
            eventTypesList?.forEach {
                if (it.name == buttonView?.text) {
                    eventType = it.name
                    setChips(type = it.name)
                    rootView.noSearchResults.isVisible = false
                    favoriteEventsRecyclerAdapter.submitList(null)
                    searchViewModel.clearEvents()
                    if (searchViewModel.isConnected()) performSearch()
                    else showNoInternetError(true)
                    return@forEach
                }
            }
        }
    }
}
