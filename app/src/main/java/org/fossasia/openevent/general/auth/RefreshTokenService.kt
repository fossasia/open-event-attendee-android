package org.fossasia.openevent.general.auth

import org.fossasia.openevent.general.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RefreshTokenService {

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BuildConfig.DEFAULT_BASE_URL)
        .build()

    private val authApi: AuthApi = retrofit.create(AuthApi::class.java)

    fun refreshToken(): Response<LoginResponse> {
        return authApi.refreshToken()
    }
}
