package org.fossasia.openevent.general.order

import io.reactivex.Single

class OrderService(private val orderApi: OrderApi, private val orderDao: OrderDao) {
    fun placeOrder(order: Order): Single<Order> {
        return orderApi.placeOrder(order)
                .map {
                    orderDao.insertOrder(it)
                    it
                }
    }
}