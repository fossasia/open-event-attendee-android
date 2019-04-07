package org.fossasia.openevent.general.event

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.fossasia.openevent.general.event.topic.EventTopic
import org.fossasia.openevent.general.event.topic.EventTopicApi
import org.fossasia.openevent.general.event.topic.EventTopicsDao
import org.fossasia.openevent.general.event.types.EventType
import org.fossasia.openevent.general.event.types.EventTypesApi

import java.util.Locale.filter

class EventService(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val eventTopicApi: EventTopicApi,
    private val eventTopicsDao: EventTopicsDao,
    private val eventTypesApi: EventTypesApi
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

    fun getSearchEvents(eventName: String): Single<List<Event>> {
        return eventApi.searchEvents("name", eventName).flatMap { apiList ->
            var eventIds = apiList.map { it.id }.toList()
            eventDao.getFavoriteEventWithinIds(eventIds).flatMap { favIds ->
                updateFavorites(apiList, favIds)
            }
        }
    }

    fun getFavoriteEvents(): Flowable<List<Event>> {
        return eventDao.getFavoriteEvents()
    }

    fun getEventsByLocation(locationName: String): Single<List<Event>> {
        return eventApi.searchEvents("name", locationName).flatMap { apiList ->
            val eventIds = apiList.map { it.id }.toList()
            eventTopicsDao.insertEventTopics(getEventTopicList(apiList))
            eventDao.getFavoriteEventWithinIds(eventIds).flatMap { favIds ->
                updateFavorites(apiList, favIds)
            }
        }
    }

    fun updateFavorites(apiEvents: List<Event>, favEventIds: List<Long>): Single<List<Event>> {
        apiEvents.map { if (favEventIds.contains(it.id)) it.favorite = true }
        eventDao.insertEvents(apiEvents)
        val eventIds = apiEvents.map { it.id }.toList()
        return eventDao.getEventWithIds(eventIds)
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun getEventFromApi(id: Long): Single<Event> {
        return eventApi.getEventFromApi(id)
    }

    fun getEventsUnderUser(eventId: String): Single<List<Event>> {
        return eventApi.eventsUnderUser(eventId)
    }

    fun setFavorite(eventId: Long, favorite: Boolean): Completable {
        return Completable.fromAction {
            eventDao.setFavorite(eventId, favorite)
        }
    }

    fun getSimilarEvents(id: Long): Flowable<List<Event>> {
        val eventsFlowable = eventDao.getAllSimilarEvents(id)
        return eventsFlowable.switchMap {
            if (it.isNotEmpty())
                eventsFlowable
            else
                eventTopicApi.getEventsUnderTopicId(id)
                        .toFlowable()
                        .map {
                            eventDao.insertEvents(it)
                        }
                        .flatMap {
                            eventsFlowable
                        }
        }
    }
}
