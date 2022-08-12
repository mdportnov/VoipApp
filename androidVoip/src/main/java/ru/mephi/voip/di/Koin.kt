package ru.mephi.voip.di

import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.mephi.voip.abto.CallEventsReceiver
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.ui.call.CallViewModel
import ru.mephi.voip.data.CatalogViewModel
import ru.mephi.voip.data.InitDataStore
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.ui.settings.SettingsViewModel
import ru.mephi.voip.utils.NotificationHandler

val koinModule = module {
    single { CallEventsReceiver() }
    single { NotificationHandler(androidApplication()) }
}

val repositories = module {
    single { PreferenceRepository(androidApplication()) }
    single { AccountStatusRepository(androidApplication(), get(), get(), get()) }
    single { CatalogRepository() }
}

val dataStores = module {
    single { InitDataStore(androidApplication()) }
}

val viewModels = module {
    viewModel {
        ProfileViewModel(androidApplication(), get(), get(), get())
    }

    single {
        CallViewModel(get(), get())
    }

    single {
        CatalogViewModel(get())
    }

    single {
        SettingsViewModel(androidApplication(), get(), get(), get())
    }
}