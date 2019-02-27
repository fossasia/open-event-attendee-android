package org.fossasia.openevent.general.event

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.content_no_internet.view.noInternetCard
import kotlinx.android.synthetic.main.content_no_internet.view.retry
import kotlinx.android.synthetic.main.fragment_events.eventsNestedScrollView
import kotlinx.android.synthetic.main.fragment_events.view.eventsRecycler
import kotlinx.android.synthetic.main.fragment_events.view.homeScreenLL
import kotlinx.android.synthetic.main.fragment_events.view.locationTextView
import kotlinx.android.synthetic.main.fragment_events.view.progressBar
import kotlinx.android.synthetic.main.fragment_events.view.shimmerEvents
import kotlinx.android.synthetic.main.fragment_events.view.swiperefresh
import kotlinx.android.synthetic.main.fragment_events.view.noEventsMessage
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.search.SAVED_LOCATION
import org.fossasia.openevent.general.utils.Utils.isNetworkConnected
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

// String constants for event types
const val EVENTS: String = "events"
const val SIMILAR_EVENTS: String = "similarEvents"
const val EVENT_DATE_FORMAT: String = "eventDateFormat"

class EventsFragment : Fragment() {
    private val eventsRecyclerAdapter: EventsRecyclerAdapter = EventsRecyclerAdapter()
    private val eventsViewModel by viewModel<EventsViewModel>()
    private lateinit var rootView: View
    private val preference = Preference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventsRecyclerAdapter.setEventLayout(EVENTS)
        val favoriteFabClickListener = object : FavoriteFabListener {
            override fun onClick(event: Event, isFavorite: Boolean) {
                val id = eventsRecyclerAdapter.getPos(event.id)
                eventsViewModel.setFavorite(event.id, !isFavorite)
                event.favorite = !event.favorite
                eventsRecyclerAdapter.notifyItemChanged(id)
            }
        }
        eventsRecyclerAdapter.setFavorite(favoriteFabClickListener)

        eventsViewModel.events
            .nonNull()
            .observe(this, Observer {
                eventsRecyclerAdapter.addAll(it)
                eventsRecyclerAdapter.notifyDataSetChanged()
                showEmptyMessage(eventsRecyclerAdapter.itemCount)
                Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
            })

        eventsViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(eventsNestedScrollView, it, Snackbar.LENGTH_LONG).show()
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_events, container, false)

        if (preference.getString(SAVED_LOCATION).isNullOrEmpty()) {
            findNavController(requireActivity(), R.id.frameContainer).navigate(R.id.welcomeFragment)
        }

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.show()
            thisActivity.supportActionBar?.title = "Events"
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        rootView.progressBar.isIndeterminate = true

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.eventsRecycler.adapter = eventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val bundle = Bundle()
                bundle.putLong(EVENT_ID, eventID)
                findNavController(rootView).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
            }
        }
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)

        eventsViewModel.showShimmerEvents
            .nonNull()
            .observe(this, Observer {
                if (it) {
                    rootView.shimmerEvents.startShimmer()
                } else {
                    rootView.shimmerEvents.stopShimmer()
                }
                rootView.shimmerEvents.isVisible = it
                if (it) {
                    eventsRecyclerAdapter.removeAll()
                    eventsRecyclerAdapter.notifyDataSetChanged()
                }
            })

        eventsViewModel.progress
            .nonNull()
            .observe(this, Observer {
                rootView.swiperefresh.isRefreshing = it
            })

        eventsViewModel.loadLocation()
        rootView.locationTextView.text = eventsViewModel.savedLocation
        eventsViewModel.loadLocationEvents()

        rootView.locationTextView.setOnClickListener {
            findNavController(rootView).navigate(R.id.searchLocationFragment, null, getAnimSlide())
        }

        showNoInternetScreen(isNetworkConnected(context))

        rootView.retry.setOnClickListener {
            val isNetworkConnected = isNetworkConnected(context)
            if (eventsViewModel.savedLocation != null && isNetworkConnected) {
                eventsViewModel.loadLocationEvents()
            }
            showNoInternetScreen(isNetworkConnected)
        }

        rootView.swiperefresh.setColorSchemeColors(Color.BLUE)
        rootView.swiperefresh.setOnRefreshListener {
            showNoInternetScreen(isNetworkConnected(context))
            if (!isNetworkConnected(context)) {
                rootView.swiperefresh.isRefreshing = false
            } else {
                eventsViewModel.loadLocationEvents()
            }
        }

        return rootView
    }

    private fun showNoInternetScreen(show: Boolean) {
        rootView.homeScreenLL.visibility = if (show) View.VISIBLE else View.GONE
        rootView.noInternetCard.visibility = if (!show) View.VISIBLE else View.GONE
    }

    private fun showEmptyMessage(itemCount: Int) {
        rootView.noEventsMessage.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        rootView.swiperefresh?.setOnRefreshListener(null)
        super.onStop()
    }
}
