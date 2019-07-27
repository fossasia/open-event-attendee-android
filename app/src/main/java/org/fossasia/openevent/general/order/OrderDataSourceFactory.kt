package org.fossasia.openevent.general.order

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.reactivex.disposables.CompositeDisposable
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService

class OrderDataSourceFactory(
    private val orderService: OrderService,
    private val eventService: EventService,
    private val compositeDisposable: CompositeDisposable,
    private val showExpired: Boolean,
    private val mutableProgress: MutableLiveData<Boolean>,
    private val mutableNumOfTicket: MutableLiveData<Int>,
    private val mutableMessage: SingleLiveEvent<String>,
    private val userId: Long,
    private val orderFilter: OrderFilter
) : DataSource.Factory<Int, Pair<Event, Order>>() {
    override fun create(): DataSource<Int, Pair<Event, Order>> {
        return OrderDataSource(
            orderService,
            eventService,
            compositeDisposable,
            userId,
            showExpired,
            mutableProgress,
            mutableNumOfTicket,
            mutableMessage,
            orderFilter
        )
    }
}
