package org.fossasia.openevent.general.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_search_results.view.searchRootLayout
import kotlinx.android.synthetic.main.fragment_search_results.view.eventsRecycler
import kotlinx.android.synthetic.main.fragment_search_results.view.shimmerSearch
import kotlinx.android.synthetic.main.fragment_search_results.view.errorTextView
import kotlinx.android.synthetic.main.fragment_search_results.view.noSearchResults
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDetailsFragmentArgs
import org.fossasia.openevent.general.event.FavoriteFabListener
import org.fossasia.openevent.general.event.RecyclerViewClickListener
import org.fossasia.openevent.general.favorite.FavoriteEventsRecyclerAdapter
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SearchResultsFragment : Fragment() {
    private lateinit var rootView: View
    private val eventsRecyclerAdapter: FavoriteEventsRecyclerAdapter = FavoriteEventsRecyclerAdapter()
    private val searchViewModel by viewModel<SearchViewModel>()
    private val safeArgs: SearchResultsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performSearch(safeArgs)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_results, container, false)

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = getString(R.string.search_results)
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(context)

        rootView.eventsRecycler.adapter = eventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                EventDetailsFragmentArgs.Builder()
                    .setEventId(eventID)
                    .build()
                    .toBundle()
                    .also { bundle ->
                        findNavController(rootView).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
                    }
            }
        }

        val favoriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(event: Event, isFavorite: Boolean) {
                val id = eventsRecyclerAdapter.getPos(event.id)
                searchViewModel.setFavorite(event.id, !isFavorite)
                event.favorite = !event.favorite
                eventsRecyclerAdapter.notifyItemChanged(id)
            }
        }
        eventsRecyclerAdapter.setFavorite(favoriteFabClickListener)
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)
        searchViewModel.events
            .nonNull()
            .observe(this, Observer {
                eventsRecyclerAdapter.addAll(it)
                eventsRecyclerAdapter.notifyDataSetChanged()
                showNoSearchResults(it)
                Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
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

        rootView.errorTextView.setOnClickListener {
            performSearch(safeArgs)
        }

        return rootView
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
        rootView.errorTextView.visibility = if (show) View.VISIBLE else View.GONE
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
