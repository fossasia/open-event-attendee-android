package org.fossasia.openevent.general.about

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_about_event.view.progressBarAbout
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventContent
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventDetails
import kotlinx.android.synthetic.main.fragment_about_event.view.eventName
import kotlinx.android.synthetic.main.fragment_about_event.view.scrollView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutEventFragment : Fragment() {
    private lateinit var rootView: View
    private val aboutEventViewModel by viewModel<AboutEventViewModel>()
    private var id: Long = -1
    private var isHideToolbarView: Boolean = false
    private lateinit var eventExtra: Event
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null)
            id = bundle.getLong(EVENT_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = layoutInflater.inflate(R.layout.fragment_about_event, container, false)

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = ""
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        aboutEventViewModel.error
            .nonNull()
            .observe(this, Observer {
                Snackbar.make(rootView, it, Snackbar.LENGTH_SHORT).show()
            })

        aboutEventViewModel.progressAboutEvent
            .nonNull()
            .observe(this, Observer {
                rootView.progressBarAbout.isVisible = it
            })

        aboutEventViewModel.loadEvent(id)

        aboutEventViewModel.event
            .nonNull()
            .observe(this, Observer {
                loadEvent(it)
            })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rootView.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (thisActivity is AppCompatActivity) {
                    val topMargin = resources.getDimensionPixelSize(R.dimen.details_header_margin_top)
                    if (scrollY > rootView.eventName.height + topMargin)
                    /*Toolbar title set to name of Event if scrolled more than
                    event name height and detail header top margin combined */
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
        eventExtra = event
        title = eventExtra.name
        rootView.aboutEventContent.text = event.description
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        rootView.aboutEventDetails.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
        rootView.eventName.text = event.name
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
