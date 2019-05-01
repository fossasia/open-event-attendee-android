package org.fossasia.openevent.general.favorite

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserFavoriteApi {

    @GET("user-favourite-events")
    fun getFavorites(): Single<List<UserFavorite>>

    @POST("user-favourite-events")
    fun postFavorite(@Body favorite: UserFavorite): Single<UserFavorite>

    @DELETE("user-favourite-events/{id}")
    fun removeFavourite(@Path("id") eventId: Long): Single<ResponseBody>
}
