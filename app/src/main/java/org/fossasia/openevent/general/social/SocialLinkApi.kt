package org.fossasia.openevent.general.social

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface SocialLinkApi {

    @GET("events/{id}/social-links?include=event&fields[event]=id")
    fun getSocialLinks(@Path("id") id: Long): Single<List<SocialLink>>

}