package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_event.view.*
import org.fossasia.openevent.general.about.AboutEventActivity
import org.fossasia.openevent.general.social.SocialLinksFragment
import org.fossasia.openevent.general.ticket.TicketsFragment
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber
import android.os.Build
import android.support.v4.app.ActivityCompat
import org.fossasia.openevent.general.event.topic.SimilarEventsFragment
import kotlinx.android.synthetic.main.fragment_event.view.*
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import kotlinx.android.synthetic.main.content_event.*
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.SearchResultsActivity
import org.fossasia.openevent.general.ticket.CURRENCY
import java.util.*

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
    private var menuActionBar: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId = bundle.getLong(EVENT_ID, -1)
        }
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

        eventViewModel.event.observe(this, Observer {
            it?.let {
                loadEvent(it)
                eventShare = it
            }

            rootView.buttonTickets.setOnClickListener {
                    loadTicketFragment()
            }

            if (eventShare.favorite) {
                setFavoriteIcon(R.drawable.ic_baseline_favorite_white_24px)
            }
            loadSocialLinksFragment()
            loadSimilarEventsFragment()
            Timber.d("Fetched events of id %d", eventId)
        })

        eventViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventViewModel.loadEvent(eventId)

        // Set toolbar title to event name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rootView.nestedContentEventScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (thisActivity is AppCompatActivity) {
                    if (scrollY > rootView.eventName.height + rootView.logo.height)
                        /*Toolbar title set to name of Event if scrolled more than
                        combined height of eventImage and eventName views*/
                        thisActivity.supportActionBar?.title = eventShare.name
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
            setTextField(rootView.eventOrganiserDescription, event.organizerDescription)
            rootView.organizerName.text = event.organizerName.nullToEmpty()
            rootView.eventOrganiserName.visibility = View.VISIBLE
            organizerContainer.visibility = View.VISIBLE

            Picasso.get()
                    .load(event.logoUrl)
                    .placeholder(AppCompatResources.getDrawable(context!!, R.drawable.ic_person_black_24dp)!!) // TODO: Make null safe
                    .transform(CircleTransform())
                    .into(rootView.logoIcon)
        }

        currency = Currency.getInstance(event.paymentCurrency).symbol
        // About event on-click
        val aboutEventOnClickListener = View.OnClickListener {
            val aboutIntent = Intent(context, AboutEventActivity::class.java)
            aboutIntent.putExtra(EVENT_ID, eventId)
            startActivity(aboutIntent)
        }

        // Event Description Section
        if (!event.description.isNullOrEmpty()) {
            setTextField(rootView.eventDescription, event.description)

            if (rootView.eventDescription.lineCount > LINE_COUNT) {
                rootView.seeMore.visibility = View.VISIBLE
                // start about fragment
                rootView.eventDescription.setOnClickListener(aboutEventOnClickListener)
                rootView.seeMore.setOnClickListener(aboutEventOnClickListener)
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
        if (!event.locationName.isNullOrEmpty()) {
            locationContainer.visibility = View.VISIBLE
            rootView.locationUnderMap.text = event.locationName
            rootView.eventLocationLinearLayout.visibility = View.VISIBLE
            rootView.locationUnderMap.visibility = View.VISIBLE
            rootView.imageMap.visibility = View.VISIBLE
            rootView.imageMap.setOnClickListener(mapClickListener)
            rootView.eventLocationTextView.setOnClickListener(mapClickListener)

            Picasso.get()
                    .load(eventViewModel.loadMap(event))
                    .placeholder(R.drawable.ic_map_black_24dp)
                    .into(rootView.imageMap)
        }

        // Date and Time section
        rootView.eventDateDetailsFirst.text = EventUtils.getFormattedEventDateTimeRange(startsAt, endsAt)
        rootView.eventDateDetailsSecond.text = "${EventUtils.getFormattedEventDateTimeRangeSecond(startsAt, endsAt)}"

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

    override fun onDestroyView() {
        val thisActivity = activity
        when (thisActivity) {
            is SearchResultsActivity -> thisActivity.supportActionBar?.title = resources.getString(R.string.search_results)
            is MainActivity -> {
                thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
                thisActivity.supportActionBar?.title = resources.getString(R.string.events)
            }
        }
        setHasOptionsMenu(false)
        super.onDestroyView()
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
                    setFavoriteIcon(R.drawable.ic_baseline_favorite_border_white_24px)
                } else {
                    setFavoriteIcon(R.drawable.ic_baseline_favorite_white_24px)
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

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.setGroupVisible(R.id.search_menu, false)
        menu?.setGroupVisible(R.id.event_menu, true)
        super.onPrepareOptionsMenu(menu)
    }

    private fun startCalendar(event: Event) {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.description)
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        val inflaterMenu = activity?.menuInflater
        inflaterMenu?.inflate(R.menu.event_details, menu)
        menuActionBar = menu
    }

    private fun loadTicketFragment() {
        // Initialise Ticket Fragment
        val ticketFragment = TicketsFragment()
        val bundle = Bundle()
        bundle.putLong("EVENT_ID", eventId)
        bundle.putString(CURRENCY, currency)
        ticketFragment.arguments = bundle
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.coordinatorLayout, ticketFragment)?.addToBackStack(null)?.commit()
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
        childFragmentManager.beginTransaction().add(R.id.frameContainerSimilarEvents, similarEventsFragment).commit()
    }

    private fun startMap(event: Event) {
        // start map intent
        val mapUrl = eventViewModel.loadMapUrl(event)
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        if (mapIntent.resolveActivity(activity?.packageManager) != null) {
            startActivity(mapIntent)
        }
    }

    private fun setFavoriteIcon(id: Int) {
        menuActionBar?.findItem(R.id.favorite_event)?.icon = context?.let { ContextCompat.getDrawable(it, id) }
    }
}
