package org.fossasia.openevent.general.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.fossasia.openevent.general.model.User
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {
    private val CONNECT_TIMEOUT_SECONDS = 15 // 15s

    private val READ_TIMEOUT_SECONDS = 15 // 15s

    private val BASE_URL = "https://open-event-api-dev.herokuapp.com/"

    private val authHolder = AuthHolder()

    private val objectMapper: ObjectMapper by lazy {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient().newBuilder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS.toLong(), TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BASIC))
                .authenticator(authenticator)
                .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JSONAPIConverterFactory(objectMapper, User::class.java))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(BASE_URL)
                .build()
    }

    private val authenticator: Authenticator by lazy {
        Authenticator { _, response ->
            if (response.request().header("Authorization") == null) {
                authHolder.token?.let {
                    response.request().newBuilder()
                            .header("Authorization", it)
                            .build()
                }
            } else null // Give up, we already tried authenticating
        }
    }

    @JvmStatic
    val eventApi: EventApi by lazy {
        retrofit.create(EventApi::class.java)
    }

    @JvmStatic
    fun setToken(token: String?) {
        authHolder.token = token
    }

    data class AuthHolder(var token: String? = null)

}