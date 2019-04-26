package org.fossasia.openevent.general.sponsor

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface SponsorApi {
    @GET("events/{id}/sponsors")
    fun getSponsorWithEvent(@Path("id") id: Long): Single<List<Sponsor>>
}
