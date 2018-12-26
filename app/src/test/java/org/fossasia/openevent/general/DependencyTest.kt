package org.fossasia.openevent.general

import android.app.Application
import org.fossasia.openevent.general.di.apiModule
import org.fossasia.openevent.general.di.commonModule
import org.fossasia.openevent.general.di.databaseModule
import org.fossasia.openevent.general.di.networkModule
import org.fossasia.openevent.general.di.viewModelModule
import org.junit.Test
import org.koin.android.ext.koin.with
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.test.KoinTest
import org.koin.test.dryRun
import org.mockito.Mockito.mock

class DependencyTest : KoinTest {
    @Test
    fun testDependencies() {
        // start Koin
        startKoin(listOf(commonModule, apiModule, viewModelModule,
            networkModule, databaseModule)) with mock(Application::class.java)
        // dry run of given module list
        dryRun()
    }
}
