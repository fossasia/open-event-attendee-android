package org.fossasia.openevent.general.order

import android.content.ActivityNotFoundException
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_rate_us.view.rateEventyayButton
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
import org.fossasia.openevent.general.common.FavoriteFabClickListener
import org.fossasia.openevent.general.data.Preference
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.similarevent.SimilarEventsListAdapter
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.jetbrains.anko.design.longSnackbar
import android.net.Uri
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.event.RedirectToLogin
import org.fossasia.openevent.general.search.ORDER_COMPLETED_FRAGMENT

private const val DISPLAY_RATING_DIALOG = "displayRatingDialog"

class OrderCompletedFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var eventShare: Event
    private val safeArgs: OrderCompletedFragmentArgs by navArgs()
    private val orderCompletedViewModel by viewModel<OrderCompletedViewModel>()
    private val similarEventsAdapter = SimilarEventsListAdapter()
    private val preferences = Preference()

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

        displayRateEventyayAlertDialog()

        orderCompletedViewModel.loadEvent(safeArgs.eventId)
        orderCompletedViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadEventDetails(it)
                eventShare = it
                val eventTopicId = it.eventTopic?.id ?: 0
                val location = it.searchableLocationName ?: it.locationName
                orderCompletedViewModel.fetchSimilarEvents(safeArgs.eventId, eventTopicId, location)
            })

        orderCompletedViewModel.similarEvents
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                similarEventsAdapter.submitList(it)
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
                    rootView.similarEventLayout.isVisible = true
                } else {
                    rootView.shimmerSimilarEvents.stopShimmer()
                    rootView.similarEventLayout.isVisible = similarEventsAdapter.currentList?.isEmpty() ?: true
                }
            })

        rootView.add.setOnClickListener {
            startCalendar(eventShare)
        }

        rootView.view.setOnClickListener {
            openTicketDetails()
        }

        rootView.share.setOnClickListener {
            EventUtils.share(eventShare, requireContext())
        }

        return rootView
    }

    private fun displayRateEventyayAlertDialog() {
        if (!preferences.getBoolean(DISPLAY_RATING_DIALOG, true))
            return
        val layout = layoutInflater.inflate(R.layout.dialog_rate_us, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(layout)
            .setNeutralButton(getString(R.string.no_thanks)) { _, _ ->
                preferences.putBoolean(DISPLAY_RATING_DIALOG, false)
            }.setPositiveButton(getString(R.string.maybe_later)) { _, _ ->
                preferences.putBoolean(DISPLAY_RATING_DIALOG, true)
            }.show()
        layout.rateEventyayButton.setOnClickListener {
            alertDialog.dismiss()
            val appPackageName = BuildConfig.APPLICATION_ID
            preferences.putBoolean(DISPLAY_RATING_DIALOG, false)
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventClickListener: EventClickListener = object : EventClickListener {
            override fun onClick(eventID: Long, imageView: ImageView) {
                findNavController(rootView)
                    .navigate(OrderCompletedFragmentDirections.actionOrderCompletedToEventDetail(eventID),
                        FragmentNavigatorExtras(imageView to "eventDetailImage"))
            }
        }

        val redirectToLogin = object : RedirectToLogin {
            override fun goBackToLogin() {
                findNavController(rootView).navigate(OrderCompletedFragmentDirections
                    .actionOrderCompletedToAuth(redirectedFrom = ORDER_COMPLETED_FRAGMENT))
            }
        }

        val favFabClickListener: FavoriteFabClickListener = object : FavoriteFabClickListener {
            override fun onClick(event: Event, itemPosition: Int) {
                if (orderCompletedViewModel.isLoggedIn()) {
                    orderCompletedViewModel.setFavorite(event, !event.favorite)
                    event.favorite = !event.favorite
                    similarEventsAdapter.notifyItemChanged(itemPosition)
                } else {
                    EventUtils.showLoginToLikeDialog(requireContext(),
                        layoutInflater, redirectToLogin, event.originalImageUrl, event.name)
                }
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

    private fun redirectToEventsFragment() {
        findNavController(rootView).navigate(OrderCompletedFragmentDirections.actionOrderCompletedToEvents())
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
