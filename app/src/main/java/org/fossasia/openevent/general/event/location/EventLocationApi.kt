package org.fossasia.openevent.general.event.location

import io.reactivex.Single
import retrofit2.http.GET

interface EventLocationApi {

    @GET("event-locations?sort=name")
    fun getEventLocation(): Single<List<EventLocation>>
}
