package org.fossasia.openevent.general.auth

import io.reactivex.Single
import org.fossasia.openevent.general.auth.forgot.RequestToken
import org.fossasia.openevent.general.auth.forgot.RequestTokenResponse
import retrofit2.http.*

interface AuthApi {

    @POST("../auth/session")
    fun login(@Body login: Login): Single<LoginResponse>

    @GET("users/{id}")
    fun getProfile(@Path("id") id: Long): Single<User>

    @POST("users")
    fun signUp(@Body signUp: SignUp): Single<User>

    @POST("auth/reset-password")
    fun requestToken(@Body requestToken: RequestToken): Single<RequestTokenResponse>

    @PATCH("users/{id}")
    fun updateUser(@Body user: User, @Path("id") id: Long): Single<User>

    @POST("upload/image")
    fun uploadImage(@Body uploadImage: UploadImage): Single<ImageResponse>

}