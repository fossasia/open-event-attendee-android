package org.fossasia.openevent.general

import android.app.Application
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import org.fossasia.openevent.general.di.apiModule
import org.fossasia.openevent.general.di.commonModule
import org.fossasia.openevent.general.di.networkModule
import org.fossasia.openevent.general.di.viewModelModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class OpenEventGeneral : Application() {

    companion object  {
        @JvmStatic
        var appContext: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        startKoin(this, listOf(commonModule, apiModule, viewModelModule, networkModule))
        Timber.plant(Timber.DebugTree())
        AndroidThreeTen.init(applicationContext)
    }
}
