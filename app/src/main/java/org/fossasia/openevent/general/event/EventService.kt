package org.fossasia.openevent.general.event

import io.reactivex.Single

class EventService(private val eventApi: EventApi) {

    private var events: List<Event>? = null // TODO: Cache to be later replaced by Database

    fun getEvents(): Single<List<Event>> {
        if (events != null)
            return Single.just(events)
        return eventApi.getEvents()
                .map {
                    events = it
                    it
                }
    }

}