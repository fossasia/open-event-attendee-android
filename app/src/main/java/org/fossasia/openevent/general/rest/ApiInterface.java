package org.fossasia.openevent.general.rest;

import org.fossasia.openevent.general.model.EventList;
import org.fossasia.openevent.general.model.Login;
import org.fossasia.openevent.general.model.LoginResponse;
import org.fossasia.openevent.general.model.User;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("auth/session")
    Observable<Response<LoginResponse>> login(@Body Login login);

    @GET("/v1/events")
    Observable<Response<EventList>> getEvents(@Header("Accept") String app);

    @GET("/v1/users/{id}")
    Observable<Response<User>> getProfile(@Path("id") long id);

}