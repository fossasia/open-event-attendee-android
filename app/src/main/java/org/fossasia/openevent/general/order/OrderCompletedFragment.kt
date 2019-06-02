package org.fossasia.openevent.general.order

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_event.view.eventImage
import kotlinx.android.synthetic.main.fragment_order_completed.view.similarEventsRecycler
import kotlinx.android.synthetic.main.fragment_order_completed.view.similarEventLayout
import kotlinx.android.synthetic.main.fragment_order_completed.view.shimmerSimilarEvents
import kotlinx.android.synthetic.main.fragment_order_completed.view.orderCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_order_completed.view.add
import kotlinx.android.synthetic.main.fragment_order_completed.view.name
import kotlinx.android.synthetic.main.fragment_order_completed.view.share
import kotlinx.android.synthetic.main.fragment_order_completed.view.time
import kotlinx.android.synthetic.main.fragment_order_completed.view.view
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.EventClickListener
import org.fossasia.openevent.general.common.EventsDiffCallback
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventViewHolder
import org.fossasia.openevent.general.event.EventsListAdapter
import org.fossasia.openevent.general.event.EventLayoutType
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.jetbrains.anko.design.longSnackbar

class OrderCompletedFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var eventShare: Event
    private val safeArgs: OrderCompletedFragmentArgs by navArgs()
    private val orderCompletedViewModel by viewModel<OrderCompletedViewModel>()
    private val similarEventsAdapter = EventsListAdapter(EventLayoutType.SIMILAR_EVENTS, EventsDiffCallback())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_order_completed, container, false)
        setToolbar(activity)
        setHasOptionsMenu(true)

        val similarLinearLayoutManager = LinearLayoutManager(context)
        similarLinearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.similarEventsRecycler.layoutManager = similarLinearLayoutManager
        rootView.similarEventsRecycler.adapter = similarEventsAdapter

        orderCompletedViewModel.loadEvent(safeArgs.eventId)
        orderCompletedViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadEventDetails(it)
                eventShare = it
                val eventTopicId = it.eventTopic?.id ?: 0
                orderCompletedViewModel.fetchSimilarEvents(safeArgs.eventId, eventTopicId)
            })

        orderCompletedViewModel.similarEvents
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                similarEventsAdapter.submitList(it.toList())
                rootView.similarEventLayout.isVisible = it.isNotEmpty()
            })

        orderCompletedViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.orderCoordinatorLayout.longSnackbar(it)
            })

        orderCompletedViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.shimmerSimilarEvents.isVisible = it
                if (it) {
                    rootView.shimmerSimilarEvents.startShimmer()
                } else {
                    rootView.shimmerSimilarEvents.stopShimmer()
                }
            })

        rootView.add.setOnClickListener {
            startCalendar(eventShare)
        }

        rootView.view.setOnClickListener {
            openTicketDetails()
        }

        rootView.share.setOnClickListener {
            shareEvent(eventShare)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long, itemPosition: Int) {
                var extras: Navigator.Extras? = null
                val itemEventViewHolder = rootView.similarEventsRecycler.findViewHolderForAdapterPosition(itemPosition)
                itemEventViewHolder?.let {
                    if (itemEventViewHolder is EventViewHolder) {
                        extras = FragmentNavigatorExtras(
                            itemEventViewHolder.itemView.eventImage to "eventDetailImage")
                    }
                }
                extras?.let {
                    findNavController(view)
                        .navigate(OrderCompletedFragmentDirections.actionOrderCompletedToEventDetail(eventID), it)
                } ?: findNavController(view)
                    .navigate(OrderCompletedFragmentDirections.actionOrderCompletedToEventDetail(eventID))
            }
        }

        val favFabClickListener: FavoriteFabClickListener = object : FavoriteFabClickListener {
            override fun onClick(event: Event, itemPosition: Int) {
                orderCompletedViewModel.setFavorite(event.id, !event.favorite)
                event.favorite = !event.favorite
                similarEventsAdapter.notifyItemChanged(itemPosition)
            }
        }

        similarEventsAdapter.apply {
            onEventClick = eventClickListener
            onFavFabClick = favFabClickListener
        }
    }

    private fun loadEventDetails(event: Event) {
        val dateString = StringBuilder()
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)

        rootView.name.text = event.name
        rootView.time.text = dateString.append(EventUtils.getFormattedDateShort(startsAt))
                .append(" • ")
                .append(EventUtils.getFormattedTime(startsAt))
                .append(" ")
                .append(EventUtils.getFormattedTimeZone(startsAt))
    }

    private fun startCalendar(event: Event) {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description?.stripHtml())
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.locationName)
        intent.putExtra(CalendarContract.Events.CALENDAR_TIME_ZONE, event.timezone)
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
            EventUtils.getTimeInMilliSeconds(event.startsAt, event.timezone))
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
            EventUtils.getTimeInMilliSeconds(event.endsAt, event.timezone))
        startActivity(intent)
    }

    private fun shareEvent(event: Event) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
    }

    private fun redirectToEventsFragment() {
        findNavController(rootView).popBackStack(R.id.eventsFragment, false)
    }

    private fun openEventDetails() {
        findNavController(rootView).popBackStack(R.id.eventDetailsFragment, false)
    }

    private fun openTicketDetails() {
        findNavController(rootView).navigate(OrderCompletedFragmentDirections.actionOrderCompletedToOrderUser())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_completed, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                openEventDetails()
                true
            }
            R.id.tick -> {
                redirectToEventsFragment()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
