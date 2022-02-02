package ru.mephi.voip.call.abto

import android.content.IntentFilter
import org.abtollc.sdk.AbtoApplication
import org.abtollc.sdk.AbtoPhone
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.di.initKoin
import ru.mephi.shared.di.repositoryModule
import ru.mephi.voip.di.koinModule

open class AbtoApp : AbtoApplication(), KoinComponent {
    private val callEventsReceiver: CallEventsReceiver by inject()

    private var appInBackgroundHandler: AppInBackgroundHandler? = null
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@AbtoApp)
            modules(koinModule)
        }

        registerReceiver(callEventsReceiver, IntentFilter(AbtoPhone.ACTION_ABTO_CALL_EVENT))
        appInBackgroundHandler = AppInBackgroundHandler()
        registerActivityLifecycleCallbacks(appInBackgroundHandler)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(callEventsReceiver)
        unregisterActivityLifecycleCallbacks(appInBackgroundHandler)
    }

    val isAppInBackground: Boolean
        get() = appInBackgroundHandler!!.isAppInBackground
}