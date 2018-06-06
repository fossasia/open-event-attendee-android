package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import io.reactivex.Single

class EventService(private val eventApi: EventApi, private val eventDao: EventDao) {

    lateinit var searchEvents: List<Event>

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
                    searchEvents = it
                    it
                }
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

}