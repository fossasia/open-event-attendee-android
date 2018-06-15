package org.fossasia.openevent.general.event

import io.reactivex.Flowable
import org.fossasia.openevent.general.ticket.Ticket
import org.fossasia.openevent.general.ticket.TicketApi

class TicketService(private val ticketApi: TicketApi, private val ticketsDao: TicketsDao) {

    fun getTickets(id: Long): Flowable<List<Ticket>> {

        val ticketFlowable = ticketsDao.getTicketsForEvent(id)
        return ticketFlowable.switchMap {
            if (it.isNotEmpty())
                ticketFlowable
            else
                ticketApi.getTickets(id)
                        .map {
                            ticketsDao.insertTickets(it)
                        }
                        .flatMap {
                            ticketFlowable
                        }
        }
    }
}
