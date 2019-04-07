package org.fossasia.openevent.general.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import android.content.res.ColorStateList
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_search_results.view.searchRootLayout
import kotlinx.android.synthetic.main.fragment_search_results.view.eventsRecycler
import kotlinx.android.synthetic.main.fragment_search_results.view.shimmerSearch
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_search_results.view.noSearchResults
import kotlinx.android.synthetic.main.fragment_search_results.view.chipGroup
import kotlinx.android.synthetic.main.fragment_search_results.view.todayChip
import kotlinx.android.synthetic.main.fragment_search_results.view.tomorrowChip
import kotlinx.android.synthetic.main.fragment_search_results.view.weekendChip
import kotlinx.android.synthetic.main.fragment_search_results.view.monthChip
import kotlinx.android.synthetic.main.fragment_search_results.view.chipGroupLayout
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.di.Scopes
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.event.EventDetailsFragmentArgs
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.common.ShareFabClickListener
import org.fossasia.openevent.general.favorite.FavoriteEventsRecyclerAdapter
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.fossasia.openevent.general.utils.Utils.setToolbar

class SearchResultsFragment : Fragment() {
    private lateinit var rootView: View
    private val searchViewModel by viewModel<SearchViewModel>()
    private val safeArgs: SearchResultsFragmentArgs by navArgs()
    private val favoriteEventsRecyclerAdapter: FavoriteEventsRecyclerAdapter by inject(
        scope = getOrCreateScope(Scopes.SEARCH_RESULTS_FRAGMENT.toString())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScope(getOrCreateScope(Scopes.SEARCH_RESULTS_FRAGMENT.toString()))
        performSearch(safeArgs)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_results, container, false)

        val selectedChip = when (safeArgs.date) {
            getString(R.string.today) -> rootView.todayChip
            getString(R.string.tomorrow) -> rootView.tomorrowChip
            getString(R.string.weekend) -> rootView.weekendChip
            getString(R.string.month) -> rootView.monthChip
            else -> null
        }
        selectedChip?.apply {
            isChecked = true
            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }

        rootView.chipGroup.setOnCheckedChangeListener { chipGroup, id ->

            chipGroup.children.forEach { chip ->
                if (chip is Chip) {
                    if (chip.id != chipGroup.checkedChipId) {
                        chip.chipBackgroundColor =
                            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey))
                        chip.isClickable = true
                    } else {
                        chip.isClickable = false
                        chip.chipBackgroundColor =
                            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                        rootView.noSearchResults.isVisible = false
                        favoriteEventsRecyclerAdapter.submitList(null)
                        performSearch(safeArgs, chip.text.toString())
                    }
                }
            }
        }
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

        searchViewModel.showNoInternetError
            .nonNull()
            .observe(this, Observer {
                showNoInternetError(it)
            })

        searchViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(rootView.searchRootLayout, it, Snackbar.LENGTH_LONG).show()
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
            performSearch(safeArgs)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long) { EventDetailsFragmentArgs.Builder()
                .setEventId(eventID)
                .build()
                .toBundle()
                .also { bundle ->
                    Navigation.findNavController(view).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
                }
            }
        }

        val shareFabClickListener: ShareFabClickListener = object : ShareFabClickListener {
            override fun onClick(event: Event) {
                Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
                }.also { intent ->
                    startActivity(Intent.createChooser(intent, "Share Event Details"))
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
            onShareFabClick = shareFabClickListener
            onFavFabClick = favFabClickListener
        }
    }

    private fun performSearch(args: SearchResultsFragmentArgs, eventDate: String = "") {
        val query = args.query
        val location = args.location
        val type = args.type
        val date = if (eventDate.isNotEmpty()) eventDate else args.date
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
}
