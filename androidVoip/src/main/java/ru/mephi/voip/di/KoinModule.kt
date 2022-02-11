package ru.mephi.voip.di

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.mephi.shared.appContext
import ru.mephi.shared.data.network.KtorApiService
import ru.mephi.voip.call.abto.CallEventsReceiver
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.data.CatalogRepository
import ru.mephi.voip.ui.call.CallViewModel
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.ui.profile.ProfileViewModel

val koinModule = module {
    single(named("account_prefs")) { spAccounts() }
    single { spAccounts() }
    single { KtorApiService() }
    single { CallEventsReceiver() }
}

private fun spAccounts(): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(appContext)

val viewModels = module {
    viewModel {
        ProfileViewModel(androidApplication(), get(named("account_prefs")), get())
    }

    viewModel {
        CallViewModel(androidApplication(), get(), get())
    }

    single {
        CatalogViewModel(get())
    }

    single {
        AccountStatusRepository(androidApplication(), get(), get())
    }

    single { CatalogRepository() }
}