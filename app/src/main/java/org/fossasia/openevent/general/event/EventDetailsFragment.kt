package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_event.view.*
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class EventDetailsFragment : Fragment() {
    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private lateinit var rootView: View
    private var eventId : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId = bundle.getLong("EVENT_ID", 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_event, container, false)

        eventViewModel.event.observe(this, Observer {
            it?.let {
               loadEvent(it)
            }
            Timber.d("Fetched events of id %d", eventId)
        })

        eventViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventViewModel.progress.observe(this, Observer {
            it?.let { showProgressBar(it) }
        })

        eventViewModel.loadEvent(eventId)

        return rootView
    }

    private fun showProgressBar(show: Boolean) {
        rootView.eventDetailsProgressBar.isIndeterminate = show
        rootView.eventDetailsProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun loadEvent(event: Event?) {
        if (event == null)
            return
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        rootView.event_name.setText(event.name)
        setTextField(rootView.event_description , event.description);
        setTextField(rootView.organiser_description , event.organizerDescription)
        rootView.event_venue_details.setText(event.locationName)

        rootView.starts_on.setText("${startsAt.dayOfMonth} ${startsAt.month} ${startsAt.year}")
        rootView.ends_on.setText("${endsAt.dayOfMonth} ${endsAt.month} ${endsAt.year}")

        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(rootView.logo)
        }
    }

    private fun setTextField(textView: TextView, value: String?) {
        if(!value.isNullOrEmpty()){
            textView.setText(value)
        } else {
            textView.visibility = View.GONE
        }
    }
}