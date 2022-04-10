package ru.mephi.voip.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.mephi.shared.appContext
import ru.mephi.shared.data.network.KtorApiService
import ru.mephi.voip.abto.CallEventsReceiver
import ru.mephi.voip.data.*
import ru.mephi.voip.ui.call.CallViewModel
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.ui.profile.ProfileViewModel

val koinModule = module {
    single(named("account_prefs")) { provideSharedPrefs() }
    single { provideSharedPrefs() }
    single { KtorApiService() }
    single { CallEventsReceiver() }
}


private fun providePreferenceDataStore(): SettingsStore {
    return DataStoreSettings(appContext)
}

private fun provideSharedPrefs(): SettingsStore {
    return SharedPrefsSettings()
}

val repositories = module {
    single {
        AccountStatusRepository(androidApplication(), get(), get())
    }

    single { CatalogRepository() }
}

val viewModels = module {
    single {
        ProfileViewModel(androidApplication(), get())
    }

    single {
        CallViewModel(get(), get())
    }

    single {
        CatalogViewModel(get())
    }
}