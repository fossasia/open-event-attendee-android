package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import io.reactivex.Single
import org.fossasia.openevent.general.R.id.eventName

class EventService(private val eventApi: EventApi, private val eventDao: EventDao, private val ticketsDao: TicketsDao) {

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

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun getTickets(id: Long): Single<List<Ticket>> {
        val ticketsSingle = ticketsDao.getAllTickets()
        return ticketsSingle.flatMap {
            eventApi.getTickets(id)
                    .map {
                        ticketsDao.insertTickets(it)
                    }
                    .flatMap {
                        ticketsSingle
                    }
        }
    }
}