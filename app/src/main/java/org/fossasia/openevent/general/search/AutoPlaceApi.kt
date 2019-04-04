package org.fossasia.openevent.general.search

import io.reactivex.Single
import org.fossasia.openevent.general.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path

interface AutoPlaceApi {

    @GET("/geocoding/v5/mapbox.places/{query}.json?access_token=${BuildConfig.MAPBOX_KEY}")
    fun getAutoCompletePlaceSuggestions(@Path("query") query:String):Single<AutoCompletePlaceSuggestions>
}
