package org.fossasia.openevent.general.favorite

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.DELETE

interface FavoriteEventApi {
    @GET("user-favourite-events?include=event")
    fun getFavorites(): Single<List<FavoriteEvent>>

    @POST("user-favourite-events")
    fun addFavorite(
        @Body favorite: FavoriteEvent,
        @Header("Content-Type") header: String = "application/vnd.api+json"
    ): Single<FavoriteEvent>

    @DELETE("user-favourite-events/{favoriteEventId}")
    fun removeFavorite(
        @Path("favoriteEventId") eventId: Long,
        @Header("Content-Type") header: String = "application/vnd.api+json"
    ): Completable
}
