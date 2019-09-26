package org.fossasia.openevent.general.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.ORDER_STATUS_COMPLETED
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData
import org.fossasia.openevent.general.data.Resource
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService
import org.fossasia.openevent.general.utils.extensions.withDefaultSchedulers
import timber.log.Timber

class OrdersUnderUserViewModel(
    private val orderService: OrderService,
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val config: PagedList.Config,
    private val resource: Resource

) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData
    private val mutableMessage = SingleLiveEvent<String>()
    val message: SingleLiveEvent<String> = mutableMessage
    private val mutableEventAndOrderPaged = MutableLiveData<PagedList<Pair<Event, Order>>>()
    val eventAndOrderPaged: LiveData<PagedList<Pair<Event, Order>>> = mutableEventAndOrderPaged
    private val mutableShowShimmerResults = MutableLiveData<Boolean>()
    val showShimmerResults: LiveData<Boolean> = mutableShowShimmerResults
    private val mutableNumOfTickets = MutableLiveData(0)
    val numOfTickets: LiveData<Int> = mutableNumOfTickets

    // Retain filter options
    val filter = OrderFilter()

    private lateinit var confirmOrder: ConfirmOrder

    fun getId() = authHolder.getId()

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getOrdersAndEventsOfUser(showExpired: Boolean, fromDb: Boolean) {

        val sourceFactory = OrderDataSourceFactory(
                orderService,
                eventService,
                compositeDisposable,
                showExpired,
                mutableShowShimmerResults,
                mutableNumOfTickets,
                mutableMessage,
                getId(),
                filter,
                fromDb
        )

        val ordersAndEventsPagedList = RxPagedListBuilder(sourceFactory, config)
                .setFetchScheduler(Schedulers.io())
                .buildObservable()
                .cache()

        compositeDisposable += ordersAndEventsPagedList
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .doOnSubscribe {
                    mutableShowShimmerResults.value = true
                }.subscribe({
                    val currentPagedOrdersAndEvents = mutableEventAndOrderPaged.value
                    if (currentPagedOrdersAndEvents == null) {
                        mutableEventAndOrderPaged.value = it
                    } else {
                        currentPagedOrdersAndEvents.addAll(it)
                        mutableEventAndOrderPaged.value = currentPagedOrdersAndEvents
                    }
                }, {
                    Timber.d(it, "Failed  to list events under a user ")
                })
    }

    fun clearOrders() {
        mutableEventAndOrderPaged.value = null
        mutableNumOfTickets.value = 0
    }

    fun sendPaypalConfirm(paymentId: String, pendingOrder: Order) {
        pendingOrder.let { order ->
            compositeDisposable += orderService.verifyPaypalPayment(order.identifier.toString(), paymentId)
                    .withDefaultSchedulers()
                    .doOnSubscribe {
                    }.subscribe({
                        if (it.status) {
                            confirmOrder = ConfirmOrder(order.id.toString(), ORDER_STATUS_COMPLETED)
                            confirmOrderStatus(order.identifier.toString(), confirmOrder)
                        } else {
                            mutableMessage.value = it.error
                        }
                    }, {
                        mutableMessage.value = resource.getString(R.string.error_making_paypal_payment_message)
                        Timber.e(it, "Error verifying paypal payment")
                    })
        }
    }

    private fun confirmOrderStatus(identifier: String, order: ConfirmOrder) {
        compositeDisposable += orderService.confirmOrder(identifier, order)
                .withDefaultSchedulers()
                .doFinally {
                }.subscribe({
                    mutableMessage.value = resource.getString(R.string.order_success_message)
                    Timber.d("Updated order status successfully !")
                    // orderCompleted.value = true
                }, {
                    mutableMessage.value = resource.getString(R.string.order_fail_message)
                    Timber.d(it, "Failed updating order status")
                })
    }

    fun isConnected(): Boolean = mutableConnectionLiveData.value ?: false

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}

class OrderFilter(
    var isShowingCompletedOrders: Boolean = true,
    var isShowingPendingOrders: Boolean = true,
    var isShowingPlacedOrders: Boolean = true,
    var isSortingOrdersByDate: Boolean = true
)
