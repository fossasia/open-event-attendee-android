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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import com.google.android.material.snackbar.Snackbar
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
import kotlinx.android.synthetic.main.content_event.view.imageMap
import kotlinx.android.synthetic.main.content_event.view.locationUnderMap
import kotlinx.android.synthetic.main.content_event.view.logo
import kotlinx.android.synthetic.main.content_event.view.logoIcon
import kotlinx.android.synthetic.main.content_event.view.nestedContentEventScroll
import kotlinx.android.synthetic.main.content_event.view.organizerName
import kotlinx.android.synthetic.main.content_event.view.refundPolicy
import kotlinx.android.synthetic.main.content_event.view.seeMore
import kotlinx.android.synthetic.main.content_event.view.seeMoreOrganizer
import kotlinx.android.synthetic.main.fragment_event.view.eventCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_event.view.buttonTickets
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventUtils.loadMapUrl
import org.fossasia.openevent.general.event.topic.SimilarEventsFragment
import org.fossasia.openevent.general.social.SocialLinksFragment
import org.fossasia.openevent.general.ticket.CURRENCY
import org.fossasia.openevent.general.ticket.TicketsFragment
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Currency

const val EVENT_ID = "EVENT_ID"
const val EVENT_TOPIC_ID = "EVENT_TOPIC_ID"

