package org.fossasia.openevent.general.event

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class EventService(private val eventApi: EventApi, private val eventDao: EventDao) {

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
        return eventApi.searchEvents("name", eventName)
                .map {
                    eventDao.insertEvents(it)
                    it
                }
    }

    fun getFavoriteEvents(): Flowable<List<Event>> {
        return eventDao.getFavoriteEvents()
    }

    fun getEventsByLocation(locationName: String): Single<List<Event>> {
        return eventApi.searchEvents("name", locationName).map {
            eventDao.insertEvents(it)
            it
        }
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun setFavorite(eventId: Long, favourite: Boolean): Completable {
        return Completable.fromAction {
            eventDao.setFavorite(eventId, favourite)
        }
    }
}