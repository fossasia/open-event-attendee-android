package org.fossasia.openevent.general.di

import org.fossasia.openevent.general.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val flavorSpecificModule = module {
    viewModel { WelcomeViewModel(get(), get()) }
}
