package org.fossasia.openevent.general.order

import io.reactivex.Single
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.attendees.AttendeeDao

class OrderService(
    private val orderApi: OrderApi,
    private val orderDao: OrderDao,
    private val attendeeDao: AttendeeDao
) {
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

    fun getOrdersOfUserPaged(userId: Long, query: String, page: Int): Single<List<Order>> {
        return orderApi.ordersUnderUserPaged(userId, query, page).map {
            orderDao.insertOrders(it)
            it
        }
    }

    fun getOrderById(orderId: Long): Single<Order> {
        return orderDao.getOrderById(orderId)
    }

    fun getAttendeesUnderOrder(orderIdentifier: String, attendeesIds: List<Long>): Single<List<Attendee>> {
        return orderApi.attendeesUnderOrder(orderIdentifier)
            .map {
                attendeeDao.insertAttendees(it)
                it
            }.onErrorResumeNext {
                attendeeDao.getAttendeesWithIds(attendeesIds)
            }
    }
}
