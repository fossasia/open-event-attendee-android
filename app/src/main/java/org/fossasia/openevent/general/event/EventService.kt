package org.fossasia.openevent.general.event

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.fossasia.openevent.general.event.topic.EventTopicApi

class EventService(private val eventApi: EventApi, private val eventDao: EventDao, private val eventTopicApi: EventTopicApi) {

    fun getEvents(): Flowable<List<Event>> {
        val eventsFlowable = eventDao.getAllEvents()
        return eventsFlowable.switchMap {
            if (it.isNotEmpty())
                eventsFlowable
            else
                eventApi.getEvents()
                        .map {
                            eventDao.insertEvents(it)
                        }
                        .toFlowable()
                        .flatMap {
                            eventsFlowable
                        }
        }
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

    fun setFavorite(eventId: Long, favourite: Boolean): Completable {
        return Completable.fromAction {
            eventDao.setFavorite(eventId, favourite)
        }
    }

    fun getSimilarEvents(id: Long, savedLocation: String): Single<List<Event>> {
        return eventTopicApi.getEventsUnderTopicId(id, "name", savedLocation).flatMap { apiList ->
            val eventIds = apiList.map { it.id }.toList()
            eventDao.getFavoriteEventWithinIds(eventIds).flatMap { favIds ->
                updateFavorites(apiList, favIds)
            }
        }
    }
}