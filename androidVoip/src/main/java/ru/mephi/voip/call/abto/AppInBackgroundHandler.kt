package ru.mephi.voip.call.abto

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class AppInBackgroundHandler : ActivityLifecycleCallbacks {
    private var activeActivities = 0
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        activeActivities++
    }

    override fun onActivityPaused(activity: Activity) {
        activeActivities--
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    val isAppInBackground: Boolean
        get() = activeActivities == 0
}