package ru.mephi.voip.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.mephi.voip.abto.CallEventsReceiver
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.ui.call.CallViewModel
import ru.mephi.voip.data.CatalogViewModel
import ru.mephi.voip.data.SettingsRepository
import ru.mephi.voip.utils.NotificationHandler
import ru.mephi.voip.utils.NotificationReciever
import ru.mephi.voip.vm.SettingsViewModel

val koinModule = module {
    single { CallEventsReceiver() }
}

val repositories = module {
    single { SettingsRepository(androidApplication(), get(), get()) }
    single { PhoneManager(androidApplication(), get(), get(), get()) }
    single { CatalogRepository() }
}

val notifications = module {
    single { NotificationHandler(androidApplication(), get()) }
    single { NotificationReciever(androidApplication(), get()) }
}

val viewModels = module {
    single { CallViewModel(get(), get()) }
    single { SettingsViewModel(get()) }
    single { CatalogViewModel(get()) }
}