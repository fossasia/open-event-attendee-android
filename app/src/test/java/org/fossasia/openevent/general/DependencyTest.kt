package org.fossasia.openevent.general

import android.app.Application
import org.fossasia.openevent.general.di.apiModule
import org.fossasia.openevent.general.di.commonModule
import org.fossasia.openevent.general.di.databaseModule
import org.fossasia.openevent.general.di.networkModule
import org.fossasia.openevent.general.di.viewModelModule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import org.mockito.Mockito.mock

class DependencyTest : KoinTest {
    @Test
    fun testDependencies() {
        koinApplication {
            androidContext(mock(Application::class.java))
            modules(listOf(commonModule, apiModule, databaseModule, networkModule, viewModelModule))
        }.checkModules()
    }
}
