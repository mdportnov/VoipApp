package ru.mephi.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.mephi.shared.data.database.DatabaseDriverFactory
import ru.mephi.shared.domain.MainDispatcher

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory() }
    single { MainDispatcher() }
}
