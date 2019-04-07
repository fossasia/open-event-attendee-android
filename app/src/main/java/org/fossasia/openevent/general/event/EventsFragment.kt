package org.fossasia.openevent.general.event

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
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
import kotlinx.android.synthetic.main.fragment_events.view.eventsNestedScrollView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.ScrollToTop
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.common.ShareFabClickListener
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.di.Scopes
import org.fossasia.openevent.general.search.SAVED_LOCATION
import org.fossasia.openevent.general.utils.Utils.isNetworkConnected
import org.fossasia.openevent.general.utils.Utils.getAnimFade
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ext.android.bindScope
import org.koin.androidx.scope.ext.android.getOrCreateScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import org.fossasia.openevent.general.utils.Utils.setToolbar

/**
 * Enum class for different layout types in the adapter.
 * This class can expand as number of layout types grow.
 */
enum class EventLayoutType {
    EVENTS, SIMILAR_EVENTS
}

const val EVENT_DATE_FORMAT: String = "eventDateFormat"
const val RELOADING_EVENTS: Int = 0
const val INITIAL_FETCHING_EVENTS: Int = 1

class EventsFragment : Fragment(), ScrollToTop {
    private val eventsViewModel by viewModel<EventsViewModel>()
    private lateinit var rootView: View
    private val preference = Preference()
    private val eventsListAdapter: EventsListAdapter by inject(
        scope = getOrCreateScope(Scopes.EVENTS_FRAGMENT.toString())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindScope(getOrCreateScope(Scopes.EVENTS_FRAGMENT.toString()))

        eventsViewModel.events
            .nonNull()
            .observe(this, Observer { list ->
                eventsListAdapter.submitList(list)
                showEmptyMessage(list.size)
                Timber.d("Fetched events of size %s", eventsListAdapter.itemCount)
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
        setToolbar(activity, "Events", false)

        rootView.progressBar.isIndeterminate = true

        rootView.eventsRecycler.layoutManager =
            GridLayoutManager(activity, resources.getInteger(R.integer.events_column_count))

        rootView.eventsRecycler.adapter = eventsListAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        eventsViewModel.showShimmerEvents
            .nonNull()
            .observe(viewLifecycleOwner, Observer { shouldShowShimmer ->
                if (shouldShowShimmer) {
                    rootView.shimmerEvents.startShimmer()
                    eventsListAdapter.clear()
                } else {
                    rootView.shimmerEvents.stopShimmer()
                }
                rootView.shimmerEvents.isVisible = shouldShowShimmer
            })

        eventsViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.swiperefresh.isRefreshing = it
            })

        eventsViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(eventsNestedScrollView, it, Snackbar.LENGTH_LONG).show()
            })

        eventsViewModel.loadLocation()
        rootView.locationTextView.text = eventsViewModel.savedLocation
        eventsViewModel.loadLocationEvents(INITIAL_FETCHING_EVENTS)

        rootView.locationTextView.setOnClickListener {
            findNavController(rootView).navigate(R.id.searchLocationFragment, null, getAnimSlide())
        }

        showNoInternetScreen(!isNetworkConnected(context) && eventsViewModel.events.value.isNullOrEmpty())

        rootView.retry.setOnClickListener {
            val isNetworkConnected = isNetworkConnected(context)
            if (eventsViewModel.savedLocation != null && isNetworkConnected) {
                eventsViewModel.loadLocationEvents(RELOADING_EVENTS)
            }
            showNoInternetScreen(!isNetworkConnected)
        }

        rootView.swiperefresh.setColorSchemeColors(Color.BLUE)
        rootView.swiperefresh.setOnRefreshListener {
            showNoInternetScreen(!isNetworkConnected(context))
            if (!isNetworkConnected(context)) {
                rootView.swiperefresh.isRefreshing = false
            } else {
                eventsViewModel.loadLocationEvents(RELOADING_EVENTS)
            }
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
                    findNavController(view).navigate(R.id.eventDetailsFragment, bundle, getAnimFade())
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
                eventsViewModel.setFavorite(event.id, !event.favorite)
                event.favorite = !event.favorite
                eventsListAdapter.notifyItemChanged(itemPosition)
            }
        }

        eventsListAdapter.apply {
            onEventClick = eventClickListener
            onShareFabClick = shareFabClickListener
            onFavFabClick = favFabClickListener
        }
    }

    private fun showNoInternetScreen(show: Boolean) {
        rootView.homeScreenLL.visibility = if (!show) View.VISIBLE else View.GONE
        rootView.noInternetCard.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyMessage(itemCount: Int) {
        rootView.noEventsMessage.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        rootView.swiperefresh?.setOnRefreshListener(null)
        super.onStop()
    }

    override fun scrollToTop() = rootView.eventsNestedScrollView.smoothScrollTo(0, 0)
}
