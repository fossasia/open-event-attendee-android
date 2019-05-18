package org.fossasia.openevent.general.sessions

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_session.view.progressBar
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailTrack
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailAbstract
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailLanguage
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailContainer
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailLanguageContainer
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailLocationInfoContainer
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailLocation
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailTimeContainer
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailLocationImageMap
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailType
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailName
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailEndTime
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailStartTime
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailLocationContainer
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailInfoLocation
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailAbstractContainer
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailAbstractSeeMore
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailTrackContainer
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailSignUpButton
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailTrackIcon
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LINE_COUNT_ABSTRACT = 3

class SessionFragment : Fragment() {
    private lateinit var rootView: View
    private val sessionViewModel by viewModel<SessionViewModel>()
    private val safeArgs: SessionFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_session, container, false)

        setToolbar(activity)
        setHasOptionsMenu(true)

        sessionViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        sessionViewModel.session
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                makeSessionView(it)
            })

        sessionViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.visibility = if (it) View.VISIBLE else View.GONE
                rootView.sessionDetailContainer.visibility = if (it) View.GONE else View.VISIBLE
            })

        sessionViewModel.loadSession(safeArgs.sessionId)

        return rootView
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

    override fun onDestroy() {
        super.onDestroy()
        Utils.setNewHeaderColor(activity, resources.getColor(R.color.colorPrimaryDark),
            resources.getColor(R.color.colorPrimary))
    }

    private fun makeSessionView(session: Session) {
        when (session.title.isNullOrBlank()) {
            true -> rootView.sessionDetailName.visibility = View.GONE
            false -> {
                rootView.sessionDetailName.text = session.title
                setToolbar(activity, session.title)
            }
        }

        val type = session.sessionType
        if (type == null) {
            rootView.sessionDetailType.visibility = View.GONE
        } else {
            rootView.sessionDetailType.text = "Type: ${type.name}"
        }

        val locationInfo = session.microlocation
        if (locationInfo == null) {
            rootView.sessionDetailLocationInfoContainer.visibility = View.GONE
            rootView.sessionDetailLocationContainer.visibility = View.GONE
        } else {
            rootView.sessionDetailInfoLocation.text = locationInfo.name
            rootView.sessionDetailLocation.text = locationInfo.name
            if (locationInfo.latitude.isNullOrBlank() || locationInfo.longitude.isNullOrBlank()) {
                rootView.sessionDetailLocationImageMap.visibility = View.GONE
            } else {
                rootView.sessionDetailLocationContainer.setOnClickListener {
                    startMap(locationInfo.latitude, locationInfo.longitude)
                }
                rootView.sessionDetailLocationImageMap.setOnClickListener {
                    startMap(locationInfo.latitude, locationInfo.longitude)
                }
                Picasso.get()
                    .load(sessionViewModel.loadMap(locationInfo.latitude, locationInfo.longitude))
                    .placeholder(R.drawable.ic_map_black)
                    .error(R.drawable.ic_map_black)
                    .into(rootView.sessionDetailLocationImageMap)
            }
        }

        when (session.language.isNullOrBlank()) {
            true -> rootView.sessionDetailLanguageContainer.visibility = View.GONE
            false -> rootView.sessionDetailLanguage.text = session.language
        }

        when (session.startsAt.isNullOrBlank()) {
            true -> rootView.sessionDetailStartTime.visibility = View.GONE
            false -> {
                val formattedStartTime = EventUtils.getEventDateTime(session.startsAt, "")
                val formattedTime = EventUtils.getFormattedTime(formattedStartTime)
                val formattedDate = EventUtils.getFormattedDate(formattedStartTime)
                val timezone = EventUtils.getFormattedTimeZone(formattedStartTime)
                rootView.sessionDetailStartTime.text = "$formattedTime $timezone/ $formattedDate"
            }
        }
        when (session.endsAt.isNullOrBlank()) {
            true -> rootView.sessionDetailEndTime.visibility = View.GONE
            false -> {
                val formattedEndTime = EventUtils.getEventDateTime(session.endsAt, "")
                val formattedTime = EventUtils.getFormattedTime(formattedEndTime)
                val formattedDate = EventUtils.getFormattedDate(formattedEndTime)
                val timezone = EventUtils.getFormattedTimeZone(formattedEndTime)
                rootView.sessionDetailEndTime.text = "- $formattedTime $timezone/ $formattedDate"
            }
        }
        if (session.startsAt.isNullOrBlank() && session.endsAt.isNullOrBlank())
            rootView.sessionDetailTimeContainer.visibility = View.GONE
        else
            rootView.sessionDetailTimeContainer.setOnClickListener {
                saveSessionToCalendar(session)
            }

        val description = session.longAbstract ?: session.shortAbstract
        when (description.isNullOrBlank()) {
            true -> rootView.sessionDetailAbstractContainer.visibility = View.GONE
            false -> {
                rootView.sessionDetailAbstract.text = description
                val sessionAbstractClickListener = View.OnClickListener {
                    if (rootView.sessionDetailAbstractSeeMore.text == getString(R.string.see_more)) {
                        rootView.sessionDetailAbstractSeeMore.text = getString(R.string.see_less)
                        rootView.sessionDetailAbstract.minLines = 0
                        rootView.sessionDetailAbstract.maxLines = Int.MAX_VALUE
                    } else {
                        rootView.sessionDetailAbstractSeeMore.text = getString(R.string.see_more)
                        rootView.sessionDetailAbstract.setLines(LINE_COUNT_ABSTRACT + 1)
                    }
                }

                rootView.sessionDetailAbstract.post {
                    if (rootView.sessionDetailAbstract.lineCount > LINE_COUNT_ABSTRACT) {
                        rootView.sessionDetailAbstractSeeMore.visibility = View.VISIBLE
                        rootView.sessionDetailAbstractContainer.setOnClickListener(sessionAbstractClickListener)
                    }
                }
            }
        }

        val track = session.track
        when (track == null) {
            true -> rootView.sessionDetailTrackContainer.visibility = View.GONE
            false -> {
                rootView.sessionDetailTrack.text = track.name
                val trackColor = Color.parseColor(track.color)
                rootView.sessionDetailTrackIcon.setColorFilter(trackColor)
                Utils.setNewHeaderColor(activity, trackColor)
            }
        }

        when (session.signupUrl.isNullOrBlank()) {
            true -> rootView.sessionDetailSignUpButton.visibility = View.GONE
            false -> rootView.sessionDetailSignUpButton.setOnClickListener {
                context?.let { Utils.openUrl(it, session.signupUrl) }
            }
        }
    }

    private fun saveSessionToCalendar(session: Session) {
        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, session.title)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, session.shortAbstract)
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, session.microlocation?.name)

        if (session.startsAt != null && session.endsAt != null) {
            val formattedStartTime = EventUtils.getEventDateTime(session.startsAt, "")
            val timezone = EventUtils.getFormattedTimeZone(formattedStartTime)
            intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, timezone)
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                EventUtils.getTimeInMilliSeconds(session.startsAt, timezone))
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                EventUtils.getTimeInMilliSeconds(session.endsAt, timezone))
        }

        startActivity(intent)
    }

    private fun startMap(latitude: String, longitude: String) {
        val mapUrl = "geo:<$latitude>,<$longitude>?q=<$latitude>,<$longitude>"
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
        val packageManager = activity?.packageManager
        if (packageManager != null && mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
    }
}
