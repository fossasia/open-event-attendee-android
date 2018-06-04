package org.fossasia.openevent.general.event

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TicketsViewModel(private val eventService: EventService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val progressTickets = MutableLiveData<Boolean>()
    val tickets = MutableLiveData<List<Ticket>>()
    val error = MutableLiveData<String>()

    fun loadTickets(identifier : String) {
        compositeDisposable.add(eventService.getTickets(identifier)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progressTickets.value = true
                }).doFinally({
                    progressTickets.value = false
                }).subscribe({ticketList ->
                    tickets.value = ticketList
                }, {
                    error.value = "Error fetching tickets"
                }))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}