package ru.mephi.shared.di

import kotlinx.coroutines.Dispatchers
import org.kodein.db.DB
import org.kodein.db.impl.open
import org.kodein.db.orm.kotlinx.KotlinxSerializer
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import ru.mephi.shared.data.database.CallsDB
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.database.SearchDB
import ru.mephi.shared.data.database.dto.AppointmentKodein
import ru.mephi.shared.data.database.dto.UnitMKodeIn
import ru.mephi.shared.data.model.NameItem
import ru.mephi.shared.data.network.ApiHelper
import ru.mephi.shared.data.repository.CallsRepository
import ru.mephi.shared.data.repository.CatalogRepository
import ru.mephi.shared.getApplicationFilesDirectoryPath
import kotlin.math.sin

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(
            platformModule(),
            repositoryModule,
            dispatcherModule,
        )
    }

fun initKoin() = initKoin {} // uses in AppDelegate

val dispatcherModule = module {
    factory { Dispatchers.Default }
}

val repositoryModule = module {
    fun provideCatalogRepository(
        api: ApiHelper,
        searchRecordsDao: SearchDB,
        dao: CatalogDao
    ): CatalogRepository {
        return CatalogRepository(api, searchRecordsDao, dao)
    }

    fun provideCallsRepository(dao: CallsDB): CallsRepository {
        return CallsRepository(dao)
    }

    fun provideCatalogDB() = DB.open(getApplicationFilesDirectoryPath(),
        KotlinxSerializer {
            +UnitMKodeIn.serializer()
            +AppointmentKodein.serializer()
            +NameItem.serializer()
        })

    single { provideCatalogDB() }
    single { CatalogDao(get()) }
    single { SearchDB(get()) }
    single { CallsDB(get()) }
    single { provideCatalogRepository(get(), get(), get()) }
    single { provideCallsRepository(get()) }
}
