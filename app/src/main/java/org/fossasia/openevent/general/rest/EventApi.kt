package org.fossasia.openevent.general.rest

import io.reactivex.Observable
import org.fossasia.openevent.general.model.EventList
import org.fossasia.openevent.general.model.Login
import org.fossasia.openevent.general.model.LoginResponse
import org.fossasia.openevent.general.model.User
import retrofit2.http.*


interface EventApi {

    @Headers("Content-Type: application/json")
    @POST("auth/session")
    fun login(@Body login: Login): Observable<LoginResponse>

    @GET("/v1/events")
    fun getEvents(@Header("Accept") app: String): Observable<EventList>

    @GET("/v1/users/{id}")
    fun getProfile(@Path("id") id: Long): Observable<User>

}