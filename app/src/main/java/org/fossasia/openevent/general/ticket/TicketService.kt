package org.fossasia.openevent.general.ticket

import android.provider.ContactsContract
import io.reactivex.Flowable
import io.reactivex.Single

class TicketService(private val ticketApi: TicketApi, private val ticketDao: TicketDao) {

    fun getTickets(id: Long): Flowable<List<Ticket>> {
        val ticketFlowable = ticketDao.getTicketsForEvent(id)
        return ticketFlowable.switchMap {
            if (it.isNotEmpty())
                ticketFlowable
            else
                ticketApi.getTickets(id)
                        .map {
                            ticketDao.insertTickets(it)
                        }
                        .flatMap {
                            ticketFlowable
                        }
        }
    }

    fun getTicketDetails(id: Long): Single<Ticket> {
        return ticketDao.getTicketDetails(id)
    }

    fun getTicketPriceWithIds(ids: List<Int>): Single<List<Float>> {
        return ticketDao.getTicketPriceWithIds(ids)
    }

    fun getTicketsWithIds(ids: List<Int>): Single<List<Ticket>> {
        return ticketDao.getTicketsWithIds(ids)
    }

    fun getTicketsForEvent(eventId:Long) = ticketApi.getTickets(eventId)

    fun storeTicketsToLocal(tickets:List<Ticket>) = ticketDao.insertTickets(tickets)
}
