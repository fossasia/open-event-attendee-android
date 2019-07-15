package org.fossasia.openevent.general.settings

import io.reactivex.Single

class SettingsService(
    private val settingsApi: SettingsApi,
    private val settingsDao: SettingsDao
) {

    fun fetchSettings(): Single<Settings> {
        return settingsApi.getSettings().map {
            settingsDao.insertSettings(it)
            it
        }.flatMap {
            settingsDao.getSettings()
        }
    }
}
