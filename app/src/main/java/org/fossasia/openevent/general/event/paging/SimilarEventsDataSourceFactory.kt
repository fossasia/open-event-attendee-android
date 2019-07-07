package org.fossasia.openevent.general.event.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.reactivex.disposables.CompositeDisposable
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventService

class SimilarEventsDataSourceFactory(
    private val compositeDisposable: CompositeDisposable,
    private val topicId: Long,
    private val location: String?,
    private val eventId: Long,
    private val mutableProgress: MutableLiveData<Boolean>,
    private val eventService: EventService
) : DataSource.Factory<Int, Event>() {
    override fun create(): DataSource<Int, Event> {
        return SimilarEventsDataSource(
            compositeDisposable, topicId, location, eventId, mutableProgress, eventService
        )
    }
}
