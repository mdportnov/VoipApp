package ru.mephi.voip

import com.google.android.material.color.DynamicColors
import ru.mephi.shared.utils.appContext
import ru.mephi.voip.abto.AbtoApp
import ru.mephi.voip.utils.PACKAGE_NAME
import timber.log.Timber

class VoIPApplication : AbtoApp() {
    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)

        super.onCreate()
        appContext = this
        PACKAGE_NAME = packageName

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}