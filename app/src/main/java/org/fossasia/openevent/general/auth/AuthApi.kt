package org.fossasia.openevent.general.auth

import io.reactivex.Single
import org.fossasia.openevent.general.auth.forgot.*
import retrofit2.http.*

interface AuthApi {

    @POST("../auth/session")
    fun login(@Body login: Login): Single<LoginResponse>

    @GET("users/{id}")
    fun getProfile(@Path("id") id: Long): Single<User>

    @POST("users")
    fun signUp(@Body signUp: SignUp): Single<User>

    @PATCH("auth/reset-password")
    fun submitToken(@Body submitToken: SubmitToken): Single<SubmitTokenResponse>

    @POST("auth/reset-password")
    fun requestToken(@Body requestToken: RequestToken): Single<RequestTokenResponse>

}