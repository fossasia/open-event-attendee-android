package org.fossasia.openevent.general

import android.app.Application
import android.content.Context
import timber.log.Timber
import java.lang.ref.WeakReference

class OpenEventGeneral : Application() {
    override fun onCreate() {
        super.onCreate()
        context = WeakReference(applicationContext)
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        private lateinit var context: WeakReference<Context>
        @JvmStatic
        val appContext: Context?
            get() = context.get()
    }
}
