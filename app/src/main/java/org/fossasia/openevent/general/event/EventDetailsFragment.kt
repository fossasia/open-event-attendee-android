package org.fossasia.openevent.general.event

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_event.aboutEventContainer
import kotlinx.android.synthetic.main.content_event.locationContainer
import kotlinx.android.synthetic.main.content_event.organizerContainer
import kotlinx.android.synthetic.main.content_event.similarEventsContainer
import kotlinx.android.synthetic.main.content_event.view.eventDateDetailsFirst
import kotlinx.android.synthetic.main.content_event.view.eventDateDetailsSecond
import kotlinx.android.synthetic.main.content_event.view.eventDescription
import kotlinx.android.synthetic.main.content_event.view.eventLocationLinearLayout
import kotlinx.android.synthetic.main.content_event.view.eventLocationTextView
import kotlinx.android.synthetic.main.content_event.view.eventName
import kotlinx.android.synthetic.main.content_event.view.eventOrganiserDescription
import kotlinx.android.synthetic.main.content_event.view.eventOrganiserName
import kotlinx.android.synthetic.main.content_event.view.eventTimingLinearLayout
import kotlinx.android.synthetic.main.content_event.view.imageMap
import kotlinx.android.synthetic.main.content_event.view.locationUnderMap
import kotlinx.android.synthetic.main.content_event.view.eventImage
import kotlinx.android.synthetic.main.content_event.view.organizerLogoIcon
import kotlinx.android.synthetic.main.content_event.view.nestedContentEventScroll
import kotlinx.android.synthetic.main.content_event.view.organizerName
import kotlinx.android.synthetic.main.content_event.view.refundPolicy
import kotlinx.android.synthetic.main.content_event.view.seeMore
import kotlinx.android.synthetic.main.content_event.view.seeMoreOrganizer
import kotlinx.android.synthetic.main.content_event.view.organizerContainer
import kotlinx.android.synthetic.main.fragment_event.view.buttonTickets
import kotlinx.android.synthetic.main.fragment_event.view.eventErrorCard
import kotlinx.android.synthetic.main.fragment_event.view.container
import kotlinx.android.synthetic.main.content_fetching_event_error.view.retry
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.about.AboutEventFragmentArgs
import org.fossasia.openevent.general.event.EventUtils.loadMapUrl
import org.fossasia.openevent.general.event.topic.SimilarEventsFragment
import org.fossasia.openevent.general.social.SocialLinksFragment
import org.fossasia.openevent.general.ticket.TicketsFragmentArgs
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Currency
import org.fossasia.openevent.general.utils.Utils.setToolbar

const val EVENT_ID = "eventId"
const val EVENT_TOPIC_ID = "eventTopicId"
const val EVENT_LOCATION = "eventLocation"

class EventDetailsFragment : Fragment() {
    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private val safeArgs: EventDetailsFragmentArgs by navArgs()

