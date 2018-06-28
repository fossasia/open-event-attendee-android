package org.fossasia.openevent.general.event

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
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
        return Single.zip(
                eventApi.searchEvents("name", eventName),
                eventDao.getFavouriteEventIds(),
                BiFunction<List<Event>, List<Long>, List<Event>> { t1, t2 -> updateFavorites(t1, t2) }
        )
    }

    fun getFavoriteEvents(): Flowable<List<Event>> {
        return eventDao.getFavoriteEvents()
    }

    fun getEventsByLocation(locationName: String): Single<List<Event>> {
        return Single.zip(
                eventApi.searchEvents("name", locationName),
                eventDao.getFavouriteEventIds(),
                BiFunction<List<Event>, List<Long>, List<Event>> { t1, t2 -> updateFavorites(t1, t2) }
        )
    }

    fun updateFavorites(apiEvents: List<Event>, favEventIds: List<Long>): List<Event> {
        apiEvents.map { if (favEventIds.contains(it.id)) it.favorite = true }
        eventDao.insertEvents(apiEvents)
        return apiEvents
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun setFavorite(eventId: Long, favourite: Boolean): Completable {
        return Completable.fromAction {
            eventDao.setFavorite(eventId, favourite)
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