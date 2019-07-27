package org.fossasia.openevent.general.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.android.schedulers.AndroidSchedulers

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.BuildConfig.MAPBOX_KEY
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserId
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.paging.SimilarEventsDataSourceFactory
import org.fossasia.openevent.general.favorite.FavoriteEvent
import org.fossasia.openevent.general.feedback.Feedback
import org.fossasia.openevent.general.feedback.FeedbackService
import org.fossasia.openevent.general.order.Order
import org.fossasia.openevent.general.order.OrderService
import org.fossasia.openevent.general.sessions.Session
import org.fossasia.openevent.general.sessions.SessionService
import org.fossasia.openevent.general.social.SocialLinksService
import org.fossasia.openevent.general.social.SocialLink
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerService
import org.fossasia.openevent.general.sponsor.Sponsor
import org.fossasia.openevent.general.sponsor.SponsorService
import org.fossasia.openevent.general.ticket.TicketPriceRange
import org.fossasia.openevent.general.ticket.TicketService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber
import java.lang.StringBuilder

class EventDetailsViewModel(
    private val eventService: EventService,
    private val ticketService: TicketService,
    private val authHolder: AuthHolder,
    private val speakerService: SpeakerService,
    private val sponsorService: SponsorService,
    private val sessionService: SessionService,
    private val socialLinksService: SocialLinksService,
    private val feedbackService: FeedbackService,
    private val resource: Resource,
    private val orderService: OrderService,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val config: PagedList.Config
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
    private val mutableFeedbackProgress = MutableLiveData<Boolean>()
    val feedbackProgress: LiveData<Boolean> = mutableFeedbackProgress
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
    private val mutableSimilarEvents = MutableLiveData<PagedList<Event>>()
    val similarEvents: LiveData<PagedList<Event>> = mutableSimilarEvents
    private val mutableSimilarEventsProgress = MediatorLiveData<Boolean>()
    val similarEventsProgress: MediatorLiveData<Boolean> = mutableSimilarEventsProgress
    private val mutableOrders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = mutableOrders
    private val mutablePriceRange = MutableLiveData<String>()
    val priceRange: LiveData<String> = mutablePriceRange

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun fetchEventFeedback(id: Long) {
        if (id == -1L) return

        compositeDisposable += feedbackService.getEventFeedback(id)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableFeedbackProgress.value = true
            }.doFinally {
                mutableFeedbackProgress.value = false
            }
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
        compositeDisposable += feedbackService.submitFeedback(feedback)
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

        val query = """[{
                |   'and':[{
                |       'name':'is-featured',
                |       'op':'eq',
                |       'val':'true'
                |    }]
                |}]""".trimMargin().replace("'", "\"")

        compositeDisposable += speakerService.fetchSpeakersForEvent(id, query)
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

        val sourceFactory = SimilarEventsDataSourceFactory(
            compositeDisposable,
            topicId,
            location,
            eventId,
            mutableSimilarEventsProgress,
            eventService
        )

        val similarEventPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += similarEventPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableSimilarEventsProgress.value = true
            }.subscribe({ events ->
                val currentPagedSimilarEvents = mutableSimilarEvents.value
                if (currentPagedSimilarEvents == null) {
                    mutableSimilarEvents.value = events
                } else {
                    currentPagedSimilarEvents.addAll(events)
                    mutableSimilarEvents.value = currentPagedSimilarEvents
                }
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
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableEvent.value = it
            }, {
                mutableProgress.value = false
                Timber.e(it, "Error fetching event %d", id)
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun loadEventByIdentifier(identifier: String) {
        if (identifier.isEmpty()) {
            mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable += eventService.getEventByIdentifier(identifier)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableEvent.value = it
            }, {
                Timber.e(it, "Error fetching event")
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

    fun loadOrders() {
        if (!isLoggedIn())
            return
        compositeDisposable += orderService.getOrdersOfUser(getId())
            .withDefaultSchedulers()
            .subscribe({
                mutableOrders.value = it
            }, {
                Timber.e(it, "Error fetching orders")
            })
    }

    fun syncTickets(event: Event) {
        compositeDisposable += ticketService.syncTickets(event.id)
            .withDefaultSchedulers()
            .subscribe({
                if (!it.isNullOrEmpty())
                    loadPriceRange(event)
            }, {
                Timber.e(it, "Error fetching tickets")
            })
    }

    private fun loadPriceRange(event: Event) {
        compositeDisposable += ticketService.getTicketPriceRange(event.id)
            .withDefaultSchedulers()
            .subscribe({
                setRange(it, event.paymentCurrency.toString())
            }, {
                Timber.e(it, "Error fetching ticket price range")
            })
    }

    private fun setRange(priceRange: TicketPriceRange, paymentCurrency: String) {
        val maxPrice = priceRange.maxValue
        val minPrice = priceRange.minValue
        val range = StringBuilder()
        if (maxPrice == minPrice) {
            if (maxPrice == 0f)
                range.append(resource.getString(R.string.free))
            else {
                range.append(paymentCurrency)
                range.append(" ")
                range.append("%.2f".format(minPrice))
            }
        } else {
            if (minPrice == 0f)
                range.append(resource.getString(R.string.free))
            else {
                range.append(paymentCurrency)
                range.append(" ")
                range.append("%.2f".format(minPrice))
            }
            range.append(" - ")
            range.append(paymentCurrency)
            range.append(" ")
            range.append("%.2f".format(maxPrice))
        }
        mutablePriceRange.value = range.toString()
    }

    fun loadMap(event: Event): String {
        // location handling
        val BASE_URL = "https://api.mapbox.com/v4/mapbox.emerald/pin-l-marker+673ab7"
        val LOCATION = "(${event.longitude},${event.latitude})/${event.longitude},${event.latitude}"
        return "$BASE_URL$LOCATION,15/900x500.png?access_token=$MAPBOX_KEY"
    }

    fun setFavorite(event: Event, favorite: Boolean) {
        if (favorite) {
            addFavorite(event)
        } else {
            removeFavorite(event)
        }
    }

    private fun addFavorite(event: Event) {
        val favoriteEvent = FavoriteEvent(authHolder.getId(), EventId(event.id))
        compositeDisposable += eventService.addFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                mutablePopMessage.value = resource.getString(R.string.add_event_to_shortlist_message)
            }, {
                mutablePopMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on adding like for event ID ${event.id}")
            })
    }

    private fun removeFavorite(event: Event) {
        val favoriteEventId = event.favoriteEventId ?: return

        val favoriteEvent = FavoriteEvent(favoriteEventId, EventId(event.id))
        compositeDisposable += eventService.removeFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                mutablePopMessage.value = resource.getString(R.string.remove_event_from_shortlist_message)
            }, {
                mutablePopMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on removing like for event ID ${event.id}")
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
