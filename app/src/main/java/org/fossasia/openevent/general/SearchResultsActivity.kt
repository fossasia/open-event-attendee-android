package org.fossasia.openevent.general

import android.arch.lifecycle.Observer
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_search_results.*
import org.fossasia.openevent.general.event.*
import org.fossasia.openevent.general.favorite.FavoriteEventsRecyclerAdapter
import org.fossasia.openevent.general.search.DATE
import org.fossasia.openevent.general.search.LOCATION
import org.fossasia.openevent.general.search.QUERY
import org.fossasia.openevent.general.search.SearchViewModel
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class SearchResultsActivity : AppCompatActivity() {
    private val eventsRecyclerAdapter: FavoriteEventsRecyclerAdapter = FavoriteEventsRecyclerAdapter()
    private val searchViewModel by viewModel<SearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.search_results)

        progressBar.isIndeterminate = true
        eventsRecycler.layoutManager = LinearLayoutManager(this)

        eventsRecycler.adapter = eventsRecyclerAdapter
        eventsRecycler.isNestedScrollingEnabled = false
        performSearch(intent)

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(EVENT_ID, eventID)
                fragment.arguments = bundle
                supportFragmentManager?.beginTransaction()?.replace(R.id.searchRootLayout, fragment)?.addToBackStack(null)?.commit()
            }
        }

        val favouriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(event: Event, isFavourite: Boolean) {
                val id = eventsRecyclerAdapter.getPos(event.id)
                searchViewModel.setFavorite(event.id, !isFavourite)
                event.favorite = !event.favorite
                eventsRecyclerAdapter.notifyItemChanged(id)
            }
        }
        eventsRecyclerAdapter.setFavorite(favouriteFabClickListener)
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)
        searchViewModel.events.observe(this, Observer {
            it?.let {
                eventsRecyclerAdapter.addAll(it)
                eventsRecyclerAdapter.notifyDataSetChanged()
                showNoSearchResults(it)
            }
            Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
        })

        searchViewModel.progress.observe(this, Observer {
            it?.let { Utils.showProgressBar(progressBar, it) }
        })

        searchViewModel.showNoInternetError.observe(this, Observer {
            it?.let { showNoInternetError(it) }
        })

        searchViewModel.error.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        errorTextView.setOnClickListener {
            performSearch(intent)
        }
    }

    private fun performSearch(intent: Intent?) {
        val query = intent?.getStringExtra(QUERY)
        val location = intent?.getStringExtra(LOCATION)
        val date = intent?.getStringExtra(DATE)
        searchViewModel.searchEvent = query
        if (searchViewModel.savedLocation != null && TextUtils.isEmpty(location) && date == "Anytime")
            searchViewModel.loadEvents(searchViewModel.savedLocation.nullToEmpty(), searchViewModel.savedDate.nullToEmpty())
        else
            searchViewModel.loadEvents(location.toString(), date.toString())
    }

    private fun showNoSearchResults(events: List<Event>) {
        noSearchResults.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showNoInternetError(show: Boolean) {
        errorTextView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                return true
            }
        }
        return false
    }
}
