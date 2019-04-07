package org.fossasia.openevent.general.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_about_event.view.appBar
import kotlinx.android.synthetic.main.fragment_about_event.view.progressBarAbout
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventContent
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventDetails
import kotlinx.android.synthetic.main.fragment_about_event.view.eventName
import kotlinx.android.synthetic.main.fragment_about_event.view.detailsHeader
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventImage
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar

class AboutEventFragment : Fragment() {
    private lateinit var rootView: View
    private val aboutEventViewModel by viewModel<AboutEventViewModel>()
    private lateinit var eventExtra: Event
    private val safeArgs: AboutEventFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = layoutInflater.inflate(R.layout.fragment_about_event, container, false)

        setToolbar(activity)
        setHasOptionsMenu(true)

        aboutEventViewModel.event
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadEvent(it)
            })

        aboutEventViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(rootView, it, Snackbar.LENGTH_SHORT).show()
            })

        aboutEventViewModel.progressAboutEvent
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBarAbout.isVisible = it
            })

        aboutEventViewModel.loadEvent(safeArgs.eventId)

        return rootView
    }

    private fun loadEvent(event: Event) {
        eventExtra = event
        rootView.aboutEventContent.text = event.description?.stripHtml()
        val startsAt = EventUtils.getEventDateTime(event.startsAt, event.timezone)
        val endsAt = EventUtils.getEventDateTime(event.endsAt, event.timezone)

        rootView.aboutEventDetails.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
        rootView.eventName.text = event.name
        Picasso.get()
            .load(event.originalImageUrl)
            .placeholder(R.drawable.header)
            .into(rootView.aboutEventImage)

        rootView.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, offset ->
            val thisActivity = activity
            if (thisActivity is AppCompatActivity) {
                if (Math.abs(offset) == appBarLayout.getTotalScrollRange()) {
                    rootView.detailsHeader.isVisible = false
                    thisActivity.supportActionBar?.title = event.name
                } else {
                    rootView.detailsHeader.isVisible = true
                    thisActivity.supportActionBar?.title = ""
                }
            }
        })
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
