package org.fossasia.openevent.general.order

import io.reactivex.Single
import org.fossasia.openevent.general.attendees.AttendeeDao

class OrderService(private val orderApi: OrderApi, private val orderDao: OrderDao, private val attendeeDao: AttendeeDao) {
    fun placeOrder(order: Order): Single<Order> {
        return orderApi.placeOrder(order)
                .map { order ->
                    val attendeeIds = order.attendees?.map { order.id }
                    if (attendeeIds != null) {
                        attendeeDao.getAttendeesWithIds(attendeeIds).map {
                            if (it.size == attendeeIds.size) {
                                orderDao.insertOrder(order)
                            }
                        }
                    }
                    order
                }
    }

    fun chargeOrder(identifier: String, charge: Charge): Single<Charge> {
        return orderApi.chargeOrder(identifier, charge)
    }

}