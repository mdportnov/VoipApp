package ru.mephi.voip.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.mephi.voip.abto.CallEventsReceiver
import ru.mephi.voip.data.*
import ru.mephi.voip.ui.call.CallViewModel
import ru.mephi.voip.utils.NotificationHandler
import ru.mephi.voip.utils.NotificationReciever
import ru.mephi.voip.vm.FavouritesViewModel
import ru.mephi.voip.vm.SettingsViewModel

val koinModule = module {
    single { CallEventsReceiver() }
}

val repositories = module {
    single { SettingsRepository(androidApplication(), get(), get()) }
    single { PhoneManager(androidApplication(), get(), get(), get()) }
    single { CatalogRepository() }
    single { FavouritesRepository(get()) }
}

val notifications = module {
    single { NotificationHandler(androidApplication(), get()) }
    single { NotificationReciever(androidApplication(), get()) }
}

val viewModels = module {
    single { FavouritesViewModel(get()) }
    single { CallViewModel(get(), get()) }
    single { SettingsViewModel() }
    single { CatalogViewModel(get()) }
}