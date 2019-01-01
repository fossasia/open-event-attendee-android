package org.fossasia.openevent.general.about

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_about_event.aboutEventCollapsingLayout
import kotlinx.android.synthetic.main.activity_about_event.aboutEventContent
import kotlinx.android.synthetic.main.activity_about_event.aboutEventDetails
import kotlinx.android.synthetic.main.activity_about_event.aboutEventToolbar
import kotlinx.android.synthetic.main.activity_about_event.appBar
import kotlinx.android.synthetic.main.activity_about_event.detailsHeader
import kotlinx.android.synthetic.main.activity_about_event.eventName
import kotlinx.android.synthetic.main.activity_about_event.progressBarAbout
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutEventActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private val aboutEventViewModel by viewModel<AboutEventViewModel>()
    private var id: Long = -1
    private val EVENT_ID: String = "EVENT_ID"
    private var isHideToolbarView: Boolean = false
    private lateinit var eventExtra: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_event)

        setSupportActionBar(aboutEventToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        aboutEventCollapsingLayout.title = " "
        appBar.addOnOffsetChangedListener(this)

        aboutEventViewModel.error
            .nonNull()
            .observe(this, Observer {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            })

        id = intent.getLongExtra(EVENT_ID, -1)

        aboutEventViewModel.progressAboutEvent
            .nonNull()
            .observe(this, Observer {
                progressBarAbout.isVisible = it
            })

        aboutEventViewModel.loadEvent(id)

        aboutEventViewModel.event
            .nonNull()
            .observe(this, Observer {
                loadEvent(it)
            })
    }

    private fun loadEvent(event: Event) {
        eventExtra = event
        aboutEventContent.text = event.description
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        aboutEventDetails.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)

        eventName.text = event.name
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()

        if (percentage == 1f && isHideToolbarView) {
            // Collapsed
            detailsHeader.visibility = View.GONE
            aboutEventCollapsingLayout.title = eventExtra.name
            isHideToolbarView = !isHideToolbarView
        }
        if (percentage < 1f && !isHideToolbarView) {
            // Not Collapsed
            detailsHeader.visibility = View.VISIBLE
            aboutEventCollapsingLayout.title = " "
            isHideToolbarView = !isHideToolbarView
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
