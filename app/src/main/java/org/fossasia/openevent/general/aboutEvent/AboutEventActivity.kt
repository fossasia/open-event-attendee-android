package org.fossasia.openevent.general.aboutEvent

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_about_event.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.ticket.AboutEventViewModel
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber
import java.lang.StringBuilder

class AboutEventActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    private val aboutEventViewModel by viewModel<AboutEventViewModel>()
    private var id: Long = -1
    private val EVENT_ID: String = "EVENT_ID"
    private var isHideToolbarView: Boolean = false
    private lateinit var eventExtra: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_about_event)

        setSupportActionBar(aboutEventToolbar)

        if (supportActionBar != null) supportActionBar?.setDisplayHomeAsUpEnabled(true)
        aboutEventCollapsingLayout.title = " "
        appbar.addOnOffsetChangedListener(this)

        aboutEventViewModel.error.observe(this, Observer {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        val intent = intent
        id = intent.getLongExtra(EVENT_ID, -1)

        aboutEventViewModel.progressAboutEvent.observe(this, Observer {
            it?.let { showProgressBar(it) }
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
        val dateString = StringBuilder("")

        aboutEventDetails.text = dateString.append(getAboutEventFormattedDate(event.startsAt))
                                           .append(" - ")
                                           .append(getAboutEventFormattedDate(event.endsAt))

        eventName.text = event.name
    }

    private fun showProgressBar(show: Boolean) {
        progressBarAbout.isIndeterminate = show
        progressBarAbout.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {

        val maxScroll = appBarLayout!!.totalScrollRange
        Timber.d("MAX "+maxScroll)
        val percentage =  Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()

        if (percentage == 1f && isHideToolbarView) {
            //Collapsed
            if (TextUtils.isEmpty(eventExtra.name)) {
                detailsHeader.visibility = View.GONE
                aboutEventCollapsingLayout.title = eventExtra.name
                Timber.d("collapse "+eventExtra.name)
                isHideToolbarView = !isHideToolbarView
            } else {
                detailsHeader.visibility = View.GONE
                aboutEventCollapsingLayout.title = eventExtra.name
                Timber.d("collapse 2"+eventExtra.name)
                isHideToolbarView = !isHideToolbarView
            }
        } else if (percentage < 1f && !isHideToolbarView) {
            //Not Collapsed
            Timber.d("no collapse"+eventExtra.name)
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

    private fun getAboutEventFormattedDate(date: String): String{

        val dateString = EventUtils.getLocalizedDateTime(date)

        //Format Month
        val month = dateString.dayOfWeek
        val lowerCaseMonth = month.toString().toLowerCase()
        val formatMonth = (lowerCaseMonth.substring(0, 1).toUpperCase() + lowerCaseMonth.substring(1)).substring(0, 3)

        //Format Day
        val day = dateString.month
        val lowerCaseDay = day.toString().toLowerCase()
        val formatDay = (lowerCaseDay.substring(0, 1).toUpperCase() + lowerCaseDay.substring(1)).substring(0, 3)

        return formatMonth + ", " + formatDay + " " + dateString.dayOfMonth
    }
}