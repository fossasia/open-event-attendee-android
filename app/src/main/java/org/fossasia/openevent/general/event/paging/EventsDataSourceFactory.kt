package org.fossasia.openevent.general.event.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.reactivex.disposables.CompositeDisposable
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService

class EventsDataSourceFactory(
    private val compositeDisposable: CompositeDisposable,
    private val eventService: EventService,
    private val query: String?,
    private val mutableProgress: MutableLiveData<Boolean>
) : DataSource.Factory<Int, Event>() {
    override fun create(): DataSource<Int, Event> {
        return EventsDataSource(
            eventService,
            compositeDisposable,
            query,
            mutableProgress
        )
    }
}
