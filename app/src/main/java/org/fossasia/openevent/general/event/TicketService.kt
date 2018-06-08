package org.fossasia.openevent.general.event

import io.reactivex.Single

class TicketService(private val ticketApi: TicketApi, private val ticketsDao: TicketsDao) {

    fun getTickets(id: Long): Single<List<Ticket>> {
        val ticketsFLowable = ticketsDao.getAllTickets(id)
        return ticketsFLowable.flatMap{
            if (it.isNotEmpty())
                ticketsFLowable
            else
                ticketApi.getTickets(id)
                        .map {
                            setEventId(it, id)
                            ticketsDao.insertTickets(it)
                        }
                        .flatMap {
                            ticketsFLowable
                        }
        }
    }

    fun setEventId(tickets: List<Ticket>, id: Long){
        for(ticket: Ticket in tickets){
            ticket.eventId = id
        }
    }
}