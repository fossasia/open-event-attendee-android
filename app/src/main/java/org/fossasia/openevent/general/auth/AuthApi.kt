package org.fossasia.openevent.general.auth

import io.reactivex.Single
import org.fossasia.openevent.general.auth.change.ChangeRequestToken
import org.fossasia.openevent.general.auth.change.ChangeRequestTokenResponse
import org.fossasia.openevent.general.auth.forgot.RequestToken
import org.fossasia.openevent.general.auth.forgot.RequestTokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @POST("../auth/session")
    fun login(@Body login: Login): Single<LoginResponse>

    @GET("users/{id}")
    fun getProfile(@Path("id") id: Long): Single<User>

    @POST("users")
    fun signUp(@Body signUp: SignUp): Single<User>

    @POST("auth/reset-password")
    fun requestToken(@Body requestToken: RequestToken): Single<RequestTokenResponse>

    @POST("auth/change-password")
    fun changeRequestToken(@Body changeRequestToken: ChangeRequestToken): Single<ChangeRequestTokenResponse>

    @PATCH("users/{id}")
    fun updateUser(@Body user: User, @Path("id") id: Long): Single<User>

    @POST("upload/image")
    fun uploadImage(@Body uploadImage: UploadImage): Single<ImageResponse>
}
