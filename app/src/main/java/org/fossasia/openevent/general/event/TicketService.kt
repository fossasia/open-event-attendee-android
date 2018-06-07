package org.fossasia.openevent.general.event

import io.reactivex.Flowable

class TicketService(private val ticketApi: TicketApi, private val ticketsDao: TicketsDao) {

    fun getTickets(id: Long): Flowable<List<Ticket>> {
        return ticketApi.getTickets(id)
    }
}