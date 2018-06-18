package org.fossasia.openevent.general.ticket

import io.reactivex.Flowable

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
