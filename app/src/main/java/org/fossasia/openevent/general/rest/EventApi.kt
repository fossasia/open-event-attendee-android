package org.fossasia.openevent.general.rest

import io.reactivex.Observable
import org.fossasia.openevent.general.model.Event
import org.fossasia.openevent.general.model.Login
import org.fossasia.openevent.general.model.LoginResponse
import org.fossasia.openevent.general.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface EventApi {

    @POST("auth/session")
    fun login(@Body login: Login): Observable<LoginResponse>

    @GET("/v1/events")
    fun getEvents(): Observable<List<Event>>

    @GET("/v1/users/{id}")
    fun getProfile(@Path("id") id: Long): Observable<User>

}