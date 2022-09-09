package ru.mephi.voip.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.mephi.voip.abto.CallEventsReceiver
import ru.mephi.voip.data.PhoneManager
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.ui.call.CallViewModel
import ru.mephi.voip.data.CatalogViewModel
import ru.mephi.voip.data.InitDataStore
import ru.mephi.voip.ui.profile.ProfileViewModel
import ru.mephi.voip.ui.settings.PreferenceRepository
import ru.mephi.voip.ui.settings.SettingsViewModel
import ru.mephi.voip.utils.NotificationHandler
import ru.mephi.voip.utils.NotificationReciever

val koinModule = module {
    single { CallEventsReceiver() }
}

val repositories = module {
    single { PreferenceRepository(androidApplication()) }
    single { PhoneManager(androidApplication(), get(), get(), get()) }
    single { CatalogRepository() }
}

val dataStores = module {
    single { InitDataStore(androidApplication()) }
}

val notifications = module {
    single { NotificationHandler(androidApplication(), get()) }
    single { NotificationReciever(androidApplication(), get()) }
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