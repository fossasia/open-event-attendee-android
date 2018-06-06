package org.fossasia.openevent.general.event

import android.arch.paging.PagedList
import android.arch.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class EventService(private val eventApi: EventApi, private val eventDao: EventDao) {

    fun getEvents(): Flowable<PagedList<Event>> {
        val myPagingConfig = PagedList.Config.Builder()
                .setPageSize(50)
                .setPrefetchDistance(150)
                .build()

        val events: Flowable<PagedList<Event>> = RxPagedListBuilder(
                eventDao.getAllEvents(),
                myPagingConfig
        ).buildFlowable(BackpressureStrategy.LATEST)

        return events
    }

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

}