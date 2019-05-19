package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.fossasia.openevent.general.BuildConfig.MAPBOX_KEY
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserId
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.feedback.Feedback
import org.fossasia.openevent.general.sessions.Session
import org.fossasia.openevent.general.sessions.SessionService
import org.fossasia.openevent.general.social.SocialLinksService
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerService
import org.fossasia.openevent.general.sponsor.Sponsor
import org.fossasia.openevent.general.sponsor.SponsorService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class EventDetailsViewModel(
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val speakerService: SpeakerService,
    private val sponsorService: SponsorService,
    private val sessionService: SessionService,
    private val socialLinksService: SocialLinksService,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser
    private val mutablePopMessage = SingleLiveEvent<String>()
    val popMessage: LiveData<String> = mutablePopMessage
    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableEventFeedback = MutableLiveData<List<Feedback>>()
    val eventFeedback: LiveData<List<Feedback>> = mutableEventFeedback
    private val mutableSubmittedFeedback = MutableLiveData<Feedback>()
    val submittedFeedback: LiveData<Feedback> = mutableSubmittedFeedback
    private val mutableEventSessions = MutableLiveData<List<Session>>()
    val eventSessions: LiveData<List<Session>> = mutableEventSessions
    private val mutableEventSpeakers = MutableLiveData<List<Speaker>>()
    val eventSpeakers: LiveData<List<Speaker>> = mutableEventSpeakers
    private val mutableEventSponsors = MutableLiveData<List<Sponsor>>()
    val eventSponsors: LiveData<List<Sponsor>> = mutableEventSponsors
    private val mutableSocialLinks = MutableLiveData<List<SocialLink>>()
    val socialLinks: LiveData<List<SocialLink>> = mutableSocialLinks
    private val mutableSimilarEvents = MutableLiveData<List<Event>>()
    val similarEvents: LiveData<List<Event>> = mutableSimilarEvents

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun fetchEventFeedback(id: Long) {
        if (id == -1L) return

        compositeDisposable += eventService.getEventFeedback(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableEventFeedback.value = it
            }, {
                Timber.e(it, "Error fetching events feedback")
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.feedback))
            })
    }

    fun submitFeedback(comment: String, rating: Float?, eventId: Long) {
        val feedback = Feedback(rating = rating.toString(), comment = comment,
            event = EventId(eventId), user = UserId(getId()))
        compositeDisposable += eventService.submitFeedback(feedback)
            .withDefaultSchedulers()
            .subscribe({
                mutablePopMessage.value = resource.getString(R.string.feedback_submitted)
                mutableSubmittedFeedback.value = it
            }, {
                mutablePopMessage.value = resource.getString(R.string.error_submitting_feedback)
            })
    }
    fun fetchEventSpeakers(id: Long) {
        if (id == -1L) return

        compositeDisposable += speakerService.fetchSpeakersForEvent(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableEventSpeakers.value = it
            }, {
                Timber.e(it, "Error fetching speaker for event id %d", id)
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.speakers))
            })
    }

    fun fetchSocialLink(id: Long) {
        if (id == -1L) return

        compositeDisposable += socialLinksService.getSocialLinks(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableSocialLinks.value = it
            }, {
                Timber.e(it, "Error fetching social link for event id $id")
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.social_links))
            })
    }

    fun fetchSimilarEvents(eventId: Long, topicId: Long, location: String?) {
        if (eventId == -1L) return

        if (topicId != -1L) {
            compositeDisposable += eventService.getSimilarEvents(topicId)
                .withDefaultSchedulers()
                .subscribe({
                    val similarEventList = mutableListOf<Event>()
                    mutableSimilarEvents.value?.let { currentEvents -> similarEventList.addAll(currentEvents) }
                    val list = it.filter { it.id != eventId }
                    similarEventList.addAll(list)
                    mutableSimilarEvents.value = similarEventList
                }, {
                    Timber.e(it, "Error fetching similar events")
                    mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                        resource.getString(R.string.similar_events))
                })
        }

        compositeDisposable += eventService.getEventsByLocation(location)
            .withDefaultSchedulers()
            .subscribe({
                val similarEventList = mutableListOf<Event>()
                mutableSimilarEvents.value?.let { currentEvents -> similarEventList.addAll(currentEvents) }
                val list = it.filter { it.id != eventId }
                similarEventList.addAll(list)
                mutableSimilarEvents.value = similarEventList
            }, {
                Timber.e(it, "Error fetching similar events")
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.similar_events))
            })
    }

    fun fetchEventSponsors(id: Long) {
        if (id == -1L) return

        compositeDisposable += sponsorService.fetchSponsorsWithEvent(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableEventSponsors.value = it
            }, {
                Timber.e(it, "Error fetching sponsor for event id %d", id)
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.sponsors))
            })
    }

    fun loadEvent(id: Long) {
        if (id == -1L) {
            mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable += eventService.getEvent(id)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableEvent.value = it
            }, {
                Timber.e(it, "Error fetching event %d", id)
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun fetchEventSessions(id: Long) {
        if (id == -1L) return

        compositeDisposable += sessionService.fetchSessionForEvent(id)
            .withDefaultSchedulers()
            .subscribe({
                mutableEventSessions.value = it
            }, {
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.sessions))
                Timber.e(it, "Error fetching events sessions")
            })
    }

    fun loadMap(event: Event): String {
        // location handling
        val BASE_URL = "https://api.mapbox.com/v4/mapbox.emerald/pin-l-marker+673ab7"
        val LOCATION = "(${event.longitude},${event.latitude})/${event.longitude},${event.latitude}"
        return "$BASE_URL$LOCATION,15/900x500.png?access_token=$MAPBOX_KEY"
    }

    fun setFavorite(eventId: Long, favorite: Boolean) {
        compositeDisposable += eventService.setFavorite(eventId, favorite)
            .withDefaultSchedulers()
            .subscribe({
                Timber.d("Success")
            }, {
                Timber.e(it, "Error")
                mutablePopMessage.value = resource.getString(R.string.error)
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
