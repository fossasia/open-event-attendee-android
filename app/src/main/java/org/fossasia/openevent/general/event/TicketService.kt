package org.fossasia.openevent.general.event

import io.reactivex.Flowable

class TicketService(private val ticketApi: TicketApi, private val ticketsDao: TicketsDao) {

    fun getTickets(id: Long): Flowable<List<Ticket>> {
        val ticketsFlowable = ticketsDao.getAllTickets(id)
        return ticketsFlowable.switchMap{
            ticketApi.getTickets(id)
                    .map {
                        ticketsDao.insertTickets(it)
                    }
                    .flatMap {
                        ticketsFlowable
                    }
        }
    }
}