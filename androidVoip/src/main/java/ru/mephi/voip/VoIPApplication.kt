package ru.mephi.voip

import ru.mephi.shared.appContext
import ru.mephi.voip.abto.AbtoApp
import ru.mephi.voip.utils.PACKAGE_NAME
import timber.log.Timber

class VoIPApplication : AbtoApp() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        PACKAGE_NAME = packageName

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}