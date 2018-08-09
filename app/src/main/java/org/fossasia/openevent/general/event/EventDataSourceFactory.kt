package org.fossasia.openevent.general.event

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource

class EventDataSourceFactory(private val eventDataSource: EventDataSource)
    : DataSource.Factory<Long, Event> {

    val eventDataSourceLiveData = MutableLiveData<EventDataSource>()

    override fun create(): DataSource<Long, Event> {
        eventDataSourceLiveData.postValue(eventDataSource)
        return eventDataSource
    }
}