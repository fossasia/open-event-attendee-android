package org.fossasia.openevent.general.di

import org.fossasia.openevent.general.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val flavorSpecificModule = module {
    viewModel { WelcomeViewModel(get(), get()) }
}
