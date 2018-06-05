package org.fossasia.openevent.general.event

import io.reactivex.Flowable

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

    fun getSearchEvents(eventName: String): Flowable<List<Event>> {
        val eventsFlowable = eventDao.getAllEventsByName()
        return eventsFlowable.switchMap {
            eventDao.deleteAll()
            eventApi.searchEvents(eventName)
                    .map {
                        eventDao.insertEvents(it)
                    }
                    .toFlowable()
                    .flatMap {
                        eventsFlowable
                    }
        }
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

}