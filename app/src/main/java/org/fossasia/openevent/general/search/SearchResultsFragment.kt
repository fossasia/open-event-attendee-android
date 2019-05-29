package org.fossasia.openevent.general.search

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
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
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.types.EventType
import org.fossasia.openevent.general.favorite.FavoriteEventsRecyclerAdapter
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.longSnackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import androidx.appcompat.view.ContextThemeWrapper
import org.fossasia.openevent.general.common.EventsDiffCallback
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import kotlinx.android.synthetic.main.item_card_events.view.eventImage
import org.fossasia.openevent.general.event.EventViewHolder
import org.fossasia.openevent.general.utils.extensions.setPostponeSharedElementTransition
import org.fossasia.openevent.general.utils.extensions.setStartPostponedEnterTransition

class SearchResultsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {

    private lateinit var rootView: View
    private val searchViewModel by viewModel<SearchViewModel>()
    private val safeArgs: SearchResultsFragmentArgs by navArgs()
    private val favoriteEventsRecyclerAdapter = FavoriteEventsRecyclerAdapter(EventsDiffCallback())

    private lateinit var days: Array<String>
    private lateinit var eventDate: String
    private lateinit var eventType: String
    private var eventTypesList: List<EventType>? = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        days = resources.getStringArray(R.array.days)
        eventDate = searchViewModel.savedTime ?: safeArgs.date
        eventType = searchViewModel.savedType ?: safeArgs.type

        searchViewModel.loadEventTypes()
        searchViewModel.eventTypes
            .nonNull()
            .observe(this, Observer { list ->
                eventTypesList = list
            })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_results, container, false)
        setPostponeSharedElementTransition()

        setChips()
        setToolbar(activity, getString(R.string.search_results))
        setHasOptionsMenu(true)

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(context)

        rootView.eventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false
        rootView.viewTreeObserver.addOnDrawListener {
            setStartPostponedEnterTransition()
        }

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

        rootView.retry.setOnClickListener {
            performSearch()
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        favoriteEventsRecyclerAdapter.apply {
            onEventClick = null
            onFavFabClick = null
        }
    }

    private fun setChips(date: String = eventDate, type: String = eventType) {
        if (rootView.chipGroup.childCount> 0) {
            rootView.chipGroup.removeAllViews()
        }
            when {
            date != getString(R.string.anytime) && type != getString(R.string.anything) -> {
                addChips(date, true)
                addChips(type, true)
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
            override fun onClick(eventID: Long, itemPosition: Int) {
                var extras: Navigator.Extras? = null
                val itemEventViewHolder = rootView.eventsRecycler.findViewHolderForAdapterPosition(itemPosition)
                itemEventViewHolder?.let {
                    if (itemEventViewHolder is EventViewHolder) {
                        extras = FragmentNavigatorExtras(
                            itemEventViewHolder.itemView.eventImage to "eventDetailImage")
                    }
                }
                extras?.let {
                    findNavController(view).navigate(SearchResultsFragmentDirections
                        .actionSearchResultsToEventDetail(eventId = eventID), it)
                } ?: findNavController(view)
                    .navigate(SearchResultsFragmentDirections.actionSearchResultsToEventDetail(eventId = eventID))
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
        val freeEvents = safeArgs.freeEvents
        val sortBy = safeArgs.sort

        val sessionsAndSpeakers = safeArgs.sessionsAndSpeakers
        searchViewModel.searchEvent = query
        searchViewModel.loadEvents(location, date, type, freeEvents, sortBy, sessionsAndSpeakers)
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
            R.id.filter -> {
                findNavController(rootView)
                    .navigate(SearchResultsFragmentDirections.actionSearchResultsToSearchFilter(
                    date = safeArgs.date,
                    freeEvents = safeArgs.freeEvents,
                    location = safeArgs.location,
                    type = safeArgs.type,
                    query = safeArgs.query,
                    sort = safeArgs.sort,
                    sessionsAndSpeakers = safeArgs.sessionsAndSpeakers))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_results, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        days.forEach {
            if (it == buttonView?.text) {
                searchViewModel.savedTime = if (isChecked) it else null
                eventDate = if (isChecked) it else getString(R.string.anytime)
                setChips(date = it)
                refreshEvents()
                return@forEach
            }
        }
        eventTypesList?.forEach {
            if (it.name == buttonView?.text) {
                searchViewModel.savedType = if (isChecked) it.name else null
                eventType = if (isChecked) it.name else getString(R.string.anything)
                refreshEvents()
                return@forEach
            }
        }
    }

    private fun refreshEvents() {
        setChips()
        rootView.noSearchResults.isVisible = false
        favoriteEventsRecyclerAdapter.submitList(null)
        searchViewModel.clearEvents()
        if (searchViewModel.isConnected()) performSearch()
        else showNoInternetError(true)
    }
}
