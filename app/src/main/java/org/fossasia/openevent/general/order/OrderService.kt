package org.fossasia.openevent.general.order

import io.reactivex.Single
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.attendees.AttendeeDao
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventDao
import org.fossasia.openevent.general.paypal.Paypal
import org.fossasia.openevent.general.paypal.PaypalApi
import org.fossasia.openevent.general.paypal.PaypalPaymentResponse

class OrderService(
    private val orderApi: OrderApi,
    private val orderDao: OrderDao,
    private val attendeeDao: AttendeeDao,
    private val paypalApi: PaypalApi,
    private val eventDao: EventDao
) {
    fun verifyPaypalPayment(orderIdentifier: String, paymentId: String): Single<PaypalPaymentResponse> =
        paypalApi.verifyPaypalPayment(orderIdentifier, Paypal(paymentId = paymentId))

    fun getOrderAndEventSourceFactoryFromDb(showExpired: Boolean): Single<List<Pair<Event, Order>?>> {
        return orderDao.getOrders(showExpired)
            .map {
                it.map { order ->
                    order.event?.id?.let { eventId ->
                        val event = eventDao.getEventObjectById(eventId)
                        Pair(event, order)
                    }
                }
            }
    }

    fun placeOrder(order: Order): Single<Order> {
        return orderApi.placeOrder(order)
                .map { order ->
                    val attendeeIds = order.attendees.map { order.id }
                    attendeeDao.getAttendeesWithIds(attendeeIds).map {
                        if (it.size == attendeeIds.size) {
                            orderDao.insertOrder(order)
                        }
                    }
                    order
                }
    }

    fun chargeOrder(identifier: String, charge: Charge): Single<Charge> {
        return orderApi.chargeOrder(identifier, charge)
    }

    fun confirmOrder(identifier: String, order: ConfirmOrder): Single<Order> {
        return orderApi.confirmOrder(identifier, order)
    }

    fun getOrdersOfUser(userId: Long): Single<List<Order>> {
        return orderApi.ordersUnderUser(userId)
            .map {
                orderDao.insertOrders(it)
                it
            }.onErrorResumeNext {
                orderDao.getAllOrders().map { it }
            }
    }

    fun getOrdersOfUserPaged(userId: Long, query: String, page: Int, isExpired: Boolean): Single<List<Order>> {
        return orderApi.ordersUnderUserPaged(userId, query, page).map {
            val updatedOrdersList = it.map {
                it.isExpired = isExpired
                it
            }
            orderDao.insertOrders(updatedOrdersList)
            attendeeDao.insertAttendees(updatedOrdersList.map { it.attendees }.flatten())
            updatedOrdersList
        }
    }

    fun getOrderById(orderId: Long): Single<Order> {
        return orderDao.getOrderById(orderId)
    }

    fun getAttendeesUnderOrder(attendeesIds: List<Long>): Single<List<Attendee>> {
        return attendeeDao.getAttendeesWithIds(attendeesIds)
    }
}
