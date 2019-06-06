package org.fossasia.openevent.general.speakercall

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.sessions.Session
import org.fossasia.openevent.general.sessions.SessionService
import org.fossasia.openevent.general.sessions.sessiontype.SessionType
import org.fossasia.openevent.general.sessions.track.Track
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class SpeakersCallProposalViewModel(
    private val resource: Resource,
    private val speakerService: SpeakerService,
    private val authHolder: AuthHolder,
    private val eventService: EventService,
    private val sessionService: SessionService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableMessage = SingleLiveEvent<String>()
    val message: LiveData<String> = mutableMessage
    private val mutableProgress = MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSubmitSuccess = MutableLiveData<Boolean>(false)
    val submitSuccess: LiveData<Boolean> = mutableSubmitSuccess
    private val mutableSpeakerProgress = MutableLiveData<Boolean>(false)
    val speakerProgress: LiveData<Boolean> = mutableSpeakerProgress
    private val mutableSpeaker = MutableLiveData<Speaker>()
    val speaker: LiveData<Speaker> = mutableSpeaker
    private val mutableSession = MutableLiveData<Session>()
    val session: LiveData<Session> = mutableSession
    private val mutableSessionTypes = MutableLiveData<List<SessionType>>()
    val sessionTypes: LiveData<List<SessionType>> = mutableSessionTypes
    private val mutableTracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = mutableTracks

    var isSpeakerInfoShown = true
    var sessionTypePosition = 0
    var trackPosition = 0

    fun submitProposal(session: Session) {
        compositeDisposable += sessionService.createSession(session)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableSubmitSuccess.value = true
            }, {
                mutableProgress.value = false
                mutableMessage.value = resource.getString(R.string.fail_create_proposal_message)
                Timber.e(it, "Fail on creating new session")
            })
    }

    fun loadSession(sessionId: Long) {
        if (sessionId == -1L) return

        compositeDisposable += sessionService.fetchSession(sessionId)
            .distinctUntilChanged()
            .withDefaultSchedulers()
            .subscribe({
                mutableSession.value = it
            }, {
                mutableMessage.value = resource.getString(R.string.fail_getting_current_proposal_message)
                Timber.e("Fail on fetching session $sessionId")
            })
    }

    fun editProposal(sessionId: Long, session: Session) {
        compositeDisposable += sessionService.updateSession(sessionId, session)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableSubmitSuccess.value = true
            }, {
                mutableProgress.value = false
                mutableMessage.value = resource.getString(R.string.fail_update_proposal_message)
                Timber.e(it, "Fail on updating session $sessionId")
            })
    }

    fun getId(): Long = authHolder.getId()

    fun loadTracks(eventId: Long) {
        if (eventId == -1L) return

        compositeDisposable += eventService.fetchTracksUnderEvent(eventId)
            .withDefaultSchedulers()
            .subscribe({
                mutableTracks.value = it
            }, {
                mutableMessage.value = resource.getString(R.string.error_fetching_tracks_message)
                Timber.e(it, "Fail on fetching tracks for event $eventId")
            })
    }

    fun loadSessionTypes(eventId: Long) {
        if (eventId == -1L) return

        compositeDisposable += eventService.fetchSessionTypesUnderEvent(eventId)
            .withDefaultSchedulers()
            .subscribe({
                mutableSessionTypes.value = it
            }, {
                mutableMessage.value = resource.getString(R.string.error_fetching_session_types_message)
                Timber.e(it, "Fail on fetching session types for event $eventId")
            })
    }

    fun loadSpeaker(speakerId: Long) {
        if (speakerId == -1L) {
            return
        }

        compositeDisposable += speakerService.fetchSpeaker(speakerId)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableSpeakerProgress.value = true
            }.subscribe({
                mutableSpeaker.value = it
                mutableSpeakerProgress.value = false
            }, {
                Timber.e(it, "Fail on fetching speaker id $speakerId")
                mutableSpeakerProgress.value = false
            })
    }
}
