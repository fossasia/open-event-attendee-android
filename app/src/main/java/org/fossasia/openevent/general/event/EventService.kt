package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

class EventService(private val eventApi: EventApi, private val eventDao: EventDao) {

    private var events: List<Event>? = null

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
                                events = it
                                it
                           }


    }

    fun getSearchEvent(id: Long): Single<Event> {
        return Observable.fromIterable(events)
                .filter {
                    it.id == id
                }.switchIfEmpty {
                    eventApi.getEvent(id)
                }.firstOrError()
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

}