    private lateinit var rootView: View
    private var eventTopicId: Long? = null
    private var eventLocation: String? = null
    private lateinit var eventShare: Event
    private var currency: String? = null
    private val LINE_COUNT: Int = 3
    private val LINE_COUNT_ORGANIZER: Int = 2
    private var menuActionBar: Menu? = null
    private var title: String = ""
    private var runOnce: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventViewModel.event
            .nonNull()
            .observe(this, Observer {
                loadEvent(it)
                eventShare = it
                title = eventShare.name

                // Update favorite icon and external event url menu option
                activity?.invalidateOptionsMenu()

                if (runOnce) {
                    loadSocialLinksFragment()
                    loadSimilarEventsFragment()
                }
                runOnce = false

                Timber.d("Fetched events of id %d", safeArgs.eventId)
                showEventErrorScreen(false)
                setHasOptionsMenu(true)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_event, container, false)
        setToolbar(activity)
        setHasOptionsMenu(true)

        rootView.buttonTickets.setOnClickListener {
            loadTicketFragment()
        }

        eventViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                showEventErrorScreen(true)
            })

        eventViewModel.loadEvent(safeArgs.eventId)
        rootView.retry.setOnClickListener {
            eventViewModel.loadEvent(safeArgs.eventId)
        }

        // Set toolbar title to event name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rootView.nestedContentEventScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (scrollY > rootView.eventName.height + rootView.eventImage.height)
                    /*Toolbar title set to name of Event if scrolled more than
                    combined height of eventImage and eventName views*/
                    setToolbar(activity, title)
                else
                    // Toolbar title set to an empty string
                    setToolbar(activity)
            }
        }

        return rootView
    }

    private fun loadEvent(event: Event) {
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)

        rootView.eventName.text = event.name

        // Organizer Section
        if (!event.organizerName.isNullOrEmpty()) {
            rootView.eventOrganiserName.text = "by " + event.organizerName.nullToEmpty()
            setTextField(rootView.eventOrganiserDescription, event.organizerDescription?.stripHtml()?.trim())
            rootView.organizerName.text = event.organizerName.nullToEmpty()
            rootView.eventOrganiserName.isVisible = true
            organizerContainer.isVisible = true

            Picasso.get()
                    .load(event.logoUrl)
                    .placeholder(requireDrawable(requireContext(), R.drawable.ic_person_black))
                    .transform(CircleTransform())
                    .into(rootView.organizerLogoIcon)

            val organizerDescriptionListener = View.OnClickListener {
                if (rootView.seeMoreOrganizer.text == getString(R.string.see_more)) {
                    rootView.seeMoreOrganizer.text = getString(R.string.see_less)
                    rootView.eventOrganiserDescription.minLines = 0
                    rootView.eventOrganiserDescription.maxLines = Int.MAX_VALUE
                } else {
                    rootView.seeMoreOrganizer.text = getString(R.string.see_more)
                    rootView.eventOrganiserDescription.setLines(3)
                }
            }

            rootView.eventOrganiserDescription.post {
                if (rootView.eventOrganiserDescription.lineCount > LINE_COUNT_ORGANIZER) {
                    rootView.seeMoreOrganizer.isVisible = true
                    // Set up toggle organizer description
                    rootView.seeMoreOrganizer.setOnClickListener(organizerDescriptionListener)
                    rootView.eventOrganiserDescription.setOnClickListener(organizerDescriptionListener)
                }
            }
        } else {
            rootView.organizerContainer.isVisible = false
        }

        currency = Currency.getInstance(event.paymentCurrency).symbol
        // About event on-click
        val aboutEventOnClickListener = View.OnClickListener {
            AboutEventFragmentArgs.Builder()
                .setEventId(safeArgs.eventId)
                .build()
                .toBundle()
                .also { bundle ->
                    findNavController(rootView).navigate(R.id.aboutEventFragment, bundle, getAnimSlide())
                }
        }

        // Event Description Section
        val description = event.description.stripHtml()
        if (!description.isNullOrEmpty()) {
            setTextField(rootView.eventDescription, description)

            rootView.eventDescription.post {
                if (rootView.eventDescription.lineCount > LINE_COUNT) {
                    rootView.seeMore.isVisible = true
                    // start about fragment
                    rootView.eventDescription.setOnClickListener(aboutEventOnClickListener)
                    rootView.seeMore.setOnClickListener(aboutEventOnClickListener)
                }
            }
        } else {
            aboutEventContainer.isVisible = false
        }

        // Map Section
        if (!event.locationName.isNullOrEmpty()) {
            locationContainer.isVisible = true
            rootView.eventLocationTextView.text = event.locationName
        }

        // load location to map
        val mapClickListener = View.OnClickListener { startMap(event) }

        val locationNameIsEmpty = event.locationName.isNullOrEmpty()
        locationContainer.isVisible = !locationNameIsEmpty
        rootView.eventLocationLinearLayout.isVisible = !locationNameIsEmpty
        rootView.locationUnderMap.isVisible = !locationNameIsEmpty
        rootView.imageMap.isVisible = !locationNameIsEmpty
        if (!locationNameIsEmpty) {
            rootView.locationUnderMap.text = event.locationName
            rootView.imageMap.setOnClickListener(mapClickListener)
            rootView.eventLocationLinearLayout.setOnClickListener(mapClickListener)

            Picasso.get()
                    .load(eventViewModel.loadMap(event))
                    .placeholder(R.drawable.ic_map_black)
                    .error(R.drawable.ic_map_black)
                    .into(rootView.imageMap)
        }

        // Date and Time section
        rootView.eventDateDetailsFirst.text = EventUtils.getFormattedEventDateTimeRange(startsAt, endsAt)
        rootView.eventDateDetailsSecond.text = EventUtils.getFormattedEventDateTimeRangeSecond(startsAt, endsAt)

        // Refund policy
        rootView.refundPolicy.text = event.refundPolicy

        // Similar Events Section
        if (event.eventTopic != null || !event.locationName.isNullOrBlank() ||
            !event.searchableLocationName.isNullOrBlank()) {
            similarEventsContainer.isVisible = true
            eventTopicId = event.eventTopic?.id
            eventLocation =
                if (event.searchableLocationName.isNullOrBlank()) event.locationName
                else event.searchableLocationName
        }

        // Set Cover Image
        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.header)
                    .into(rootView.eventImage)
        }

        // Add event to Calendar
        val dateClickListener = View.OnClickListener { startCalendar(event) }
        rootView.eventTimingLinearLayout.setOnClickListener(dateClickListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.add_to_calendar -> {
                // Add event to Calendar
                startCalendar(eventShare)
                true
            }
            R.id.report_event -> {
                reportEvent(eventShare)
                true
            }
            R.id.open_external_event_url -> {
                eventShare.externalEventUrl?.let { Utils.openUrl(requireContext(), it) }
                true
            }
            R.id.favorite_event -> {
                eventViewModel.setFavorite(safeArgs.eventId, !(eventShare.favorite))
                true
            }
            R.id.event_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(eventShare))
                sendIntent.type = "text/plain"
                rootView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setTextField(textView: TextView, value: String?) {
        when (value.isNullOrBlank()) {
            true -> textView.isVisible = false
            false -> textView.text = value
        }
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

    private fun reportEvent(event: Event) {
        val email = "support@eventyay.com"
        val subject = "Report of ${event.name} (${event.identifier})"
        val body = "Let us know what's wrong"
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)

        startActivity(Intent.createChooser(emailIntent, "Chooser Title"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.event_details, menu)
        menuActionBar = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (::eventShare.isInitialized) {
            if (eventShare.externalEventUrl == null) {
                menu.findItem(R.id.open_external_event_url).isVisible = false
            }
            setFavoriteIconFilled(eventShare.favorite)
        }
        super.onPrepareOptionsMenu(menu)
    }

    private fun loadTicketFragment() {
        TicketsFragmentArgs.Builder()
            .setEventId(safeArgs.eventId)
            .setCurrency(currency)
            .build()
            .toBundle()
            .also { bundle ->
                findNavController(rootView).navigate(R.id.ticketsFragment, bundle, getAnimSlide())
            }
    }

    private fun loadSocialLinksFragment() {
        // Initialise SocialLinks Fragment
        val socialLinksFragemnt = SocialLinksFragment()
        val bundle = Bundle()
        bundle.putLong(EVENT_ID, safeArgs.eventId)
        socialLinksFragemnt.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.frameContainerSocial, socialLinksFragemnt).commit()
    }

    private fun loadSimilarEventsFragment() {
        // Initialise SimilarEvents Fragment
        val similarEventsFragment = SimilarEventsFragment()
        val bundle = Bundle()
        bundle.putLong(EVENT_ID, safeArgs.eventId)
        eventTopicId?.let { bundle.putLong(EVENT_TOPIC_ID, it) }
        eventLocation?.let { bundle.putString(EVENT_LOCATION, it) }
        similarEventsFragment.arguments = bundle
        childFragmentManager.beginTransaction()
            .replace(R.id.frameContainerSimilarEvents, similarEventsFragment).commit()
    }

    private fun startMap(event: Event) {
        // start map intent
        val mapUrl = loadMapUrl(event)
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        val packageManager = activity?.packageManager
        if (packageManager != null && mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
    }

    private fun setFavoriteIconFilled(filled: Boolean) {
        val id = when {
            filled -> R.drawable.ic_baseline_favorite_white
            else -> R.drawable.ic_baseline_favorite_border_white
        }
        menuActionBar?.findItem(R.id.favorite_event)?.icon = ContextCompat.getDrawable(requireContext(), id)
    }

    private fun showEventErrorScreen(show: Boolean) {
        rootView.container.isVisible = !show
        rootView.eventErrorCard.isVisible = show
        val menuItemSize = menuActionBar?.size() ?: 0
        for (i in 0..(menuItemSize - 1)) {
            menuActionBar?.getItem(i)?.isVisible = !show
        }
    }
}
