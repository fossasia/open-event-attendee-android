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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
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
import kotlinx.android.synthetic.main.fragment_session.view.speakersUnderSessionRecycler
import kotlinx.android.synthetic.main.fragment_session.view.speakersProgressBar
import kotlinx.android.synthetic.main.fragment_session.view.sessionDetailSpeakersContainer
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SpeakerClickListener
import org.fossasia.openevent.general.speakers.SpeakerRecyclerAdapter
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LINE_COUNT_ABSTRACT = 3

class SessionFragment : Fragment() {
    private lateinit var rootView: View
    private val sessionViewModel by viewModel<SessionViewModel>()
    private val speakersAdapter = SpeakerRecyclerAdapter()
    private val safeArgs: SessionFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_session, container, false)

        setToolbar(activity)
        setHasOptionsMenu(true)

        sessionViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
                if (it == getString(R.string.error_fetching_speakers_for_session)) {
                    rootView.sessionDetailSpeakersContainer.isVisible = false
                }
            })

        sessionViewModel.session
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                makeSessionView(it)
            })

        sessionViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.isVisible = it
                rootView.sessionDetailContainer.isVisible = !it
            })

        sessionViewModel.speakersUnderSession
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                speakersAdapter.addAll(it)
                if (it.isEmpty())
                    rootView.sessionDetailSpeakersContainer.isVisible = false
                else
                    rootView.speakersProgressBar.isVisible = false
            })

        sessionViewModel.loadSession(safeArgs.sessionId)
        val currentSpeakers = sessionViewModel.speakersUnderSession.value
        if (currentSpeakers == null)
            sessionViewModel.loadSpeakersUnderSession(safeArgs.sessionId)
        else {
            speakersAdapter.addAll(currentSpeakers)
            if (currentSpeakers.isEmpty())
                rootView.sessionDetailSpeakersContainer.isVisible = false
            else
                rootView.speakersProgressBar.isVisible = false
        }

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = HORIZONTAL
        rootView.speakersUnderSessionRecycler.layoutManager = layoutManager
        rootView.speakersUnderSessionRecycler.adapter = speakersAdapter

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val speakerClickListener = object : SpeakerClickListener {
            override fun onClick(speakerId: Long) {
                findNavController(rootView).navigate(SessionFragmentDirections.actionSessionToSpeaker(speakerId))
            }
        }
        speakersAdapter.apply {
            onSpeakerClick = speakerClickListener
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

    override fun onDestroyView() {
        super.onDestroyView()
        speakersAdapter.onSpeakerClick = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.setNewHeaderColor(activity, resources.getColor(R.color.colorPrimaryDark),
            resources.getColor(R.color.colorPrimary))
    }

    private fun makeSessionView(session: Session) {
        when (session.title.isNullOrBlank()) {
            true -> rootView.sessionDetailName.isVisible = false
            false -> {
                rootView.sessionDetailName.text = session.title
                setToolbar(activity, session.title)
            }
        }

        val type = session.sessionType
        if (type == null) {
            rootView.sessionDetailType.isVisible = false
        } else {
            rootView.sessionDetailType.text = getString(R.string.type_name, type.name)
        }

        val locationInfo = session.microlocation
        if (locationInfo == null) {
            rootView.sessionDetailLocationInfoContainer.isVisible = false
            rootView.sessionDetailLocationContainer.isVisible = false
        } else {
            rootView.sessionDetailInfoLocation.text = locationInfo.name
            rootView.sessionDetailLocation.text = locationInfo.name
            if (locationInfo.latitude.isNullOrBlank() || locationInfo.longitude.isNullOrBlank()) {
                rootView.sessionDetailLocationImageMap.isVisible = false
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
            true -> rootView.sessionDetailLanguageContainer.isVisible = false
            false -> rootView.sessionDetailLanguage.text = session.language
        }

        when (session.startsAt.isNullOrBlank()) {
            true -> rootView.sessionDetailStartTime.isVisible = false
            false -> {
                val formattedStartTime = EventUtils.getEventDateTime(session.startsAt, "")
                val formattedTime = EventUtils.getFormattedTime(formattedStartTime)
                val formattedDate = EventUtils.getFormattedDate(formattedStartTime)
                val timezone = EventUtils.getFormattedTimeZone(formattedStartTime)
                rootView.sessionDetailStartTime.text = "$formattedTime $timezone/ $formattedDate"
            }
        }
        when (session.endsAt.isNullOrBlank()) {
            true -> rootView.sessionDetailEndTime.isVisible = false
            false -> {
                val formattedEndTime = EventUtils.getEventDateTime(session.endsAt, "")
                val formattedTime = EventUtils.getFormattedTime(formattedEndTime)
                val formattedDate = EventUtils.getFormattedDate(formattedEndTime)
                val timezone = EventUtils.getFormattedTimeZone(formattedEndTime)
                rootView.sessionDetailEndTime.text = "- $formattedTime $timezone/ $formattedDate"
            }
        }
        if (session.startsAt.isNullOrBlank() && session.endsAt.isNullOrBlank())
            rootView.sessionDetailTimeContainer.isVisible = false
        else
            rootView.sessionDetailTimeContainer.setOnClickListener {
                saveSessionToCalendar(session)
            }

        val description = session.longAbstract ?: session.shortAbstract
        when (description.isNullOrBlank()) {
            true -> rootView.sessionDetailAbstractContainer.isVisible = false
            false -> {
                rootView.sessionDetailAbstract.text = description.stripHtml()
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
                        rootView.sessionDetailAbstractSeeMore.isVisible = true
                        rootView.sessionDetailAbstractContainer.setOnClickListener(sessionAbstractClickListener)
                    }
                }
            }
        }

        val track = session.track
        when (track == null) {
            true -> rootView.sessionDetailTrackContainer.isVisible = false
            false -> {
                rootView.sessionDetailTrack.text = track.name
                val trackColor = Color.parseColor(track.color)
                rootView.sessionDetailTrackIcon.setColorFilter(trackColor)
                Utils.setNewHeaderColor(activity, trackColor)
            }
        }

        when (session.signupUrl.isNullOrBlank()) {
            true -> rootView.sessionDetailSignUpButton.isVisible = false
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
