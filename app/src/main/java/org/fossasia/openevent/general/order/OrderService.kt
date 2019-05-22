package org.fossasia.openevent.general.order

import io.reactivex.Single
import org.fossasia.openevent.general.attendees.Attendee
import org.fossasia.openevent.general.attendees.AttendeeDao
import org.fossasia.openevent.general.paypal.CreatePaypalPaymentResponse
import org.fossasia.openevent.general.paypal.Paypal
import org.fossasia.openevent.general.paypal.PaypalApi

class OrderService(
    private val orderApi: OrderApi,
    private val orderDao: OrderDao,
    private val attendeeDao: AttendeeDao,
    private val paypalApi: PaypalApi
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

    fun createPayPal(orderIdentifier: String, paypal: Paypal): Single<CreatePaypalPaymentResponse> {
        return paypalApi.createPaypalPayment(orderIdentifier, paypal)
    }
}