class EventDetailsFragment : Fragment() {
    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private lateinit var rootView: View
    private var eventId: Long = -1
    private var eventTopicId: Long? = null
    private lateinit var eventShare: Event
    private var currency: String? = null
    private val LINE_COUNT: Int = 3
    private val LINE_COUNT_ORGANIZER: Int = 2
    private var menuActionBar: Menu? = null
    private var title: String = ""
    private var runOnce: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId = bundle.getLong(EVENT_ID, -1)
        }

        eventViewModel.event
            .nonNull()
            .observe(this, Observer {
                loadEvent(it)
                eventShare = it
                title = eventShare.name

                if (eventShare.favorite) {
                    setFavoriteIcon(R.drawable.ic_baseline_favorite_white)
                }

                if (runOnce) {
                    loadSocialLinksFragment()
                    loadSimilarEventsFragment()
                }
                runOnce = false

                Timber.d("Fetched events of id %d", eventId)
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_event, container, false)
        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = ""
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        rootView.buttonTickets.setOnClickListener {
            loadTicketFragment()
        }

        eventViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(rootView.eventCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
            })

        eventViewModel.loadEvent(eventId)

        // Set toolbar title to event name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rootView.nestedContentEventScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (thisActivity is AppCompatActivity) {
                    if (scrollY > rootView.eventName.height + rootView.logo.height)
                        /*Toolbar title set to name of Event if scrolled more than
                        combined height of eventImage and eventName views*/
                        thisActivity.supportActionBar?.title = title
                    else
                        // Toolbar title set to an empty string
                        thisActivity.supportActionBar?.title = ""
                }
            }
        }

        return rootView
    }

    private fun loadEvent(event: Event) {
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)

        rootView.eventName.text = event.name

        // Organizer Section
        if (!event.organizerName.isNullOrEmpty()) {
            rootView.eventOrganiserName.text = "by " + event.organizerName.nullToEmpty()
            setTextField(rootView.eventOrganiserDescription, event.organizerDescription?.stripHtml()?.trim())
            rootView.organizerName.text = event.organizerName.nullToEmpty()
            rootView.eventOrganiserName.visibility = View.VISIBLE
            organizerContainer.visibility = View.VISIBLE

            Picasso.get()
                    .load(event.logoUrl)
                    .placeholder(requireDrawable(requireContext(), R.drawable.ic_person_black))
                    .transform(CircleTransform())
                    .into(rootView.logoIcon)

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
                    rootView.seeMoreOrganizer.visibility = View.VISIBLE
                    // Set up toggle organizer description
                    rootView.seeMoreOrganizer.setOnClickListener(organizerDescriptionListener)
                    rootView.eventOrganiserDescription.setOnClickListener(organizerDescriptionListener)
                }
            }
        }

        currency = Currency.getInstance(event.paymentCurrency).symbol
        // About event on-click
        val aboutEventOnClickListener = View.OnClickListener {
            val bundle = Bundle()
            bundle.putLong(EVENT_ID, eventId)
            findNavController(rootView).navigate(R.id.aboutEventFragment, bundle, getAnimSlide())
        }

        // Event Description Section
        if (!event.description.isNullOrEmpty()) {
            setTextField(rootView.eventDescription, event.description?.stripHtml())

            rootView.eventDescription.post {
                if (rootView.eventDescription.lineCount > LINE_COUNT) {
                    rootView.seeMore.visibility = View.VISIBLE
                    // start about fragment
                    rootView.eventDescription.setOnClickListener(aboutEventOnClickListener)
                    rootView.seeMore.setOnClickListener(aboutEventOnClickListener)
                }
            }
        } else {
            aboutEventContainer.visibility = View.GONE
        }

        // Map Section
        if (!event.locationName.isNullOrEmpty()) {
            locationContainer.visibility = View.VISIBLE
            rootView.eventLocationTextView.text = event.locationName
        }

        // load location to map
        val mapClickListener = View.OnClickListener { startMap(event) }

        val locationNameIsEmpty = event.locationName.isNullOrEmpty()
        locationContainer.isGone = locationNameIsEmpty
        rootView.eventLocationLinearLayout.isGone = locationNameIsEmpty
        rootView.locationUnderMap.isGone = locationNameIsEmpty
        rootView.imageMap.isGone = locationNameIsEmpty
        if (!locationNameIsEmpty) {
            rootView.locationUnderMap.text = event.locationName
            rootView.imageMap.setOnClickListener(mapClickListener)
            rootView.eventLocationTextView.setOnClickListener(mapClickListener)

            Picasso.get()
                    .load(eventViewModel.loadMap(event))
                    .placeholder(R.drawable.ic_map_black)
                    .into(rootView.imageMap)
        }

        // Date and Time section
        rootView.eventDateDetailsFirst.text = EventUtils.getFormattedEventDateTimeRange(startsAt, endsAt)
        rootView.eventDateDetailsSecond.text = EventUtils.getFormattedEventDateTimeRangeSecond(startsAt, endsAt)

        // Refund policy
        rootView.refundPolicy.text = event.refundPolicy

        // Similar Events Section
        if (event.eventTopic != null) {
            similarEventsContainer.visibility = View.VISIBLE
            eventTopicId = event.eventTopic?.id
        }

        // Set Cover Image
        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(rootView.logo)
        }

        // Add event to Calendar
        val dateClickListener = View.OnClickListener { startCalendar(event) }
        rootView.eventDateDetailsFirst.setOnClickListener(dateClickListener)
        rootView.eventDateDetailsSecond.setOnClickListener(dateClickListener)
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
                return true
            }
            R.id.report_event -> {
                reportEvent(eventShare)
                return true
            }
            R.id.favorite_event -> {
                eventViewModel.setFavorite(eventId, !(eventShare.favorite))
                if (eventShare.favorite) {
                    setFavoriteIcon(R.drawable.ic_baseline_favorite_border_white)
                } else {
                    setFavoriteIcon(R.drawable.ic_baseline_favorite_white)
                }
                return true
            }
            R.id.event_share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(eventShare))
                sendIntent.type = "text/plain"
                rootView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setTextField(textView: TextView, value: String?) {
        if (value.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = value
        }
    }

    private fun startCalendar(event: Event) {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description?.stripHtml())
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, EventUtils.getTimeInMilliSeconds(event.startsAt))
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, EventUtils.getTimeInMilliSeconds(event.endsAt))
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

    private fun loadTicketFragment() {
        // Initialise Ticket Fragment
        val ticketFragment = TicketsFragment()
        val bundle = Bundle()
        bundle.putLong("EVENT_ID", eventId)
        bundle.putString(CURRENCY, currency)
        ticketFragment.arguments = bundle
        findNavController(rootView).navigate(R.id.ticketsFragment, bundle, getAnimSlide())
    }

    private fun loadSocialLinksFragment() {
        // Initialise SocialLinks Fragment
        val socialLinksFragemnt = SocialLinksFragment()
        val bundle = Bundle()
        bundle.putLong("EVENT_ID", eventId)
        socialLinksFragemnt.arguments = bundle
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.frameContainerSocial, socialLinksFragemnt).commit()
    }

    private fun loadSimilarEventsFragment() {
        // Initialise SimilarEvents Fragment
        val similarEventsFragment = SimilarEventsFragment()
        val bundle = Bundle()
        bundle.putLong(EVENT_ID, eventId)
        eventTopicId?.let { bundle.putLong(EVENT_TOPIC_ID, it) }
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

    private fun setFavoriteIcon(id: Int) {
        menuActionBar?.findItem(R.id.favorite_event)?.icon = context?.let { ContextCompat.getDrawable(it, id) }
    }
}
