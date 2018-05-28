package org.fossasia.openevent.general.rest;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory;

import org.fossasia.openevent.general.model.User;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;


public class ApiClient {
    private static final int CONNECT_TIMEOUT_SECONDS = 15; // 15s

    private static final int READ_TIMEOUT_SECONDS = 15; // 15s

    public static final String BASE_URL = "https://open-event-api-dev.herokuapp.com/";

    private static Retrofit retrofit = null;
    private static ApiInterface apiInterface;
    private static OkHttpClient.Builder okHttpClientBuilder;
    private static Authenticator authenticator;

    static {
        okHttpClientBuilder = new OkHttpClient().newBuilder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);

    }

    public static Retrofit getClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiInterface getClient2(String TOKEN) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OkHttpClient okHttpClient = okHttpClientBuilder.addInterceptor(new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BASIC))
                .authenticator(getAuthenticator(TOKEN))
                .build();

        apiInterface = new Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(new JSONAPIConverterFactory(objectMapper, User.class))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .baseUrl(BASE_URL)
                .build()
                .create(ApiInterface.class);

        return apiInterface;

    }
    public static Authenticator getAuthenticator(String TOKEN) {
        if (authenticator == null) {
            authenticator = new Authenticator() {
                @Override
                public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
                    if (response.request().header("Authorization") != null) {
                        return null; // Give up, we've already failed to authenticate.
                    }
                    return response.request().newBuilder()
                            .header("Authorization", TOKEN)
                            .build();
                }
            };
        }
        return authenticator;
    }

}