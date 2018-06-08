package org.fossasia.openevent.general.event

import android.arch.paging.PagedList
import android.arch.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single

class EventService(private val eventApi: EventApi, private val eventDao: EventDao) {

    fun getEvents(): Flowable<PagedList<Event>> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(50)
                .setPrefetchDistance(150)
                .build()

        val events: Flowable<PagedList<Event>> = RxPagedListBuilder(
                eventDao.getAllEvents(),
                myPagingConfig
        ).setBoundaryCallback(BoundaryCallback(eventDao, eventApi))
                .buildFlowable(BackpressureStrategy.LATEST)

        return events
    }

    fun getSearchEvents(eventName: String): Single<List<Event>> {
        return eventApi.searchEvents("name", eventName)
                .map {
                    eventDao.insertEvents(it)
                    it
                }
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

}