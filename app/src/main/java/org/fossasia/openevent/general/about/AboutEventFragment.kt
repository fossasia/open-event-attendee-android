package org.fossasia.openevent.general.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_about_event.view.appBar
import kotlinx.android.synthetic.main.fragment_about_event.view.progressBarAbout
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventContent
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventDetails
import kotlinx.android.synthetic.main.fragment_about_event.view.eventName
import kotlinx.android.synthetic.main.fragment_about_event.view.detailsHeader
import kotlinx.android.synthetic.main.fragment_about_event.view.aboutEventCollapsingLayout
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EVENT_ID
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutEventFragment : Fragment(), AppBarLayout.OnOffsetChangedListener {
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

        aboutEventViewModel.event
            .nonNull()
            .observe(this, Observer {
                loadEvent(it)
            })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = layoutInflater.inflate(R.layout.fragment_about_event, container, false)

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.title = ""
            thisActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        rootView.appBar.addOnOffsetChangedListener(this)

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

        return rootView
    }

    private fun loadEvent(event: Event) {
        eventExtra = event
        title = eventExtra.name
        rootView.aboutEventContent.text = event.description?.stripHtml()
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        rootView.aboutEventDetails.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)
        rootView.eventName.text = event.name
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()

        if (percentage == 1f && isHideToolbarView) {
            // Collapsed
            rootView.detailsHeader.visibility = View.GONE
            rootView.aboutEventCollapsingLayout.title = title
            isHideToolbarView = !isHideToolbarView
        }
        if (percentage < 1f && !isHideToolbarView) {
            // Not Collapsed
            rootView.detailsHeader.visibility = View.VISIBLE
            rootView.aboutEventCollapsingLayout.title = " "
            isHideToolbarView = !isHideToolbarView
        }
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
