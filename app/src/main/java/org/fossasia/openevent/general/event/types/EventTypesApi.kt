package org.fossasia.openevent.general.event.types

import io.reactivex.Single
import retrofit2.http.GET

interface EventTypesApi {

    @GET("event-types?sort=name")
    fun getEventTypes(): Single<List<EventType>>
}
