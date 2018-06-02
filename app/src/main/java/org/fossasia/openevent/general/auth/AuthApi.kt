package org.fossasia.openevent.general.auth

import io.reactivex.Single
import retrofit2.http.*

interface AuthApi {

    @POST("../auth/session")
    fun login(@Body login: Login): Single<LoginResponse>

    @GET("users/{id}")
    fun getProfile(@Path("id") id: Long): Single<User>

    @POST("users")
    fun signUp(@Body user: User): Single<User>

}