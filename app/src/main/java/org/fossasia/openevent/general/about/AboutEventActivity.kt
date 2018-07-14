package org.fossasia.openevent.general.about

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about_event.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils
import org.koin.android.architecture.ext.viewModel
import java.lang.StringBuilder

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

        aboutEventViewModel.error.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        id = intent.getLongExtra(EVENT_ID, -1)

        aboutEventViewModel.progressAboutEvent.observe(this, Observer {
            it?.let { Utils.showProgressBar(progressBarAbout, it) }
        })

        aboutEventViewModel.loadEvent(id)

        aboutEventViewModel.event.observe(this, Observer {
            it?.let {
                loadEvent(it)
            }
        })
    }

    private fun loadEvent(event: Event){
        eventExtra = event
        aboutEventContent.text = event.description
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        aboutEventDetails.text = EventUtils.getFormattedDateTimeRangeBulleted(startsAt, endsAt)

        eventName.text = event.name
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage =  Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()

        if (percentage == 1f && isHideToolbarView) {
            //Collapsed
            detailsHeader.visibility = View.GONE
            aboutEventCollapsingLayout.title = eventExtra.name
            isHideToolbarView = !isHideToolbarView
        }
        if (percentage < 1f && !isHideToolbarView) {
            //Not Collapsed
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