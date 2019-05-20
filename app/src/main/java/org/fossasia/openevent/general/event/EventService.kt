package org.fossasia.openevent.general.event

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.fossasia.openevent.general.event.feedback.Feedback
import org.fossasia.openevent.general.event.feedback.FeedbackApi
import org.fossasia.openevent.general.event.faq.EventFAQ
import org.fossasia.openevent.general.event.faq.EventFAQApi
import org.fossasia.openevent.general.event.location.EventLocation
import org.fossasia.openevent.general.event.location.EventLocationApi
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.topic.EventTopicApi
import org.fossasia.openevent.general.event.topic.EventTopicsDao
import org.fossasia.openevent.general.event.types.EventType
import org.fossasia.openevent.general.event.types.EventTypesApi
import org.fossasia.openevent.general.speakercall.SpeakersCall
import org.fossasia.openevent.general.speakercall.SpeakersCallDao
import java.util.Date

class EventService(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val eventTopicApi: EventTopicApi,
    private val eventTopicsDao: EventTopicsDao,
    private val eventTypesApi: EventTypesApi,
    private val eventLocationApi: EventLocationApi,
    private val eventFeedbackApi: FeedbackApi,
    private val eventFAQApi: EventFAQApi,
    private val speakersCallDao: SpeakersCallDao
) {

    fun getEvents(): Flowable<List<Event>> {
        val eventsFlowable = eventDao.getAllEvents()
        return eventsFlowable.switchMap {
            if (it.isNotEmpty())
                eventsFlowable
            else
                eventApi.getEvents()
                    .map {
                        eventDao.insertEvents(it)
                        eventTopicsDao.insertEventTopics(getEventTopicList(it))
                    }
                    .toFlowable()
                    .flatMap {
                        eventsFlowable
                    }
        }
    }

    fun getEventLocations(): Single<List<EventLocation>> {
        return eventLocationApi.getEventLocation()
    }

    fun getEventFAQs(id: Long): Single<List<EventFAQ>> {
        return eventFAQApi.getEventFAQ(id)
    }

    private fun getEventTopicList(eventsList: List<Event>): List<EventTopic?> {
        return eventsList
            .filter { it.eventTopic != null }
            .map { it.eventTopic }
            .toList()
    }

    fun getEventTopics(): Flowable<List<EventTopic>> {
        return eventTopicsDao.getAllEventTopics()
    }

    fun getEventTypes(): Single<List<EventType>> {
        return eventTypesApi.getEventTypes()
    }

    fun getEventFeedback(id: Long): Single<List<Feedback>> {
        return eventFeedbackApi.getEventFeedback(id)
    }

    fun submitFeedback(feedback: Feedback): Single<Feedback> {
        return eventFeedbackApi.postfeedback(feedback)
    }

    fun getSearchEvents(eventName: String, sortBy: String): Flowable<List<Event>> {
        return eventApi.searchEvents(sortBy, eventName).flatMapPublisher { apiList ->
            updateFavorites(apiList)
        }
    }

    fun getFavoriteEvents(): Flowable<List<Event>> {
        return eventDao.getFavoriteEvents()
    }

    fun getEventsByLocation(locationName: String?): Flowable<List<Event>> {
        val query = "[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$locationName%\"}," +
            "{\"name\":\"ends-at\",\"op\":\"ge\",\"val\":\"%${EventUtils.getTimeInISO8601(Date())}%\"}]"
        return eventApi.searchEvents("name", query).flatMapPublisher { apiList ->
            updateFavorites(apiList)
        }
    }

    fun updateFavorites(apiList: List<Event>): Flowable<List<Event>> {

        val ids = apiList.map { it.id }.toList()
        eventTopicsDao.insertEventTopics(getEventTopicList(apiList))
        return eventDao.getFavoriteEventWithinIds(ids)
            .flatMapPublisher { favIds ->
                apiList.map { if (favIds.contains(it.id)) it.favorite = true }
                eventDao.insertEvents(apiList)
                val eventIds = apiList.map { it.id }.toList()
                eventDao.getEventWithIds(eventIds)
            }
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun getEventById(eventId: Long): Single<Event> {
        return eventDao.getEventById(eventId)
            .onErrorResumeNext {
                eventApi.getEventFromApi(eventId).map {
                    eventDao.insertEvent(it)
                    it
                }
            }
    }

    fun getEventsUnderUser(eventIds: List<Long>): Flowable<List<Event>> {
        val query = buildQuery(eventIds)
        return eventApi.eventsUnderUser(query)
            .flatMapPublisher {
                eventDao.insertEvents(it)
                eventDao.getEventWithIds(eventIds)
            }
            .onErrorResumeNext(eventDao.getEventWithIds(eventIds))
    }

    fun setFavorite(eventId: Long, favorite: Boolean): Completable {
        return Completable.fromAction {
            eventDao.setFavorite(eventId, favorite)
        }
    }

    fun getSimilarEvents(id: Long): Flowable<List<Event>> {
        return eventTopicApi.getEventsUnderTopicId(id)
            .flatMapPublisher {
                updateFavorites(it)
            }
    }

    fun getSpeakerCall(id: Long): Single<SpeakersCall> =
        speakersCallDao.getSpeakerCall(id).onErrorResumeNext {
            eventApi.getSpeakerCallForEvent(id).doAfterSuccess {
                speakersCallDao.insertSpeakerCall(it)
            }
        }

    private fun buildQuery(eventIds: List<Long>): String {
        var subQuery = ""

        var eventId = -1L
        val idList = ArrayList<Long>()
        val eventIdAndTimes = mutableMapOf<Long, Int>()
        eventIds.forEach { id ->
            val times = eventIdAndTimes[id]
            if (eventIdAndTimes.containsKey(id) && times != null) {
                eventIdAndTimes[id] = times + 1
            } else {
                eventIdAndTimes[id] = 1
            }
            idList.add(id)
            eventId = id
            subQuery += ",{\"name\":\"id\",\"op\":\"eq\",\"val\":\"$eventId\"}"
        }

        val formattedSubQuery = if (subQuery != "")
            subQuery.substring(1) // remove "," from the beginning
        else
            "" // if there are no orders

        return if (idList.size == 1)
            "[{\"name\":\"id\",\"op\":\"eq\",\"val\":\"$eventId\"}]"
        else
            "[{\"or\":[$formattedSubQuery]}]"
    }
}
