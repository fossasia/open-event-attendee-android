package org.fossasia.openevent.general.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_search_results.view.searchRootLayout
import kotlinx.android.synthetic.main.fragment_search_results.view.eventsRecycler
import kotlinx.android.synthetic.main.fragment_search_results.view.shimmerSearch
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.fragment_search_results.view.noSearchResults
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
        postponeEnterTransition()
        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = getString(R.string.search_results)
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(context)

        rootView.eventsRecycler.adapter = favoriteEventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false
        rootView.eventsRecycler.viewTreeObserver
            .addOnPreDrawListener {
                startPostponedEnterTransition()
                true
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

        rootView.retry.setOnClickListener {
            performSearch(safeArgs)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long, sharedImage: ImageView) {
                val extras =
                    FragmentNavigatorExtras(
                        sharedImage to eventID.toString()
                    )
                EventDetailsFragmentArgs.Builder()
                    .setEventId(eventID)
                    .build()
                    .toBundle()
                    .also { bundle ->
                        Navigation.findNavController(view).navigate(R.id.eventDetailsFragment,
                            bundle,
                            getAnimFade(),
                            extras)
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

    private fun performSearch(args: SearchResultsFragmentArgs) {
        val query = args.query
        val location = args.location
        val date = args.date
        searchViewModel.searchEvent = query
        searchViewModel.loadEvents(location, date)
    }

    private fun showNoSearchResults(events: List<Event>) {
        rootView.noSearchResults.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showNoInternetError(show: Boolean) {
        rootView.noInternetCard.visibility = if (show) View.VISIBLE else View.GONE
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
