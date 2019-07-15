package org.fossasia.openevent.general.settings

import io.reactivex.Single
import retrofit2.http.GET

interface SettingsApi {

    @GET("settings")
    fun getSettings(): Single<Settings>
}
