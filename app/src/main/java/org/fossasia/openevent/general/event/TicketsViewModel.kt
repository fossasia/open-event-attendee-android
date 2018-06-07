package org.fossasia.openevent.general.event

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class TicketsViewModel(private val ticketService: TicketService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progressTickets = MutableLiveData<Boolean>()
    val tickets = MutableLiveData<List<Ticket>>()
    val error = MutableLiveData<String>()

    fun loadTickets(id: Long) {
        if (id.equals(-1)) {
            error.value = "Error fetching tickets"
            return
        }
        compositeDisposable.add(ticketService.getTickets(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progressTickets.value = true
                }).doFinally({
            progressTickets.value = false
        }).subscribe({ ticketList ->
            tickets.value = ticketList
        }, {
            error.value = "Error fetching tickets"
            Timber.e(it, "Error fetching tickets %d", id)
        }))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}