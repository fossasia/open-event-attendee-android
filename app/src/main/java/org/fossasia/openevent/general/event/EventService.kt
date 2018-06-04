package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import org.fossasia.openevent.general.search.SearchEventDao

class EventService(private val eventApi: EventApi, private val eventDao: EventDao, private val searchEventDao: SearchEventDao) {

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
        val eventsFlowable = searchEventDao.getAllEvents()
        return eventsFlowable.switchMap {
            searchEventDao.deleteAll()
            eventApi.searchEvents(eventName)
                    .map {
                        searchEventDao.insertEvents(it)
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