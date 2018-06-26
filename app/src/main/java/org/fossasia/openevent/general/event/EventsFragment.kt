package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.content_no_internet.view.*
import kotlinx.android.synthetic.main.fragment_events.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.search.SearchLocationActivity
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

//String constants for event types
const val EVENTS: String = "events"
const val SIMILAR_EVENTS: String = "similarEvents"
const val EVENT_DATE_FORMAT: String = "eventDateFormat"

class EventsFragment : Fragment() {
    private val eventsRecyclerAdapter: EventsRecyclerAdapter = EventsRecyclerAdapter()
    private val eventsViewModel by viewModel<EventsViewModel>()
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventsRecyclerAdapter.setEventLayout(EVENTS)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_events, container, false)

        rootView.progressBar.isIndeterminate = true

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.eventsRecycler.adapter = eventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.rootLayout, fragment)?.addToBackStack(null)?.commit()
            }
        }

        val favouriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(event: Event, isFavourite: Boolean) {
                val id = eventsRecyclerAdapter.getPos(event.id)
                eventsViewModel.setFavorite(event.id, !isFavourite)
                event.favorite = !event.favorite
                eventsRecyclerAdapter.notifyItemChanged(id)
            }
        }
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)
        eventsRecyclerAdapter.setFavorite(favouriteFabClickListener)
        eventsViewModel.events.observe(this, Observer {
            it?.let {
                eventsRecyclerAdapter.addAll(it)
                eventsRecyclerAdapter.notifyDataSetChanged()
            }
            Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
        })

        eventsViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventsViewModel.progress.observe(this, Observer {
            it?.let { Utils.showProgressBar(rootView.progressBar, it) }
        })

        if (eventsViewModel.savedLocation != null) {
            rootView.locationTextView.text = eventsViewModel.savedLocation
            eventsViewModel.loadLocationEvents(eventsViewModel.savedLocation.toString())
        } else {
            rootView.locationTextView.text = "where?"
        }

        rootView.locationTextView.setOnClickListener {
            val intent = Intent(activity, SearchLocationActivity::class.java)
            startActivity(intent)
        }

        showNoInternetScreen(isNetworkConnected())

        rootView.retry.setOnClickListener {
            val isNetworkConnected = isNetworkConnected()
            if (eventsViewModel.savedLocation != null && isNetworkConnected){
                eventsViewModel.loadLocationEvents(eventsViewModel.savedLocation.toString())
            }
            showNoInternetScreen(isNetworkConnected)
        }

        rootView.swiperefresh.setColorSchemeColors(Color.BLUE)
        rootView.swiperefresh.setOnRefreshListener({
            eventsViewModel.loadLocationEvents(eventsViewModel.savedLocation.toString())
            rootView.swiperefresh.isRefreshing = false
        })

        return rootView
    }

    override fun onStop() {
        if (rootView.swiperefresh != null)
            rootView.swiperefresh.setOnRefreshListener(null)
        super.onStop()
    }

    private fun showNoInternetScreen(show: Boolean) {
        rootView.homeScreenLL.visibility = if (show) View.VISIBLE else View.GONE
        rootView.noInternetCard.visibility = if (!show) View.VISIBLE else View.GONE
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        return connectivityManager?.activeNetworkInfo != null
    }

    override fun onStop() {
        if (rootView.swiperefresh != null)
            rootView.swiperefresh.setOnRefreshListener(null)
        super.onStop()
    }
}