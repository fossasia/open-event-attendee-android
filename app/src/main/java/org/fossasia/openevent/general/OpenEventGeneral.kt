package org.fossasia.openevent.general

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.jakewharton.threetenabp.AndroidThreeTen
import org.fossasia.openevent.general.di.apiModule
import org.fossasia.openevent.general.di.commonModule
import org.fossasia.openevent.general.di.databaseModule
import org.fossasia.openevent.general.di.flavorSpecificModule
import org.fossasia.openevent.general.di.networkModule
import org.fossasia.openevent.general.di.viewModelModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class OpenEventGeneral : MultiDexApplication() {

    companion object {
        @JvmStatic
        var appContext: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        startKoin(this, listOf(
            commonModule, apiModule, viewModelModule, networkModule, databaseModule, flavorSpecificModule
        ))
        Timber.plant(Timber.DebugTree())
        AndroidThreeTen.init(applicationContext)
    }
}
