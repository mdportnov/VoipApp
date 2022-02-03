package ru.mephi.voip.di

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.mephi.shared.appContext
import ru.mephi.shared.data.network.ApiHelper
import ru.mephi.shared.data.network.KtorApiService
import ru.mephi.shared.vm.CallerViewModel
import ru.mephi.voip.call.MySipService
import ru.mephi.voip.call.abto.CallEventsReceiver
import ru.mephi.voip.call.ui.CallViewModel
import ru.mephi.voip.ui.SharedViewModel
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.ui.profile.ProfileViewModel

val koinModule = module {
    single(named("account_prefs")) { spAccounts() }
    single { spAccounts() }
    single { KtorApiService() }
    single { CallEventsReceiver() }
    single { MySipService() }
}

private fun spAccounts(): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(appContext)

val viewModels = module {
    viewModel {
        SharedViewModel(androidApplication(), get(named("account_prefs")), get())
    }

    viewModel {
        ProfileViewModel(androidApplication(), get(named("account_prefs")), get())
    }

    single {
        CallerViewModel()
    }

    viewModel {
        CallViewModel(androidApplication(), get(), get())
    }

    single {
        CatalogViewModel(get())
    }
}

//val roomDatabaseModule = module {
//    fun provideCallDatabaseDao(database: CallDatabase): CallRecordsDao {
//        return database.getCallRecordsDao()
//    }
//
//    fun provideSearchDatabaseDao(database: SearchDatabase): SearchRecordsDao {
//        return database.getSearchRecordsDao()
//    }
//
//
//    fun provideCallDatabase(app: Application): CallDatabase {
//        return Room.databaseBuilder(
//            app, CallDatabase::class.java,
//            "calls_database.db"
//        ).allowMainThreadQueries()
//            .fallbackToDestructiveMigration()
//            .build()
//    }
//
//    fun provideSearchDatabase(app: Application): SearchDatabase {
//        return Room.databaseBuilder(
//            app, SearchDatabase::class.java,
//            "search_database.db"
//        ).allowMainThreadQueries().build()
//    }
//
//    single { provideCallDatabaseDao(get()) }
//    single { provideSearchDatabaseDao(get()) }
//    single { provideCallDatabase(androidApplication()) }
//    single { provideSearchDatabase(androidApplication()) }
//}